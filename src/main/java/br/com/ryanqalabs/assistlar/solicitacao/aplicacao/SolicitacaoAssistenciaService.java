package br.com.ryanqalabs.assistlar.solicitacao.aplicacao;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoRegraNegocio;
import br.com.ryanqalabs.assistlar.compartilhado.erro.RecursoNaoEncontradoException;
import br.com.ryanqalabs.assistlar.contratacao.dominio.Contratacao;
import br.com.ryanqalabs.assistlar.contratacao.dominio.StatusContratacao;
import br.com.ryanqalabs.assistlar.contratacao.infraestrutura.ContratacaoRepository;
import br.com.ryanqalabs.assistlar.historico.api.HistoricoStatusResposta;
import br.com.ryanqalabs.assistlar.historico.dominio.HistoricoStatus;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoEntidadeHistorico;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoResponsavel;
import br.com.ryanqalabs.assistlar.historico.infraestrutura.HistoricoStatusRepository;
import br.com.ryanqalabs.assistlar.solicitacao.api.SolicitacaoCancelamentoRequisicao;
import br.com.ryanqalabs.assistlar.solicitacao.api.SolicitacaoCriacaoRequisicao;
import br.com.ryanqalabs.assistlar.solicitacao.api.SolicitacaoResposta;
import br.com.ryanqalabs.assistlar.solicitacao.dominio.SolicitacaoAssistencia;
import br.com.ryanqalabs.assistlar.solicitacao.dominio.StatusSolicitacao;
import br.com.ryanqalabs.assistlar.solicitacao.infraestrutura.SolicitacaoAssistenciaRepository;

@Service
public class SolicitacaoAssistenciaService {

    private static final EnumSet<StatusSolicitacao> STATUS_EM_ANDAMENTO =
            EnumSet.of(StatusSolicitacao.ABERTA, StatusSolicitacao.EM_ATENDIMENTO);

    private final SolicitacaoAssistenciaRepository repository;
    private final ContratacaoRepository contratacaoRepository;
    private final HistoricoStatusRepository historicoRepository;
    private final Clock relogio;

    public SolicitacaoAssistenciaService(SolicitacaoAssistenciaRepository repository,
            ContratacaoRepository contratacaoRepository, HistoricoStatusRepository historicoRepository,
            Clock relogio) {
        this.repository = repository;
        this.contratacaoRepository = contratacaoRepository;
        this.historicoRepository = historicoRepository;
        this.relogio = relogio;
    }

    @Transactional
    public SolicitacaoResposta abrir(SolicitacaoCriacaoRequisicao requisicao) {
        Contratacao contratacao = contratacaoRepository.findById(requisicao.contratacaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "contratacao-nao-encontrada", "Contratacao nao encontrada."));
        if (contratacao.getStatus() != StatusContratacao.ATIVA) {
            throw new ExcecaoRegraNegocio("contratacao-nao-ativa",
                    "A solicitacao exige uma contratacao ativa.");
        }
        if (!contratacao.getPlanoAssistencia().cobre(requisicao.tipoAssistencia())) {
            throw new ExcecaoRegraNegocio("servico-nao-coberto",
                    "O tipo de assistencia nao e coberto pelo plano contratado.");
        }
        if (repository.existsByContratacaoIdAndTipoAssistenciaAndStatusIn(requisicao.contratacaoId(),
                requisicao.tipoAssistencia(), STATUS_EM_ANDAMENTO)) {
            throw solicitacaoDuplicada();
        }

        Instant instante = Instant.now(relogio);
        SolicitacaoAssistencia solicitacao = SolicitacaoAssistencia.abrir(contratacao,
                requisicao.tipoAssistencia(), requisicao.descricaoProblema(), instante);
        try {
            repository.saveAndFlush(solicitacao);
        } catch (DataIntegrityViolationException excecao) {
            throw solicitacaoDuplicada();
        }
        historicoRepository.save(HistoricoStatus.registrarSolicitacao(solicitacao.getId(), null,
                solicitacao.getStatus().name(), null, TipoResponsavel.CLIENTE, instante));
        return SolicitacaoResposta.de(solicitacao);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResposta buscar(UUID id) {
        return SolicitacaoResposta.de(buscarEntidade(id));
    }

    @Transactional
    public SolicitacaoResposta iniciar(UUID id) {
        SolicitacaoAssistencia solicitacao = buscarEntidade(id);
        String statusAnterior = solicitacao.getStatus().name();
        Instant instante = Instant.now(relogio);
        solicitacao.iniciar(instante);
        salvarTransicao(solicitacao, statusAnterior, null, TipoResponsavel.OPERADOR, instante);
        return SolicitacaoResposta.de(solicitacao);
    }

    @Transactional
    public SolicitacaoResposta concluir(UUID id) {
        SolicitacaoAssistencia solicitacao = buscarEntidade(id);
        String statusAnterior = solicitacao.getStatus().name();
        Instant instante = Instant.now(relogio);
        solicitacao.concluir(instante);
        salvarTransicao(solicitacao, statusAnterior, null, TipoResponsavel.OPERADOR, instante);
        return SolicitacaoResposta.de(solicitacao);
    }

    @Transactional
    public SolicitacaoResposta cancelar(UUID id, SolicitacaoCancelamentoRequisicao requisicao) {
        SolicitacaoAssistencia solicitacao = buscarEntidade(id);
        String statusAnterior = solicitacao.getStatus().name();
        TipoResponsavel responsavel = solicitacao.getStatus() == StatusSolicitacao.ABERTA
                ? TipoResponsavel.CLIENTE : TipoResponsavel.OPERADOR;
        Instant instante = Instant.now(relogio);
        solicitacao.cancelar(requisicao.motivo(), instante);
        salvarTransicao(solicitacao, statusAnterior, requisicao.motivo(), responsavel, instante);
        return SolicitacaoResposta.de(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<HistoricoStatusResposta> listarHistorico(UUID id) {
        buscarEntidade(id);
        return historicoRepository.findByTipoEntidadeAndEntidadeIdOrderByRegistradoEmAscIdAsc(
                        TipoEntidadeHistorico.SOLICITACAO_ASSISTENCIA, id)
                .stream().map(HistoricoStatusResposta::de).toList();
    }

    private void salvarTransicao(SolicitacaoAssistencia solicitacao, String statusAnterior, String motivo,
            TipoResponsavel responsavel, Instant instante) {
        try {
            repository.saveAndFlush(solicitacao);
        } catch (ObjectOptimisticLockingFailureException excecao) {
            throw new ExcecaoConflito("concorrencia-solicitacao",
                    "A solicitacao foi alterada simultaneamente. Consulte o estado atual e tente novamente.");
        }
        historicoRepository.save(HistoricoStatus.registrarSolicitacao(solicitacao.getId(), statusAnterior,
                solicitacao.getStatus().name(), motivo, responsavel, instante));
    }

    private SolicitacaoAssistencia buscarEntidade(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(
                "solicitacao-nao-encontrada", "Solicitacao de assistencia nao encontrada."));
    }

    private ExcecaoConflito solicitacaoDuplicada() {
        return new ExcecaoConflito("solicitacao-em-andamento-existente",
                "Ja existe uma solicitacao aberta ou em atendimento para este tipo de assistencia.");
    }
}
