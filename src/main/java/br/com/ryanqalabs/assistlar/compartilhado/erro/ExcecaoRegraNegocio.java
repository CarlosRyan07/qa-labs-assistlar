package br.com.ryanqalabs.assistlar.compartilhado.erro;

public class ExcecaoRegraNegocio extends RuntimeException {

    private final String codigo;

    public ExcecaoRegraNegocio(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
