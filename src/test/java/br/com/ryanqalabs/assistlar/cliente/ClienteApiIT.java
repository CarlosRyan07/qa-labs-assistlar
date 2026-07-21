package br.com.ryanqalabs.assistlar.cliente;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import br.com.ryanqalabs.assistlar.compartilhado.configuracao.TempoConfiguracao;
import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteApiIT extends PostgreSqlTestContainer {

    @LocalServerPort
    private int porta;

    @BeforeEach
    void configurarRestAssured() {
        RestAssured.port = porta;
        RestAssured.basePath = "/api";
    }

    @Test
    void deveExecutarJornadaDeCadastroConsultaInativacaoEReativacao() {
        String email = "jornada-" + UUID.randomUUID() + "@exemplo.com";

        String clienteId = given()
                .contentType(ContentType.JSON)
                .body(requisicao("Ana Silva", email, LocalDate.of(1990, 5, 10)))
        .when()
                .post("/clientes")
        .then()
                .statusCode(201)
                .header("Location", matchesPattern(".*/api/clientes/[0-9a-f-]{36}"))
                .body("email", equalTo(email))
                .body("status", equalTo("ATIVO"))
                .extract().path("id");

        given()
        .when()
                .get("/clientes/{id}", clienteId)
        .then()
                .statusCode(200)
                .body("id", equalTo(clienteId));

        given()
        .when()
                .post("/clientes/{id}/inativacao", clienteId)
        .then()
                .statusCode(200)
                .body("status", equalTo("INATIVO"));

        given()
        .when()
                .post("/clientes/{id}/reativacao", clienteId)
        .then()
                .statusCode(200)
                .body("status", equalTo("ATIVO"));
    }

    @Test
    void deveImpedirEmailDuplicadoIgnorandoMaiusculas() {
        String email = "duplicado-" + UUID.randomUUID() + "@exemplo.com";
        String corpo = requisicao("Primeiro Cliente", email, LocalDate.of(1990, 1, 1));

        given().contentType(ContentType.JSON).body(corpo)
                .when().post("/clientes")
                .then().statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(requisicao("Segundo Cliente", email.toUpperCase(), LocalDate.of(1992, 2, 2)))
        .when()
                .post("/clientes")
        .then()
                .statusCode(409)
                .contentType(startsWith("application/problem+json"))
                .body("type", equalTo("/erros/email-ja-cadastrado"));
    }

    @Test
    void deveAceitarExatamenteCentoEVinteAnosERejeitarUmDiaAlemDoLimite() {
        LocalDate hoje = LocalDate.now(TempoConfiguracao.FUSO_NEGOCIO);
        LocalDate exatamenteCentoEVinte = hoje.minusYears(120);
        LocalDate acimaDoLimite = exatamenteCentoEVinte.minusDays(1);

        given()
                .contentType(ContentType.JSON)
                .body(requisicao("Cliente Centenario", "limite-" + UUID.randomUUID() + "@exemplo.com",
                        exatamenteCentoEVinte))
        .when()
                .post("/clientes")
        .then()
                .statusCode(201)
                .body("dataNascimento", equalTo(exatamenteCentoEVinte.toString()));

        given()
                .contentType(ContentType.JSON)
                .body(requisicao("Cliente Antigo", "antigo-" + UUID.randomUUID() + "@exemplo.com", acimaDoLimite))
        .when()
                .post("/clientes")
        .then()
                .statusCode(400)
                .body("type", equalTo("/erros/idade-maxima-excedida"));
    }

    @Test
    void deveAceitarExatamenteDezoitoAnosERejeitarMenores() {
        LocalDate hoje = LocalDate.now(TempoConfiguracao.FUSO_NEGOCIO);
        LocalDate exatamenteDezoito = hoje.minusYears(18);
        LocalDate aindaMenor = exatamenteDezoito.plusDays(1);

        given()
                .contentType(ContentType.JSON)
                .body(requisicao("Cliente Adulto", "adulto-" + UUID.randomUUID() + "@exemplo.com",
                        exatamenteDezoito))
        .when()
                .post("/clientes")
        .then()
                .statusCode(201)
                .body("dataNascimento", equalTo(exatamenteDezoito.toString()));

        given()
                .contentType(ContentType.JSON)
                .body(requisicao("Cliente Menor", "menor-" + UUID.randomUUID() + "@exemplo.com", aindaMenor))
        .when()
                .post("/clientes")
        .then()
                .statusCode(422)
                .body("type", equalTo("/erros/idade-minima-nao-atendida"))
                .body("detail", equalTo("O cliente deve ter pelo menos 18 anos."));

        given()
                .contentType(ContentType.JSON)
                .body(requisicao("Cliente Hoje", "hoje-" + UUID.randomUUID() + "@exemplo.com", hoje))
        .when()
                .post("/clientes")
        .then()
                .statusCode(422)
                .body("type", equalTo("/erros/idade-minima-nao-atendida"));
    }

    @Test
    void deveRejeitarNomeComMenosDeTresCaracteres() {
        given()
                .contentType(ContentType.JSON)
                .body(requisicao("AB", "nome-" + UUID.randomUUID() + "@exemplo.com", LocalDate.of(1990, 1, 1)))
        .when()
                .post("/clientes")
        .then()
                .statusCode(400)
                .body("type", equalTo("/erros/dados-invalidos"))
                .body("erros[0].campo", equalTo("nome"))
                .body("erros[0].mensagem", equalTo("O nome deve ter entre 3 e 120 caracteres."));
    }

    @Test
    void deveRejeitarDataFutura() {
        LocalDate amanha = LocalDate.now(TempoConfiguracao.FUSO_NEGOCIO).plusDays(1);

        given()
                .contentType(ContentType.JSON)
                .body(requisicao("Cliente Futuro", "futuro-" + UUID.randomUUID() + "@exemplo.com", amanha))
        .when()
                .post("/clientes")
        .then()
                .statusCode(400)
                .body("type", equalTo("/erros/data-nascimento-futura"));
    }

    private String requisicao(String nome, String email, LocalDate dataNascimento) {
        return """
                {"nome":"%s","email":"%s","dataNascimento":"%s"}
                """.formatted(nome, email, dataNascimento);
    }
}
