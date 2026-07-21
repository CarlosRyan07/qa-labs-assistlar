package br.com.ryanqalabs.assistlar.solicitacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.cliente.infraestrutura.ClienteRepository;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.contratacao.api.ContratacaoCriacaoRequisicao;
import br.com.ryanqalabs.assistlar.contratacao.api.ContratacaoResposta;
import br.com.ryanqalabs.assistlar.contratacao.aplicacao.ContratacaoService;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoEntidadeHistorico;
import br.com.ryanqalabs.assistlar.historico.infraestrutura.HistoricoStatusRepository;
import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;
import br.com.ryanqalabs.assistlar.solicitacao.api.SolicitacaoCriacaoRequisicao;
import br.com.ryanqalabs.assistlar.solicitacao.api.SolicitacaoResposta;
import br.com.ryanqalabs.assistlar.solicitacao.aplicacao.SolicitacaoAssistenciaService;
import br.com.ryanqalabs.assistlar.solicitacao.dominio.StatusSolicitacao;
import br.com.ryanqalabs.assistlar.solicitacao.infraestrutura.SolicitacaoAssistenciaRepository;
import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;

@SpringBootTest
class SolicitacaoConcorrenciaIT extends PostgreSqlTestContainer {

    private static final UUID PLANO_COMPLETO = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final EnumSet<StatusSolicitacao> STATUS_QUE_CONSOMEM = EnumSet.of(
            StatusSolicitacao.ABERTA, StatusSolicitacao.EM_ATENDIMENTO, StatusSolicitacao.CONCLUIDA);

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContratacaoService contratacaoService;

    @Autowired
    private SolicitacaoAssistenciaService solicitacaoService;

    @Autowired
    private SolicitacaoAssistenciaRepository solicitacaoRepository;

    @Autowired
    private HistoricoStatusRepository historicoRepository;

    @Autowired
    private Clock relogio;

    @Test
    void deveManterLimiteEAberturaUnicaDoMesmoTipoEmOperacoesSimultaneas() throws Exception {
        UUID contratacaoId = criarContratacaoCompletaAtiva();
        SolicitacaoResposta primeira = solicitacaoService.abrir(requisicao(contratacaoId));
        solicitacaoService.iniciar(primeira.id());
        solicitacaoService.concluir(primeira.id());

        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<ResultadoConcorrencia> uma = executor.submit(
                    () -> abrirConcorrente(contratacaoId, prontas, iniciar));
            Future<ResultadoConcorrencia> outra = executor.submit(
                    () -> abrirConcorrente(contratacaoId, prontas, iniciar));
            assertThat(prontas.await(5, TimeUnit.SECONDS)).isTrue();
            iniciar.countDown();

            assertThat(List.of(uma.get(10, TimeUnit.SECONDS), outra.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(
                            ResultadoConcorrencia.SUCESSO,
                            ResultadoConcorrencia.SOLICITACAO_EM_ANDAMENTO_EXISTENTE);
        }

        long consumo = solicitacaoRepository.countByContratacaoIdAndTipoAssistenciaAndStatusIn(
                contratacaoId, TipoAssistencia.ELETRICISTA, STATUS_QUE_CONSOMEM);
        assertThat(consumo).isEqualTo(2);
    }

    @Test
    void deveManterUmaUnicaTransicaoQuandoDoisIniciosSaoSimultaneos() throws Exception {
        UUID contratacaoId = criarContratacaoCompletaAtiva();
        UUID solicitacaoId = solicitacaoService.abrir(requisicao(contratacaoId)).id();
        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<ResultadoConcorrencia> uma = executor.submit(
                    () -> iniciarConcorrente(solicitacaoId, prontas, iniciar));
            Future<ResultadoConcorrencia> outra = executor.submit(
                    () -> iniciarConcorrente(solicitacaoId, prontas, iniciar));
            assertThat(prontas.await(5, TimeUnit.SECONDS)).isTrue();
            iniciar.countDown();

            assertThat(List.of(uma.get(10, TimeUnit.SECONDS), outra.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(
                            ResultadoConcorrencia.SUCESSO,
                            ResultadoConcorrencia.CONFLITO_OTIMISTA);
        }

        assertThat(solicitacaoRepository.findById(solicitacaoId).orElseThrow().getStatus())
                .isEqualTo(StatusSolicitacao.EM_ATENDIMENTO);
        assertThat(historicoRepository.findByTipoEntidadeAndEntidadeIdOrderByRegistradoEmAscIdAsc(
                TipoEntidadeHistorico.SOLICITACAO_ASSISTENCIA, solicitacaoId)).hasSize(2);
    }

    private ResultadoConcorrencia abrirConcorrente(UUID contratacaoId, CountDownLatch prontas, CountDownLatch iniciar)
            throws InterruptedException {
        aguardarDisparo(prontas, iniciar);
        try {
            solicitacaoService.abrir(requisicao(contratacaoId));
            return ResultadoConcorrencia.SUCESSO;
        } catch (ExcecaoConflito excecao) {
            assertThat(excecao.getCodigo()).isEqualTo("solicitacao-em-andamento-existente");
            return ResultadoConcorrencia.SOLICITACAO_EM_ANDAMENTO_EXISTENTE;
        }
    }

    private ResultadoConcorrencia iniciarConcorrente(UUID solicitacaoId, CountDownLatch prontas, CountDownLatch iniciar)
            throws InterruptedException {
        aguardarDisparo(prontas, iniciar);
        try {
            solicitacaoService.iniciar(solicitacaoId);
            return ResultadoConcorrencia.SUCESSO;
        } catch (ExcecaoConflito excecao) {
            assertThat(excecao.getCodigo()).isEqualTo("concorrencia-solicitacao");
            return ResultadoConcorrencia.CONFLITO_OTIMISTA;
        }
    }

    private void aguardarDisparo(CountDownLatch prontas, CountDownLatch iniciar) throws InterruptedException {
        prontas.countDown();
        if (!iniciar.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("As operacoes concorrentes nao foram liberadas a tempo.");
        }
    }

    private UUID criarContratacaoCompletaAtiva() {
        Cliente cliente = clienteRepository.saveAndFlush(Cliente.cadastrar("Cliente Limite",
                UUID.randomUUID() + "@exemplo.com", LocalDate.of(1990, 1, 1), relogio));
        ContratacaoResposta contratacao = contratacaoService.criar(
                new ContratacaoCriacaoRequisicao(cliente.getId(), PLANO_COMPLETO));
        return contratacaoService.ativar(contratacao.id()).id();
    }

    private SolicitacaoCriacaoRequisicao requisicao(UUID contratacaoId) {
        return new SolicitacaoCriacaoRequisicao(contratacaoId, TipoAssistencia.ELETRICISTA,
                "Falha eletrica concorrente");
    }

    private enum ResultadoConcorrencia {
        SUCESSO,
        SOLICITACAO_EM_ANDAMENTO_EXISTENTE,
        CONFLITO_OTIMISTA
    }
}
