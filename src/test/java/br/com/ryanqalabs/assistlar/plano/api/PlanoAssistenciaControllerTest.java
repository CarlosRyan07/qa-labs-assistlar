package br.com.ryanqalabs.assistlar.plano.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.ryanqalabs.assistlar.compartilhado.erro.RecursoNaoEncontradoException;
import br.com.ryanqalabs.assistlar.compartilhado.configuracao.TempoConfiguracao;
import br.com.ryanqalabs.assistlar.plano.aplicacao.PlanoAssistenciaService;
import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;

@WebMvcTest(PlanoAssistenciaController.class)
@Import(TempoConfiguracao.class)
class PlanoAssistenciaControllerTest {

    private static final UUID PLANO_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanoAssistenciaService service;

    @Test
    void deveListarPlanosAtivos() throws Exception {
        when(service.listarAtivos()).thenReturn(List.of(planoEssencial()));

        mockMvc.perform(get("/api/planos").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("ESSENCIAL"))
                .andExpect(jsonPath("$[0].coberturas.length()").value(2));
    }

    @Test
    void deveBuscarPlanoPorId() throws Exception {
        when(service.buscarAtivo(PLANO_ID)).thenReturn(planoEssencial());

        mockMvc.perform(get("/api/planos/{id}", PLANO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PLANO_ID.toString()))
                .andExpect(jsonPath("$.coberturas[0].tipoAssistencia").value("ELETRICISTA"));
    }

    @Test
    void deveRetornarNotFoundParaPlanoInexistente() throws Exception {
        when(service.buscarAtivo(PLANO_ID)).thenThrow(new RecursoNaoEncontradoException(
                "plano-nao-encontrado", "Plano de assistencia nao encontrado."));

        mockMvc.perform(get("/api/planos/{id}", PLANO_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/erros/plano-nao-encontrado"));
    }

    @Test
    void deveRetornarBadRequestParaUuidInvalido() throws Exception {
        mockMvc.perform(get("/api/planos/uuid-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/requisicao-invalida"));
    }

    private PlanoResposta planoEssencial() {
        return new PlanoResposta(PLANO_ID, "ESSENCIAL", "Plano Essencial", "Descricao",
                List.of(
                        new CoberturaResposta(TipoAssistencia.ELETRICISTA, 1),
                        new CoberturaResposta(TipoAssistencia.ENCANADOR, 1)));
    }
}
