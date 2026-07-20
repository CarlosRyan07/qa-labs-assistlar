package br.com.ryanqalabs.assistlar.historico.api;

import java.time.Instant;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.historico.dominio.HistoricoStatus;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoResponsavel;

public record HistoricoStatusResposta(
        UUID id,
        String statusAnterior,
        String statusNovo,
        String motivo,
        TipoResponsavel tipoResponsavel,
        Instant registradoEm) {

    public static HistoricoStatusResposta de(HistoricoStatus historico) {
        return new HistoricoStatusResposta(historico.getId(), historico.getStatusAnterior(), historico.getStatusNovo(),
                historico.getMotivo(), historico.getTipoResponsavel(), historico.getRegistradoEm());
    }
}
