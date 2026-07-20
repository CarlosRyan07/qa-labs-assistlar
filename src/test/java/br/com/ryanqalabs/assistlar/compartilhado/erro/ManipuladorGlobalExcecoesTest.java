package br.com.ryanqalabs.assistlar.compartilhado.erro;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ManipuladorGlobalExcecoesTest {

    private static final Instant AGORA = Instant.parse("2026-07-20T18:00:00Z");

    private ManipuladorGlobalExcecoes manipulador;
    private MockHttpServletRequest requisicao;

    @BeforeEach
    void configurar() {
        Clock relogio = Clock.fixed(AGORA, ZoneId.of("America/Sao_Paulo"));
        manipulador = new ManipuladorGlobalExcecoes(relogio);
        requisicao = new MockHttpServletRequest("GET", "/api/recurso");
    }

    @Test
    void deveRetornarNotFoundSemExporDetalhesInternos() {
        ResponseEntity<ProblemDetail> resposta = manipulador.recursoNaoEncontrado(
                new RecursoNaoEncontradoException("cliente-nao-encontrado", "Cliente nao encontrado."), requisicao);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().getType()).hasToString("/erros/cliente-nao-encontrado");
        assertThat(resposta.getBody().getInstance()).hasToString("/api/recurso");
        assertThat(resposta.getBody().getProperties()).containsEntry("timestamp", AGORA);
    }

    @Test
    void deveRetornarConflictParaConflito() {
        ResponseEntity<ProblemDetail> resposta = manipulador.conflito(
                new ExcecaoConflito("estado-invalido", "A transicao solicitada nao e permitida."), requisicao);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().getTitle()).isEqualTo("Conflito na operacao");
        assertThat(resposta.getBody().getDetail()).isEqualTo("A transicao solicitada nao e permitida.");
    }

    @Test
    void deveRetornarUnprocessableContentParaRegraDeNegocio() {
        ResponseEntity<ProblemDetail> resposta = manipulador.regraNegocio(
                new ExcecaoRegraNegocio("cliente-inelegivel", "O cliente nao esta elegivel."), requisicao);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getBody().getType()).hasToString("/erros/cliente-inelegivel");
    }

    @Test
    void deveListarErrosDeValidacaoPorCampo() {
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(new Object(), "requisicao");
        resultado.addError(new FieldError("requisicao", "nome", "deve ser informado"));
        MethodArgumentNotValidException excecao = new MethodArgumentNotValidException(null, resultado);

        ResponseEntity<ProblemDetail> resposta = manipulador.validacao(excecao, requisicao);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().getProperties().get("erros"))
                .asList()
                .containsExactly(new ErroValidacao("nome", "deve ser informado"));
    }

    @Test
    void deveRetornarBadRequestParaCorpoIlegivel() {
        ResponseEntity<ProblemDetail> resposta = manipulador.requisicaoInvalida(
                new HttpMessageNotReadableException("JSON invalido", new MockHttpInputMessage(new byte[0])),
                requisicao);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().getType()).hasToString("/erros/requisicao-invalida");
    }
}
