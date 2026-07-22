package br.com.ryanqalabs.assistlar.compartilhado.erro;

public class ExcecaoDadosInvalidos extends RuntimeException {

    private final String codigo;

    public ExcecaoDadosInvalidos(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
