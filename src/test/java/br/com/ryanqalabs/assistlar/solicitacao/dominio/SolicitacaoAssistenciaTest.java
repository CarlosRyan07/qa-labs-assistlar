package br.com.ryanqalabs.assistlar.solicitacao.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoRegraNegocio;
import br.com.ryanqalabs.assistlar.contratacao.dominio.Contratacao;
import br.com.ryanqalabs.assistlar.plano.dominio.PlanoAssistencia;
import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;

class SolicitacaoAssistenciaTest {

    private static final Instant ABERTA_EM = Instant.parse("2026-07-20T15:00:00Z");

    @Test
    void deveAbrirIniciarEConcluirSolicitacao() {
        SolicitacaoAssistencia solicitacao = novaSolicitacao();

        assertThat(solicitacao.getId()).isNotNull();
        assertThat(solicitacao.getContratacao()).isNotNull();
        assertThat(solicitacao.getTipoAssistencia()).isEqualTo(TipoAssistencia.ELETRICISTA);
        assertThat(solicitacao.getDescricaoProblema()).isEqualTo("Tomada sem energia");
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.ABERTA);
        assertThat(solicitacao.getAbertaEm()).isEqualTo(ABERTA_EM);
        assertThat(solicitacao.getVersao()).isZero();

        solicitacao.iniciar(ABERTA_EM.plusSeconds(60));
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.EM_ATENDIMENTO);
        assertThat(solicitacao.getIniciadaEm()).isEqualTo(ABERTA_EM.plusSeconds(60));

        solicitacao.concluir(ABERTA_EM.plusSeconds(120));
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.CONCLUIDA);
        assertThat(solicitacao.getConcluidaEm()).isEqualTo(ABERTA_EM.plusSeconds(120));
    }

    @Test
    void deveCancelarAbertaSemExigirMotivo() {
        SolicitacaoAssistencia solicitacao = novaSolicitacao();

        solicitacao.cancelar("  ", ABERTA_EM.plusSeconds(60));

        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.CANCELADA);
        assertThat(solicitacao.getMotivoCancelamento()).isNull();
        assertThat(solicitacao.getCanceladaEm()).isEqualTo(ABERTA_EM.plusSeconds(60));
    }

    @Test
    void deveExigirMotivoParaCancelarEmAtendimento() {
        SolicitacaoAssistencia solicitacao = novaSolicitacao();
        solicitacao.iniciar(ABERTA_EM.plusSeconds(60));

        assertThatThrownBy(() -> solicitacao.cancelar(null, ABERTA_EM.plusSeconds(120)))
                .isInstanceOf(ExcecaoRegraNegocio.class)
                .hasMessage("O motivo e obrigatorio para cancelar uma solicitacao em atendimento.");

        solicitacao.cancelar("  Risco eletrico eliminado  ", ABERTA_EM.plusSeconds(120));
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.CANCELADA);
        assertThat(solicitacao.getMotivoCancelamento()).isEqualTo("Risco eletrico eliminado");
    }

    @Test
    void deveRejeitarTransicoesInvalidas() {
        SolicitacaoAssistencia aberta = novaSolicitacao();
        assertThatThrownBy(() -> aberta.concluir(ABERTA_EM.plusSeconds(60)))
                .isInstanceOf(ExcecaoConflito.class);

        aberta.iniciar(ABERTA_EM.plusSeconds(60));
        assertThatThrownBy(() -> aberta.iniciar(ABERTA_EM.plusSeconds(90)))
                .isInstanceOf(ExcecaoConflito.class);

        aberta.concluir(ABERTA_EM.plusSeconds(120));
        assertThatThrownBy(() -> aberta.cancelar("Motivo", ABERTA_EM.plusSeconds(180)))
                .isInstanceOf(ExcecaoConflito.class);
    }

    private SolicitacaoAssistencia novaSolicitacao() {
        Clock relogio = Clock.fixed(ABERTA_EM, ZoneOffset.UTC);
        Cliente cliente = Cliente.cadastrar("Cliente", "solicitacao@exemplo.com",
                LocalDate.of(1990, 1, 1), relogio);
        Contratacao contratacao = Contratacao.criar(cliente, new PlanoTeste(), ABERTA_EM);
        contratacao.ativar(ABERTA_EM);
        return SolicitacaoAssistencia.abrir(contratacao, TipoAssistencia.ELETRICISTA,
                "  Tomada sem energia  ", ABERTA_EM);
    }

    private static final class PlanoTeste extends PlanoAssistencia {
    }
}
