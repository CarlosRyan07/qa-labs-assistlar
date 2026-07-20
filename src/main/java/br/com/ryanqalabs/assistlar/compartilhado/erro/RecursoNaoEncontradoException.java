package br.com.ryanqalabs.assistlar.compartilhado.erro;

public class RecursoNaoEncontradoException extends RuntimeException {

    private final String codigo;

    public RecursoNaoEncontradoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
