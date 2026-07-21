package br.com.ryanqalabs.assistlar.elegibilidade;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.ryanqalabs.assistlar.compartilhado.configuracao.TempoConfiguracao;
import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ElegibilidadeApiIT extends PostgreSqlTestContainer {

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
    void deveInformarElegibilidadeSemPersistirResultado() {
        String clienteId = cadastrarCliente("Adulto Elegivel", LocalDate.of(1990, 1, 1));
        Integer contratacoesAntes = quantidade("contratacao");
        Integer historicosAntes = quantidade("historico_status");

        given()
                .queryParam("clienteId", clienteId)
                .queryParam("planoId", PLANO_ESSENCIAL)
        .when()
                .get("/elegibilidades")
        .then()
                .statusCode(200)
                .body("elegivel", equalTo(true))
                .body("motivos", empty());

        assertThat(quantidade("contratacao")).isEqualTo(contratacoesAntes);
        assertThat(quantidade("historico_status")).isEqualTo(historicosAntes);
    }

    @Test
    void deveManterProtecaoParaClienteMenorRegistradoDiretamenteNoBanco() {
        UUID clienteId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO cliente
                    (id, nome, email, data_nascimento, status, criado_em, atualizado_em)
                VALUES (?, ?, ?, ?, 'ATIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, clienteId, "Menor Legado", clienteId + "@exemplo.com",
                LocalDate.now(TempoConfiguracao.FUSO_NEGOCIO).minusYears(16));

        given().when().post("/clientes/{id}/inativacao", clienteId).then().statusCode(200);

        given()
                .queryParam("clienteId", clienteId)
                .queryParam("planoId", PLANO_ESSENCIAL)
        .when()
                .get("/elegibilidades")
        .then()
                .statusCode(200)
                .body("elegivel", equalTo(false))
                .body("motivos", containsInAnyOrder("CLIENTE_INATIVO", "CLIENTE_MENOR_DE_IDADE"));
    }

    @Test
    void deveRetornarNotFoundParaRecursoInexistente() {
        given()
                .queryParam("clienteId", UUID.randomUUID())
                .queryParam("planoId", PLANO_ESSENCIAL)
        .when()
                .get("/elegibilidades")
        .then()
                .statusCode(404)
                .body("type", equalTo("/erros/cliente-nao-encontrado"));
    }

    private String cadastrarCliente(String nome, LocalDate dataNascimento) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"%s","email":"%s@exemplo.com","dataNascimento":"%s"}
                        """.formatted(nome, UUID.randomUUID(), dataNascimento))
        .when()
                .post("/clientes")
        .then()
                .statusCode(201)
                .extract().path("id");
    }

    private Integer quantidade(String tabela) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tabela, Integer.class);
    }
}
