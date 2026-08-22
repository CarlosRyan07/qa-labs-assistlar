package br.com.ryanqalabs.assistlar.compartilhado.api;

import java.util.List;

public record PaginaResposta<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {
}
