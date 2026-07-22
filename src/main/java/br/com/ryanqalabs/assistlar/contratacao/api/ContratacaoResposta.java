package br.com.ryanqalabs.assistlar.contratacao.api;

import java.time.Instant;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.contratacao.dominio.Contratacao;
import br.com.ryanqalabs.assistlar.contratacao.dominio.StatusContratacao;

public record ContratacaoResposta(
        UUID id,
        UUID clienteId,
        UUID planoId,
        StatusContratacao status,
        Instant criadaEm,
        Instant ativadaEm,
        Instant canceladaEm,
        long versao) {

    public static ContratacaoResposta de(Contratacao contratacao) {
        return new ContratacaoResposta(contratacao.getId(), contratacao.getCliente().getId(),
                contratacao.getPlanoAssistencia().getId(), contratacao.getStatus(), contratacao.getCriadaEm(),
                contratacao.getAtivadaEm(), contratacao.getCanceladaEm(), contratacao.getVersao());
    }
}
