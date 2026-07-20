package br.com.ryanqalabs.assistlar.contratacao.api;

import jakarta.validation.constraints.Size;

public record ContratacaoCancelamentoRequisicao(
        @Size(max = 500, message = "O motivo deve ter no maximo 500 caracteres.") String motivo) {
}
