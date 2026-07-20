package br.com.ryanqalabs.assistlar.cliente.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoDadosInvalidos;

class ClienteTest {

    private static final ZoneId FUSO_NEGOCIO = ZoneId.of("America/Sao_Paulo");
    private static final Instant AGORA = Instant.parse("2026-07-20T15:00:00Z");
    private static final Clock RELOGIO = Clock.fixed(AGORA, FUSO_NEGOCIO);

    @Test
    void deveCadastrarMenorDeIdadeAtivoENormalizarDados() {
        Cliente cliente = Cliente.cadastrar("  Ana Silva  ", "  ANA@EXEMPLO.COM  ",
                LocalDate.of(2010, 7, 20), RELOGIO);

        assertThat(cliente.getId()).isNotNull();
        assertThat(cliente.getNome()).isEqualTo("Ana Silva");
        assertThat(cliente.getEmail()).isEqualTo("ana@exemplo.com");
        assertThat(cliente.getDataNascimento()).isEqualTo(LocalDate.of(2010, 7, 20));
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.ATIVO);
        assertThat(cliente.getCriadoEm()).isEqualTo(AGORA);
        assertThat(cliente.getAtualizadoEm()).isEqualTo(AGORA);
    }

    @Test
    void deveAceitarExatamenteDezoitoAnos() {
        Cliente cliente = Cliente.cadastrar("Cliente", "cliente18@exemplo.com",
                LocalDate.of(2008, 7, 20), RELOGIO);

        assertThat(cliente.getDataNascimento()).isEqualTo(LocalDate.of(2008, 7, 20));
    }

    @Test
    void deveAceitarExatamenteCentoEVinteAnos() {
        Cliente cliente = Cliente.cadastrar("Cliente", "cliente120@exemplo.com",
                LocalDate.of(1906, 7, 20), RELOGIO);

        assertThat(cliente.getDataNascimento()).isEqualTo(LocalDate.of(1906, 7, 20));
    }

    @Test
    void deveRejeitarDataFutura() {
        assertThatThrownBy(() -> Cliente.cadastrar("Cliente", "futuro@exemplo.com",
                LocalDate.of(2026, 7, 21), RELOGIO))
                .isInstanceOf(ExcecaoDadosInvalidos.class)
                .hasMessage("A data de nascimento nao pode estar no futuro.");
    }

    @Test
    void deveRejeitarIdadeSuperiorACentoEVinteAnosConsiderandoADataCompleta() {
        assertThatThrownBy(() -> Cliente.cadastrar("Cliente", "antigo@exemplo.com",
                LocalDate.of(1906, 7, 19), RELOGIO))
                .isInstanceOf(ExcecaoDadosInvalidos.class)
                .hasMessage("A idade do cliente nao pode ser superior a 120 anos.");
    }

    @Test
    void deveInativarEReativarAtualizandoTimestamp() {
        Cliente cliente = Cliente.cadastrar("Cliente", "estado@exemplo.com",
                LocalDate.of(1990, 1, 1), RELOGIO);
        Instant depois = AGORA.plusSeconds(60);

        cliente.inativar(depois);
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.INATIVO);
        assertThat(cliente.getAtualizadoEm()).isEqualTo(depois);

        cliente.reativar(depois.plusSeconds(60));
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.ATIVO);
        assertThat(cliente.getAtualizadoEm()).isEqualTo(depois.plusSeconds(60));
    }

    @Test
    void deveRejeitarTransicoesRepetidas() {
        Cliente cliente = Cliente.cadastrar("Cliente", "repetido@exemplo.com",
                LocalDate.of(1990, 1, 1), RELOGIO);

        assertThatThrownBy(() -> cliente.reativar(AGORA.plusSeconds(1)))
                .isInstanceOf(ExcecaoConflito.class)
                .hasMessage("O cliente ja esta ativo.");

        cliente.inativar(AGORA.plusSeconds(2));
        assertThatThrownBy(() -> cliente.inativar(AGORA.plusSeconds(3)))
                .isInstanceOf(ExcecaoConflito.class)
                .hasMessage("O cliente ja esta inativo.");
    }
}
