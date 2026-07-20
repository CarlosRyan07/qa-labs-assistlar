package br.com.ryanqalabs.assistlar.elegibilidade.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.ryanqalabs.assistlar.cliente.dominio.StatusCliente;

class AvaliadorElegibilidadeTest {

    private static final UUID CLIENTE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID PLANO_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-07-20T15:00:00Z"),
            ZoneId.of("America/Sao_Paulo"));
    private final AvaliadorElegibilidade avaliador = new AvaliadorElegibilidade();

    @Test
    void deveConsiderarElegivelClienteAtivoAdultoSemContratacao() {
        ResultadoElegibilidade resultado = avaliador.avaliar(contexto(
                StatusCliente.ATIVO, LocalDate.of(1990, 1, 1), true, false), RELOGIO);

        assertThat(resultado.clienteId()).isEqualTo(CLIENTE_ID);
        assertThat(resultado.planoId()).isEqualTo(PLANO_ID);
        assertThat(resultado.elegivel()).isTrue();
        assertThat(resultado.motivos()).isEmpty();
    }

    @Test
    void deveAceitarExatamenteDezoitoAnos() {
        ResultadoElegibilidade resultado = avaliador.avaliar(contexto(
                StatusCliente.ATIVO, LocalDate.of(2008, 7, 20), true, false), RELOGIO);

        assertThat(resultado.elegivel()).isTrue();
    }

    @Test
    void deveRejeitarQuemAindaNaoCompletouDezoitoAnos() {
        ResultadoElegibilidade resultado = avaliador.avaliar(contexto(
                StatusCliente.ATIVO, LocalDate.of(2008, 7, 21), true, false), RELOGIO);

        assertThat(resultado.elegivel()).isFalse();
        assertThat(resultado.motivos()).containsExactly(MotivoInelegibilidade.CLIENTE_MENOR_DE_IDADE);
    }

    @Test
    void deveAcumularTodosOsMotivosDeInelegibilidade() {
        ResultadoElegibilidade resultado = avaliador.avaliar(contexto(
                StatusCliente.INATIVO, LocalDate.of(2010, 1, 1), false, true), RELOGIO);

        assertThat(resultado.elegivel()).isFalse();
        assertThat(resultado.motivos()).containsExactly(
                MotivoInelegibilidade.CLIENTE_INATIVO,
                MotivoInelegibilidade.CLIENTE_MENOR_DE_IDADE,
                MotivoInelegibilidade.PLANO_INATIVO,
                MotivoInelegibilidade.CLIENTE_POSSUI_CONTRATACAO_VIGENTE);
    }

    private ContextoElegibilidade contexto(StatusCliente status, LocalDate nascimento,
            boolean planoAtivo, boolean possuiContratacaoVigente) {
        return new ContextoElegibilidade(CLIENTE_ID, status, nascimento, PLANO_ID,
                planoAtivo, possuiContratacaoVigente);
    }
}
