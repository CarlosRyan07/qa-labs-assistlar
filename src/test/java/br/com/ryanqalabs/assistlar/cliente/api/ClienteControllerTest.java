package br.com.ryanqalabs.assistlar.cliente.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.ryanqalabs.assistlar.cliente.aplicacao.ClienteService;
import br.com.ryanqalabs.assistlar.cliente.dominio.StatusCliente;
import br.com.ryanqalabs.assistlar.compartilhado.configuracao.TempoConfiguracao;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;

@WebMvcTest(ClienteController.class)
@Import(TempoConfiguracao.class)
class ClienteControllerTest {

    private static final UUID CLIENTE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant INSTANTE = Instant.parse("2026-07-20T15:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @Test
    void deveCadastrarComLocation() throws Exception {
        when(service.cadastrar(any())).thenReturn(resposta(StatusCliente.ATIVO));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "email": "ana@exemplo.com",
                                  "dataNascimento": "1990-05-10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/clientes/" + CLIENTE_ID))
                .andExpect(jsonPath("$.id").value(CLIENTE_ID.toString()))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void deveRejeitarPayloadInvalido() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"", "email":"invalido"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/dados-invalidos"))
                .andExpect(jsonPath("$.erros.length()").value(3));
    }

    @Test
    void deveConsultarEAlterarStatus() throws Exception {
        when(service.buscar(CLIENTE_ID)).thenReturn(resposta(StatusCliente.ATIVO));
        when(service.inativar(CLIENTE_ID)).thenReturn(resposta(StatusCliente.INATIVO));
        when(service.reativar(CLIENTE_ID)).thenReturn(resposta(StatusCliente.ATIVO));

        mockMvc.perform(get("/api/clientes/{id}", CLIENTE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"));
        mockMvc.perform(post("/api/clientes/{id}/inativacao", CLIENTE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
        mockMvc.perform(post("/api/clientes/{id}/reativacao", CLIENTE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void deveRetornarConflitoDeEmailSemDetalhesInternos() throws Exception {
        when(service.cadastrar(any())).thenThrow(new ExcecaoConflito(
                "email-ja-cadastrado", "Ja existe um cliente cadastrado com este e-mail."));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "email": "ana@exemplo.com",
                                  "dataNascimento": "1990-05-10"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/erros/email-ja-cadastrado"))
                .andExpect(jsonPath("$.detail").value("Ja existe um cliente cadastrado com este e-mail."))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void deveRejeitarUuidInvalido() throws Exception {
        mockMvc.perform(get("/api/clientes/uuid-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/erros/requisicao-invalida"));
    }

    private ClienteResposta resposta(StatusCliente status) {
        return new ClienteResposta(CLIENTE_ID, "Ana Silva", "ana@exemplo.com",
                LocalDate.of(1990, 5, 10), status, INSTANTE, INSTANTE);
    }
}
