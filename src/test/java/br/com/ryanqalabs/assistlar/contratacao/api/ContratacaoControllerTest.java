package br.com.ryanqalabs.assistlar.contratacao.api;

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
import br.com.ryanqalabs.assistlar.contratacao.aplicacao.ContratacaoService;
import br.com.ryanqalabs.assistlar.contratacao.dominio.StatusContratacao;
import br.com.ryanqalabs.assistlar.historico.api.HistoricoStatusResposta;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoResponsavel;

@WebMvcTest(ContratacaoController.class)
@Import(TempoConfiguracao.class)
class ContratacaoControllerTest {

    private static final UUID CONTRATACAO_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID CLIENTE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID PLANO_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final Instant INSTANTE = Instant.parse("2026-07-20T15:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratacaoService service;

    @Test
    void deveCriarContratacaoPendenteComLocation() throws Exception {
        when(service.criar(any())).thenReturn(resposta(StatusContratacao.PENDENTE));

        mockMvc.perform(post("/api/contratacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":"%s","planoId":"%s"}
                                """.formatted(CLIENTE_ID, PLANO_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/contratacoes/" + CONTRATACAO_ID))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveConsultarAtivarCancelarEListarHistorico() throws Exception {
        when(service.buscar(CONTRATACAO_ID)).thenReturn(resposta(StatusContratacao.PENDENTE));
        when(service.ativar(CONTRATACAO_ID)).thenReturn(resposta(StatusContratacao.ATIVA));
        when(service.cancelar(any(), any())).thenReturn(resposta(StatusContratacao.CANCELADA));
        when(service.listarHistorico(CONTRATACAO_ID)).thenReturn(List.of(new HistoricoStatusResposta(
                UUID.randomUUID(), null, "PENDENTE", null, TipoResponsavel.CLIENTE, INSTANTE)));

        mockMvc.perform(get("/api/contratacoes/{id}", CONTRATACAO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
        mockMvc.perform(post("/api/contratacoes/{id}/ativacao", CONTRATACAO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVA"));
        mockMvc.perform(post("/api/contratacoes/{id}/cancelamento", CONTRATACAO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Solicitado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
        mockMvc.perform(get("/api/contratacoes/{id}/historico", CONTRATACAO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoResponsavel").value("CLIENTE"));
    }

    @Test
    void deveRetornarUnprocessableEntityQuandoClienteInelegivel() throws Exception {
        when(service.criar(any())).thenThrow(new ExcecaoRegraNegocio(
                "cliente-inelegivel", "O cliente nao pode contratar o plano."));

        mockMvc.perform(post("/api/contratacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":"%s","planoId":"%s"}
                                """.formatted(CLIENTE_ID, PLANO_ID)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("/erros/cliente-inelegivel"));
    }

    @Test
    void deveRejeitarResponsavelInformadoNoPayload() throws Exception {
        mockMvc.perform(post("/api/contratacoes/{id}/cancelamento", CONTRATACAO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"Solicitado","tipoResponsavel":"CLIENTE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/requisicao-invalida"));
    }

    @Test
    void deveRejeitarPayloadSemIds() throws Exception {
        mockMvc.perform(post("/api/contratacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.length()").value(2));
    }

    private ContratacaoResposta resposta(StatusContratacao status) {
        return new ContratacaoResposta(CONTRATACAO_ID, CLIENTE_ID, PLANO_ID, status,
                INSTANTE, status == StatusContratacao.ATIVA ? INSTANTE : null,
                status == StatusContratacao.CANCELADA ? INSTANTE : null, 0);
    }
}
