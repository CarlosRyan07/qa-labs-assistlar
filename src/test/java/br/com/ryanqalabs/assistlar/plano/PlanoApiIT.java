package br.com.ryanqalabs.assistlar.plano;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import br.com.ryanqalabs.assistlar.plano.dominio.PlanoAssistencia;
import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;
import br.com.ryanqalabs.assistlar.plano.infraestrutura.PlanoAssistenciaRepository;
import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlanoApiIT extends PostgreSqlTestContainer {

    private static final UUID ID_ESSENCIAL = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @LocalServerPort
    private int porta;

    @Autowired
    private PlanoAssistenciaRepository repository;

    @BeforeEach
    void configurarRestAssured() {
        RestAssured.port = porta;
        RestAssured.basePath = "/api";
    }

    @Test
    void deveListarOsDoisPlanosComCoberturasCorretas() {
        given()
                .accept("application/json")
        .when()
                .get("/planos")
        .then()
                .statusCode(200)
                .body("codigo", containsInAnyOrder("ESSENCIAL", "COMPLETO"))
                .body("find { it.codigo == 'ESSENCIAL' }.coberturas", hasSize(2))
                .body("find { it.codigo == 'COMPLETO' }.coberturas", hasSize(3));
    }

    @Test
    void deveManterChaveiroForaDoPlanoEssencial() {
        given()
        .when()
                .get("/planos/10000000-0000-0000-0000-000000000001")
        .then()
                .statusCode(200)
                .body("codigo", equalTo("ESSENCIAL"))
                .body("coberturas.tipoAssistencia", containsInAnyOrder("ELETRICISTA", "ENCANADOR"));
    }

    @Test
    void deveDistinguirCoberturaExistenteDeCoberturaAusente() {
        PlanoAssistencia essencial = repository.findByIdAndAtivoTrue(ID_ESSENCIAL).orElseThrow();

        assertThat(essencial.cobre(TipoAssistencia.ELETRICISTA)).isTrue();
        assertThat(essencial.limitePara(TipoAssistencia.ELETRICISTA)).isEqualTo(1);
        assertThat(essencial.cobre(TipoAssistencia.CHAVEIRO)).isFalse();
        assertThat(essencial.limitePara(TipoAssistencia.CHAVEIRO)).isZero();
    }
}
