package br.com.ryanqalabs.assistlar.contratacao.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.plano.dominio.PlanoAssistencia;

class ContratacaoTest {

    private static final Instant CRIADA_EM = Instant.parse("2026-07-20T15:00:00Z");

    @Test
    void deveCriarPendenteEAtivar() {
        Contratacao contratacao = novaContratacao();
        Instant ativadaEm = CRIADA_EM.plusSeconds(60);

        assertThat(contratacao.getId()).isNotNull();
        assertThat(contratacao.getStatus()).isEqualTo(StatusContratacao.PENDENTE);
        assertThat(contratacao.getCriadaEm()).isEqualTo(CRIADA_EM);
        assertThat(contratacao.getAtivadaEm()).isNull();
        assertThat(contratacao.getCanceladaEm()).isNull();
        assertThat(contratacao.getVersao()).isZero();

        contratacao.ativar(ativadaEm);

        assertThat(contratacao.getStatus()).isEqualTo(StatusContratacao.ATIVA);
        assertThat(contratacao.getAtivadaEm()).isEqualTo(ativadaEm);
    }

    @Test
    void devePermitirCancelamentoDePendenteOuAtiva() {
        Contratacao pendente = novaContratacao();
        pendente.cancelar(CRIADA_EM.plusSeconds(30));
        assertThat(pendente.getStatus()).isEqualTo(StatusContratacao.CANCELADA);

        Contratacao ativa = novaContratacao();
        ativa.ativar(CRIADA_EM.plusSeconds(30));
        ativa.cancelar(CRIADA_EM.plusSeconds(60));
        assertThat(ativa.getStatus()).isEqualTo(StatusContratacao.CANCELADA);
        assertThat(ativa.getCanceladaEm()).isEqualTo(CRIADA_EM.plusSeconds(60));
    }

    @Test
    void deveRejeitarTransicoesInvalidas() {
        Contratacao contratacao = novaContratacao();
        contratacao.ativar(CRIADA_EM.plusSeconds(30));

        assertThatThrownBy(() -> contratacao.ativar(CRIADA_EM.plusSeconds(60)))
                .isInstanceOf(ExcecaoConflito.class)
                .hasMessage("Somente uma contratacao pendente pode ser ativada.");

        contratacao.cancelar(CRIADA_EM.plusSeconds(60));
        assertThatThrownBy(() -> contratacao.cancelar(CRIADA_EM.plusSeconds(90)))
                .isInstanceOf(ExcecaoConflito.class)
                .hasMessage("A contratacao ja esta cancelada.");
    }

    private Contratacao novaContratacao() {
        Clock relogio = Clock.fixed(CRIADA_EM, ZoneOffset.UTC);
        Cliente cliente = Cliente.cadastrar("Cliente", "contratacao@exemplo.com",
                LocalDate.of(1990, 1, 1), relogio);
        return Contratacao.criar(cliente, new PlanoTeste(), CRIADA_EM);
    }

    private static final class PlanoTeste extends PlanoAssistencia {
    }
}
