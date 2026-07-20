package br.com.ryanqalabs.assistlar.compartilhado.erro;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ManipuladorGlobalExcecoes {

    private final Clock relogio;

    public ManipuladorGlobalExcecoes(Clock relogio) {
        this.relogio = relogio;
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ProblemDetail> recursoNaoEncontrado(
            RecursoNaoEncontradoException excecao, HttpServletRequest requisicao) {
        return resposta(HttpStatus.NOT_FOUND, excecao.getCodigo(), "Recurso nao encontrado",
                excecao.getMessage(), requisicao, List.of());
    }

    @ExceptionHandler(ExcecaoConflito.class)
    ResponseEntity<ProblemDetail> conflito(ExcecaoConflito excecao, HttpServletRequest requisicao) {
        return resposta(HttpStatus.CONFLICT, excecao.getCodigo(), "Conflito na operacao",
                excecao.getMessage(), requisicao, List.of());
    }

    @ExceptionHandler(ExcecaoRegraNegocio.class)
    ResponseEntity<ProblemDetail> regraNegocio(ExcecaoRegraNegocio excecao, HttpServletRequest requisicao) {
        return resposta(HttpStatus.UNPROCESSABLE_CONTENT, excecao.getCodigo(), "Regra de negocio nao atendida",
                excecao.getMessage(), requisicao, List.of());
    }

    @ExceptionHandler(ExcecaoDadosInvalidos.class)
    ResponseEntity<ProblemDetail> dadosInvalidos(ExcecaoDadosInvalidos excecao, HttpServletRequest requisicao) {
        return resposta(HttpStatus.BAD_REQUEST, excecao.getCodigo(), "Dados invalidos",
                excecao.getMessage(), requisicao, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validacao(MethodArgumentNotValidException excecao, HttpServletRequest requisicao) {
        List<ErroValidacao> erros = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroValidacao(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados invalidos",
                "Um ou mais campos estao invalidos.", requisicao, erros);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<ProblemDetail> requisicaoInvalida(Exception excecao, HttpServletRequest requisicao) {
        return resposta(HttpStatus.BAD_REQUEST, "requisicao-invalida", "Requisicao invalida",
                "O formato ou um parametro da requisicao e invalido.", requisicao, List.of());
    }

    private ResponseEntity<ProblemDetail> resposta(HttpStatus status, String codigo, String titulo, String detalhe,
            HttpServletRequest requisicao, List<?> erros) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setType(URI.create("/erros/" + codigo));
        problema.setTitle(titulo);
        problema.setInstance(URI.create(requisicao.getRequestURI()));
        problema.setProperty("timestamp", Instant.now(relogio));
        problema.setProperty("erros", erros);
        return ResponseEntity.status(status).body(problema);
    }
}
