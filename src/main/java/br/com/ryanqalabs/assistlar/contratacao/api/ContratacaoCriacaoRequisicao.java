package br.com.ryanqalabs.assistlar.contratacao.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ContratacaoCriacaoRequisicao(
        @NotNull(message = "O cliente e obrigatorio.") UUID clienteId,
        @NotNull(message = "O plano e obrigatorio.") UUID planoId) {
}
