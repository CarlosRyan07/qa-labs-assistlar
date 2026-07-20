package br.com.ryanqalabs.assistlar.solicitacao.api;

import java.time.Instant;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;
import br.com.ryanqalabs.assistlar.solicitacao.dominio.SolicitacaoAssistencia;
import br.com.ryanqalabs.assistlar.solicitacao.dominio.StatusSolicitacao;

public record SolicitacaoResposta(
        UUID id,
        UUID contratacaoId,
        TipoAssistencia tipoAssistencia,
        String descricaoProblema,
        StatusSolicitacao status,
        String motivoCancelamento,
        Instant abertaEm,
        Instant iniciadaEm,
        Instant concluidaEm,
        Instant canceladaEm,
        long versao) {

    public static SolicitacaoResposta de(SolicitacaoAssistencia solicitacao) {
        return new SolicitacaoResposta(solicitacao.getId(), solicitacao.getContratacao().getId(),
                solicitacao.getTipoAssistencia(), solicitacao.getDescricaoProblema(), solicitacao.getStatus(),
                solicitacao.getMotivoCancelamento(), solicitacao.getAbertaEm(), solicitacao.getIniciadaEm(),
                solicitacao.getConcluidaEm(), solicitacao.getCanceladaEm(), solicitacao.getVersao());
    }
}
