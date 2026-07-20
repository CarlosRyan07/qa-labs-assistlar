package br.com.ryanqalabs.assistlar.historico.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class HistoricoStatusTest {

    @Test
    void deveRegistrarHistoricoDaContratacaoENormalizarMotivo() {
        UUID contratacaoId = UUID.randomUUID();
        Instant instante = Instant.parse("2026-07-20T15:00:00Z");

        HistoricoStatus historico = HistoricoStatus.registrarContratacao(contratacaoId, "ATIVA", "CANCELADA",
                "  Solicitado pelo cliente  ", TipoResponsavel.OPERADOR, instante);

        assertThat(historico.getId()).isNotNull();
        assertThat(historico.getTipoEntidade()).isEqualTo(TipoEntidadeHistorico.CONTRATACAO);
        assertThat(historico.getEntidadeId()).isEqualTo(contratacaoId);
        assertThat(historico.getStatusAnterior()).isEqualTo("ATIVA");
        assertThat(historico.getStatusNovo()).isEqualTo("CANCELADA");
        assertThat(historico.getMotivo()).isEqualTo("Solicitado pelo cliente");
        assertThat(historico.getTipoResponsavel()).isEqualTo(TipoResponsavel.OPERADOR);
        assertThat(historico.getRegistradoEm()).isEqualTo(instante);
    }

    @Test
    void deveRepresentarMotivoVazioComoAusente() {
        HistoricoStatus historico = HistoricoStatus.registrarContratacao(UUID.randomUUID(), null, "PENDENTE",
                "  ", TipoResponsavel.CLIENTE, Instant.parse("2026-07-20T15:00:00Z"));

        assertThat(historico.getMotivo()).isNull();
    }
}
