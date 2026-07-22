package br.com.ryanqalabs.assistlar.solicitacao;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SolicitacaoApiIT extends PostgreSqlTestContainer {

    private static final UUID PLANO_ESSENCIAL = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID PLANO_COMPLETO = UUID.fromString("10000000-0000-0000-0000-000000000002");

    @LocalServerPort
    private int porta;

    @BeforeEach
    void configurarRestAssured() {
        RestAssured.port = porta;
        RestAssured.basePath = "/api";
    }

    @Test
    void deveAbrirIniciarConcluirERegistrarHistorico() {
        String contratacaoId = criarContratacao(true);
        String solicitacaoId = abrirSolicitacao(contratacaoId, "ELETRICISTA");

        given().when().get("/solicitacoes-assistencia/{id}", solicitacaoId)
                .then().statusCode(200).body("status", equalTo("ABERTA"));
        given().when().post("/solicitacoes-assistencia/{id}/inicio", solicitacaoId)
                .then().statusCode(200).body("status", equalTo("EM_ATENDIMENTO"));
        given().when().post("/solicitacoes-assistencia/{id}/conclusao", solicitacaoId)
                .then().statusCode(200).body("status", equalTo("CONCLUIDA"));

        given().when().get("/solicitacoes-assistencia/{id}/historico", solicitacaoId)
                .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("find { it.statusNovo == 'ABERTA' }.statusNovo", equalTo("ABERTA"))
                .body("find { it.statusNovo == 'ABERTA' }.tipoResponsavel", equalTo("CLIENTE"))
                .body("find { it.statusNovo == 'EM_ATENDIMENTO' }.statusNovo", equalTo("EM_ATENDIMENTO"))
                .body("find { it.statusNovo == 'EM_ATENDIMENTO' }.tipoResponsavel", equalTo("OPERADOR"))
                .body("find { it.statusNovo == 'CONCLUIDA' }.statusNovo", equalTo("CONCLUIDA"));
    }

    @Test
    void deveExigirMotivoAoCancelarEmAtendimentoEManterEstadoAposRejeicao() {
        String solicitacaoId = abrirSolicitacao(criarContratacao(true), "ENCANADOR");
        given().when().post("/solicitacoes-assistencia/{id}/inicio", solicitacaoId).then().statusCode(200);

        given().contentType(ContentType.JSON).body("{}")
                .when().post("/solicitacoes-assistencia/{id}/cancelamento", solicitacaoId)
                .then().statusCode(422).body("type", equalTo("/erros/motivo-cancelamento-obrigatorio"));
        given().when().get("/solicitacoes-assistencia/{id}", solicitacaoId)
                .then().statusCode(200).body("status", equalTo("EM_ATENDIMENTO"));

        given().contentType(ContentType.JSON).body("{\"motivo\":\"  Risco eliminado  \"}")
                .when().post("/solicitacoes-assistencia/{id}/cancelamento", solicitacaoId)
                .then()
                .statusCode(200)
                .body("status", equalTo("CANCELADA"))
                .body("motivoCancelamento", equalTo("Risco eliminado"));
        given().when().get("/solicitacoes-assistencia/{id}/historico", solicitacaoId)
                .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("find { it.statusNovo == 'CANCELADA' }.tipoResponsavel", equalTo("OPERADOR"));
    }

    @Test
    void deveCancelarAbertaSemMotivoELiberarNovaSolicitacaoDoMesmoTipo() {
        String contratacaoId = criarContratacao(true);
        String primeira = abrirSolicitacao(contratacaoId, "ELETRICISTA");

        given().contentType(ContentType.JSON).body("{}")
                .when().post("/solicitacoes-assistencia/{id}/cancelamento", primeira)
                .then().statusCode(200).body("status", equalTo("CANCELADA"));

        String segunda = abrirSolicitacao(contratacaoId, "ELETRICISTA");
        assertThat(segunda).isNotEqualTo(primeira);
    }

    @Test
    void deveValidarContratacaoCoberturaEDuplicidadeEmAndamento() {
        String contratacaoPendente = criarContratacao(false);
        given().contentType(ContentType.JSON).body(requisicaoSolicitacao(contratacaoPendente, "ELETRICISTA"))
                .when().post("/solicitacoes-assistencia")
                .then().statusCode(422).body("type", equalTo("/erros/contratacao-nao-ativa"));

        String contratacaoAtiva = criarContratacao(true);
        given().contentType(ContentType.JSON).body(requisicaoSolicitacao(contratacaoAtiva, "CHAVEIRO"))
                .when().post("/solicitacoes-assistencia")
                .then().statusCode(422).body("type", equalTo("/erros/servico-nao-coberto"));

        abrirSolicitacao(contratacaoAtiva, "ENCANADOR");
        given().contentType(ContentType.JSON).body(requisicaoSolicitacao(contratacaoAtiva, "ENCANADOR"))
                .when().post("/solicitacoes-assistencia")
                .then().statusCode(409).body("type", equalTo("/erros/solicitacao-em-andamento-existente"));
    }

    @Test
    void deveAplicarLimitesDiferentesDosPlanos() {
        String essencial = criarContratacao(true, PLANO_ESSENCIAL);
        concluirSolicitacao(abrirSolicitacao(essencial, "ELETRICISTA"));
        given().contentType(ContentType.JSON).body(requisicaoSolicitacao(essencial, "ELETRICISTA"))
                .when().post("/solicitacoes-assistencia")
                .then().statusCode(422).body("type", equalTo("/erros/limite-esgotado"));

        String completo = criarContratacao(true, PLANO_COMPLETO);
        concluirSolicitacao(abrirSolicitacao(completo, "ELETRICISTA"));
        concluirSolicitacao(abrirSolicitacao(completo, "ELETRICISTA"));
        given().contentType(ContentType.JSON).body(requisicaoSolicitacao(completo, "ELETRICISTA"))
                .when().post("/solicitacoes-assistencia")
                .then().statusCode(422).body("type", equalTo("/erros/limite-esgotado"));
    }

    private String criarContratacao(boolean ativar) {
        return criarContratacao(ativar, PLANO_ESSENCIAL);
    }

    private String criarContratacao(boolean ativar, UUID planoId) {
        String clienteId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nome":"Cliente Solicitacao","email":"%s@exemplo.com","dataNascimento":"%s"}
                        """.formatted(UUID.randomUUID(), LocalDate.of(1990, 1, 1)))
                .when().post("/clientes")
                .then().statusCode(201).extract().path("id");
        String contratacaoId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"clienteId":"%s","planoId":"%s"}
                        """.formatted(clienteId, planoId))
                .when().post("/contratacoes")
                .then().statusCode(201).extract().path("id");
        if (ativar) {
            given().when().post("/contratacoes/{id}/ativacao", contratacaoId).then().statusCode(200);
        }
        return contratacaoId;
    }

    private void concluirSolicitacao(String solicitacaoId) {
        given().when().post("/solicitacoes-assistencia/{id}/inicio", solicitacaoId).then().statusCode(200);
        given().when().post("/solicitacoes-assistencia/{id}/conclusao", solicitacaoId).then().statusCode(200);
    }

    private String abrirSolicitacao(String contratacaoId, String tipoAssistencia) {
        return given()
                .contentType(ContentType.JSON)
                .body(requisicaoSolicitacao(contratacaoId, tipoAssistencia))
                .when().post("/solicitacoes-assistencia")
                .then().statusCode(201).body("status", equalTo("ABERTA"))
                .extract().path("id");
    }

    private String requisicaoSolicitacao(String contratacaoId, String tipoAssistencia) {
        return """
                {"contratacaoId":"%s","tipoAssistencia":"%s","descricaoProblema":"Problema residencial"}
                """.formatted(contratacaoId, tipoAssistencia);
    }
}
