package br.com.ryanqalabs.assistlar.solicitacao.api;

import jakarta.validation.constraints.Size;

public record SolicitacaoCancelamentoRequisicao(
        @Size(max = 500, message = "O motivo deve ter no maximo 500 caracteres.") String motivo) {
}
