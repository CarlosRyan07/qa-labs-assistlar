package br.com.ryanqalabs.assistlar.solicitacao.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.ryanqalabs.assistlar.compartilhado.configuracao.TempoConfiguracao;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoRegraNegocio;
import br.com.ryanqalabs.assistlar.historico.api.HistoricoStatusResposta;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoResponsavel;
import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;
import br.com.ryanqalabs.assistlar.solicitacao.aplicacao.SolicitacaoAssistenciaService;
import br.com.ryanqalabs.assistlar.solicitacao.dominio.StatusSolicitacao;

@WebMvcTest(SolicitacaoAssistenciaController.class)
@Import(TempoConfiguracao.class)
class SolicitacaoAssistenciaControllerTest {

    private static final UUID SOLICITACAO_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID CONTRATACAO_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final Instant INSTANTE = Instant.parse("2026-07-20T15:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitacaoAssistenciaService service;

    @Test
    void deveAbrirSolicitacaoComLocation() throws Exception {
        when(service.abrir(any())).thenReturn(resposta(StatusSolicitacao.ABERTA));

        mockMvc.perform(post("/api/solicitacoes-assistencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requisicaoCriacao()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "http://localhost/api/solicitacoes-assistencia/" + SOLICITACAO_ID))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void deveConsultarIniciarConcluirCancelarEListarHistorico() throws Exception {
        when(service.buscar(SOLICITACAO_ID)).thenReturn(resposta(StatusSolicitacao.ABERTA));
        when(service.iniciar(SOLICITACAO_ID)).thenReturn(resposta(StatusSolicitacao.EM_ATENDIMENTO));
        when(service.concluir(SOLICITACAO_ID)).thenReturn(resposta(StatusSolicitacao.CONCLUIDA));
        when(service.cancelar(any(), any())).thenReturn(resposta(StatusSolicitacao.CANCELADA));
        when(service.listarHistorico(SOLICITACAO_ID)).thenReturn(List.of(new HistoricoStatusResposta(
                UUID.randomUUID(), null, "ABERTA", null, TipoResponsavel.CLIENTE, INSTANTE)));

        mockMvc.perform(get("/api/solicitacoes-assistencia/{id}", SOLICITACAO_ID))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ABERTA"));
        mockMvc.perform(post("/api/solicitacoes-assistencia/{id}/inicio", SOLICITACAO_ID))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
        mockMvc.perform(post("/api/solicitacoes-assistencia/{id}/conclusao", SOLICITACAO_ID))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONCLUIDA"));
        mockMvc.perform(post("/api/solicitacoes-assistencia/{id}/cancelamento", SOLICITACAO_ID)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Resolvido\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"));
        mockMvc.perform(get("/api/solicitacoes-assistencia/{id}/historico", SOLICITACAO_ID))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].tipoResponsavel").value("CLIENTE"));
    }

    @Test
    void deveRetornarUnprocessableEntityParaRegraDeNegocio() throws Exception {
        when(service.abrir(any())).thenThrow(new ExcecaoRegraNegocio(
                "servico-nao-coberto", "O tipo de assistencia nao e coberto pelo plano contratado."));

        mockMvc.perform(post("/api/solicitacoes-assistencia")
                        .contentType(MediaType.APPLICATION_JSON).content(requisicaoCriacao()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("/erros/servico-nao-coberto"));
    }

    @Test
    void deveRejeitarResponsavelNoPayloadDeCancelamento() throws Exception {
        mockMvc.perform(post("/api/solicitacoes-assistencia/{id}/cancelamento", SOLICITACAO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Resolvido\",\"tipoResponsavel\":\"CLIENTE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/requisicao-invalida"));
    }

    @Test
    void deveRejeitarPayloadIncompletoOuEnumInvalido() throws Exception {
        mockMvc.perform(post("/api/solicitacoes-assistencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoAssistencia\":\"JARDINEIRO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/requisicao-invalida"));
    }

    private String requisicaoCriacao() {
        return """
                {"contratacaoId":"%s","tipoAssistencia":"ELETRICISTA","descricaoProblema":"Tomada sem energia"}
                """.formatted(CONTRATACAO_ID);
    }

    private SolicitacaoResposta resposta(StatusSolicitacao status) {
        return new SolicitacaoResposta(SOLICITACAO_ID, CONTRATACAO_ID, TipoAssistencia.ELETRICISTA,
                "Tomada sem energia", status, status == StatusSolicitacao.CANCELADA ? "Resolvido" : null,
                INSTANTE, status == StatusSolicitacao.EM_ATENDIMENTO ? INSTANTE : null,
                status == StatusSolicitacao.CONCLUIDA ? INSTANTE : null,
                status == StatusSolicitacao.CANCELADA ? INSTANTE : null, 0);
    }
}
