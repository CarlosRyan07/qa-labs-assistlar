package br.com.ryanqalabs.assistlar.solicitacao.api;

import java.util.UUID;

import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoCriacaoRequisicao(
        @NotNull(message = "A contratacao e obrigatoria.") UUID contratacaoId,
        @NotNull(message = "O tipo de assistencia e obrigatorio.") TipoAssistencia tipoAssistencia,
        @NotBlank(message = "A descricao do problema e obrigatoria.")
        @Size(max = 500, message = "A descricao do problema deve ter no maximo 500 caracteres.")
        String descricaoProblema) {
}
