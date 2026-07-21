package br.com.ryanqalabs.assistlar;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiApiIT extends PostgreSqlTestContainer {

    @LocalServerPort
    private int porta;

    @BeforeEach
    void configurarRestAssured() {
        RestAssured.port = porta;
        RestAssured.basePath = "";
    }

    @Test
    void devePublicarContratoOpenApiDoMvpSemResponsavelNosPayloads() {
        Response resposta = given().accept("application/json")
                .when().get("/v3/api-docs")
                .then().statusCode(200).extract().response();

        Map<String, Object> caminhos = resposta.jsonPath().getMap("paths");
        assertThat(caminhos).containsKeys(
                "/api/clientes",
                "/api/clientes/{id}",
                "/api/clientes/{id}/inativacao",
                "/api/clientes/{id}/reativacao",
                "/api/planos",
                "/api/planos/{id}",
                "/api/elegibilidades",
                "/api/contratacoes",
                "/api/contratacoes/{id}",
                "/api/contratacoes/{id}/ativacao",
                "/api/contratacoes/{id}/cancelamento",
                "/api/contratacoes/{id}/historico",
                "/api/solicitacoes-assistencia",
                "/api/solicitacoes-assistencia/{id}",
                "/api/solicitacoes-assistencia/{id}/inicio",
                "/api/solicitacoes-assistencia/{id}/conclusao",
                "/api/solicitacoes-assistencia/{id}/cancelamento",
                "/api/solicitacoes-assistencia/{id}/historico");

        Map<String, Object> schemas = resposta.jsonPath().getMap("components.schemas");
        assertThat(propriedades(schemas, "ContratacaoCancelamentoRequisicao"))
                .doesNotContainKey("tipoResponsavel");
        assertThat(propriedades(schemas, "SolicitacaoCancelamentoRequisicao"))
                .doesNotContainKey("tipoResponsavel");
    }

    @Test
    void deveExporSomenteHealthNoActuator() {
        given().when().get("/actuator/health").then().statusCode(200);
        given().when().get("/actuator/info").then().statusCode(404);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> propriedades(Map<String, Object> schemas, String schema) {
        return (Map<String, Object>) ((Map<String, Object>) schemas.get(schema)).get("properties");
    }
}
