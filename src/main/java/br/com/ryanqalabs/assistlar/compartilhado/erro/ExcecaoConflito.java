package br.com.ryanqalabs.assistlar.compartilhado.erro;

public class ExcecaoConflito extends RuntimeException {

    private final String codigo;

    public ExcecaoConflito(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
