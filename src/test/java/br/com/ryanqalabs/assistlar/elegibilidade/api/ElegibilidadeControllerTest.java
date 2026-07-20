package br.com.ryanqalabs.assistlar.elegibilidade.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.ryanqalabs.assistlar.compartilhado.configuracao.TempoConfiguracao;
import br.com.ryanqalabs.assistlar.elegibilidade.aplicacao.ElegibilidadeService;
import br.com.ryanqalabs.assistlar.elegibilidade.dominio.MotivoInelegibilidade;

@WebMvcTest(ElegibilidadeController.class)
@Import(TempoConfiguracao.class)
class ElegibilidadeControllerTest {

    private static final UUID CLIENTE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID PLANO_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ElegibilidadeService service;

    @Test
    void deveRetornarOkQuandoElegivel() throws Exception {
        when(service.consultar(CLIENTE_ID, PLANO_ID)).thenReturn(
                new ElegibilidadeResposta(CLIENTE_ID, PLANO_ID, true, List.of()));

        mockMvc.perform(get("/api/elegibilidades")
                        .param("clienteId", CLIENTE_ID.toString())
                        .param("planoId", PLANO_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elegivel").value(true))
                .andExpect(jsonPath("$.motivos.length()").value(0));
    }

    @Test
    void deveRetornarOkQuandoInelegivel() throws Exception {
        when(service.consultar(CLIENTE_ID, PLANO_ID)).thenReturn(new ElegibilidadeResposta(
                CLIENTE_ID, PLANO_ID, false, List.of(MotivoInelegibilidade.CLIENTE_INATIVO)));

        mockMvc.perform(get("/api/elegibilidades")
                        .param("clienteId", CLIENTE_ID.toString())
                        .param("planoId", PLANO_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elegivel").value(false))
                .andExpect(jsonPath("$.motivos[0]").value("CLIENTE_INATIVO"));
    }

    @Test
    void deveRejeitarParametroAusenteOuInvalido() throws Exception {
        mockMvc.perform(get("/api/elegibilidades").param("clienteId", "uuid-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/requisicao-invalida"));
    }
}
