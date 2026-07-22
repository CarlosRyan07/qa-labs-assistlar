package br.com.ryanqalabs.assistlar.contratacao;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;
import br.com.ryanqalabs.assistlar.suporte.TempoFixoTestesConfiguracao;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContratacaoApiIT extends PostgreSqlTestContainer {

    private static final UUID PLANO_ESSENCIAL = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void configurarRestAssured() {
        RestAssured.port = porta;
        RestAssured.basePath = "/api";
    }

    @Test
    void deveExecutarJornadaComHistoricoEPermitirNovaContratacaoAposCancelamento() {
        String clienteId = cadastrarClienteAdulto();
        String contratacaoId = criarContratacao(clienteId, PLANO_ESSENCIAL);

        given().when().get("/contratacoes/{id}", contratacaoId)
                .then().statusCode(200).body("status", equalTo("PENDENTE"));

        given().when().post("/contratacoes/{id}/ativacao", contratacaoId)
                .then().statusCode(200).body("status", equalTo("ATIVA"));

        given().contentType(ContentType.JSON).body("{\"motivo\":\"  Solicitado pelo cliente  \"}")
                .when().post("/contratacoes/{id}/cancelamento", contratacaoId)
                .then().statusCode(200).body("status", equalTo("CANCELADA"));

        given().when().get("/contratacoes/{id}/historico", contratacaoId)
                .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("find { it.statusNovo == 'PENDENTE' }.statusAnterior", equalTo(null))
                .body("find { it.statusNovo == 'PENDENTE' }.statusNovo", equalTo("PENDENTE"))
                .body("find { it.statusNovo == 'PENDENTE' }.tipoResponsavel", equalTo("CLIENTE"))
                .body("find { it.statusNovo == 'ATIVA' }.statusNovo", equalTo("ATIVA"))
                .body("find { it.statusNovo == 'ATIVA' }.tipoResponsavel", equalTo("OPERADOR"))
                .body("find { it.statusNovo == 'CANCELADA' }.statusNovo", equalTo("CANCELADA"))
                .body("find { it.statusNovo == 'CANCELADA' }.motivo", equalTo("Solicitado pelo cliente"));

        String novaContratacaoId = criarContratacao(clienteId, PLANO_ESSENCIAL);
        assertThat(novaContratacaoId).isNotEqualTo(contratacaoId);
    }

    @Test
    void deveRejeitarContratacaoDeClienteMenorPresenteEmDadoLegado() {
        UUID clienteId = inserirClienteMenor();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"clienteId":"%s","planoId":"%s"}
                        """.formatted(clienteId, PLANO_ESSENCIAL))
        .when()
                .post("/contratacoes")
        .then()
                .statusCode(422)
                .body("type", equalTo("/erros/cliente-inelegivel"));
    }

    @Test
    void deveImpedirCancelamentoComSolicitacaoEmAndamento() {
        String clienteId = cadastrarClienteAdulto();
        String contratacaoId = criarContratacao(clienteId, PLANO_ESSENCIAL);
        given().when().post("/contratacoes/{id}/ativacao", contratacaoId).then().statusCode(200);
        inserirSolicitacaoAberta(UUID.fromString(contratacaoId));

        given()
                .contentType(ContentType.JSON)
                .body("{\"motivo\":\"Cancelamento solicitado\"}")
        .when()
                .post("/contratacoes/{id}/cancelamento", contratacaoId)
        .then()
                .statusCode(422)
                .body("type", equalTo("/erros/contratacao-com-solicitacao-em-andamento"));

        given().when().get("/contratacoes/{id}", contratacaoId)
                .then().statusCode(200).body("status", equalTo("ATIVA"));
        given().when().get("/contratacoes/{id}/historico", contratacaoId)
                .then().statusCode(200).body("$", hasSize(2));
    }

    private String cadastrarClienteAdulto() {
        return cadastrarCliente(LocalDate.of(1990, 1, 1));
    }

    private String cadastrarCliente(LocalDate nascimento) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"Cliente Contratacao","email":"%s@exemplo.com","dataNascimento":"%s"}
                        """.formatted(UUID.randomUUID(), nascimento))
        .when()
                .post("/clientes")
        .then()
                .statusCode(201)
                .extract().path("id");
    }

    private String criarContratacao(String clienteId, UUID planoId) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"clienteId":"%s","planoId":"%s"}
                        """.formatted(clienteId, planoId))
        .when()
                .post("/contratacoes")
        .then()
                .statusCode(201)
                .body("status", equalTo("PENDENTE"))
                .extract().path("id");
    }

    private UUID inserirClienteMenor() {
        UUID clienteId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO cliente
                    (id, nome, email, data_nascimento, status, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 'ATIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, clienteId, "Cliente Menor Legado", clienteId + "@exemplo.com",
                TempoFixoTestesConfiguracao.DATA_CIVIL_FIXA.minusYears(16));
        return clienteId;
    }

    private void inserirSolicitacaoAberta(UUID contratacaoId) {
        jdbcTemplate.update("""
                INSERT INTO solicitacao_assistencia (
                    id, contratacao_id, tipo_assistencia, descricao_problema, status, aberta_em, versao
                ) VALUES (?, ?, 'ELETRICISTA', 'Tomada sem energia', 'ABERTA', ?, 0)
                """, UUID.randomUUID(), contratacaoId,
                TempoFixoTestesConfiguracao.INSTANTE_FIXO.atOffset(ZoneOffset.UTC));
    }
}
