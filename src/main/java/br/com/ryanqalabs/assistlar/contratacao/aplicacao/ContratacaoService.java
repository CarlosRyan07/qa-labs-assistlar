package br.com.ryanqalabs.assistlar.contratacao.aplicacao;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.cliente.infraestrutura.ClienteRepository;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoRegraNegocio;
import br.com.ryanqalabs.assistlar.compartilhado.erro.RecursoNaoEncontradoException;
import br.com.ryanqalabs.assistlar.contratacao.api.ContratacaoCancelamentoRequisicao;
import br.com.ryanqalabs.assistlar.contratacao.api.ContratacaoCriacaoRequisicao;
import br.com.ryanqalabs.assistlar.contratacao.api.ContratacaoResposta;
import br.com.ryanqalabs.assistlar.contratacao.dominio.Contratacao;
import br.com.ryanqalabs.assistlar.contratacao.infraestrutura.ConsultaSolicitacaoAtiva;
import br.com.ryanqalabs.assistlar.contratacao.infraestrutura.ContratacaoRepository;
import br.com.ryanqalabs.assistlar.elegibilidade.aplicacao.ElegibilidadeService;
import br.com.ryanqalabs.assistlar.elegibilidade.dominio.ResultadoElegibilidade;
import br.com.ryanqalabs.assistlar.historico.api.HistoricoStatusResposta;
import br.com.ryanqalabs.assistlar.historico.dominio.HistoricoStatus;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoEntidadeHistorico;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoResponsavel;
import br.com.ryanqalabs.assistlar.historico.infraestrutura.HistoricoStatusRepository;
import br.com.ryanqalabs.assistlar.plano.dominio.PlanoAssistencia;
import br.com.ryanqalabs.assistlar.plano.infraestrutura.PlanoAssistenciaRepository;

@Service
public class ContratacaoService {

    private final ContratacaoRepository repository;
    private final ClienteRepository clienteRepository;
    private final PlanoAssistenciaRepository planoRepository;
    private final ElegibilidadeService elegibilidadeService;
    private final HistoricoStatusRepository historicoRepository;
    private final ConsultaSolicitacaoAtiva consultaSolicitacaoAtiva;
    private final Clock relogio;

    public ContratacaoService(ContratacaoRepository repository, ClienteRepository clienteRepository,
            PlanoAssistenciaRepository planoRepository, ElegibilidadeService elegibilidadeService,
            HistoricoStatusRepository historicoRepository, ConsultaSolicitacaoAtiva consultaSolicitacaoAtiva,
            Clock relogio) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.planoRepository = planoRepository;
        this.elegibilidadeService = elegibilidadeService;
        this.historicoRepository = historicoRepository;
        this.consultaSolicitacaoAtiva = consultaSolicitacaoAtiva;
        this.relogio = relogio;
    }

    @Transactional
    public ContratacaoResposta criar(ContratacaoCriacaoRequisicao requisicao) {
        ResultadoElegibilidade elegibilidade = elegibilidadeService.avaliar(requisicao.clienteId(), requisicao.planoId());
        if (!elegibilidade.elegivel()) {
            throw new ExcecaoRegraNegocio("cliente-inelegivel",
                    "O cliente nao pode contratar o plano. Motivos: " + elegibilidade.motivos());
        }

        Cliente cliente = clienteRepository.getReferenceById(requisicao.clienteId());
        PlanoAssistencia plano = planoRepository.getReferenceById(requisicao.planoId());
        Instant instante = Instant.now(relogio);
        Contratacao contratacao = Contratacao.criar(cliente, plano, instante);

        try {
            repository.saveAndFlush(contratacao);
        } catch (DataIntegrityViolationException excecao) {
            throw new ExcecaoConflito("contratacao-vigente-existente",
                    "O cliente ja possui uma contratacao pendente ou ativa.");
        }
        historicoRepository.save(HistoricoStatus.registrarContratacao(contratacao.getId(), null,
                contratacao.getStatus().name(), null, TipoResponsavel.CLIENTE, instante));
        return ContratacaoResposta.de(contratacao);
    }

    @Transactional(readOnly = true)
    public ContratacaoResposta buscar(UUID id) {
        return ContratacaoResposta.de(buscarEntidade(id));
    }

    @Transactional
    public ContratacaoResposta ativar(UUID id) {
        Contratacao contratacao = buscarEntidade(id);
        String statusAnterior = contratacao.getStatus().name();
        Instant instante = Instant.now(relogio);
        contratacao.ativar(instante);
        salvarTransicao(contratacao, statusAnterior, null, TipoResponsavel.OPERADOR, instante);
        return ContratacaoResposta.de(contratacao);
    }

    @Transactional
    public ContratacaoResposta cancelar(UUID id, ContratacaoCancelamentoRequisicao requisicao) {
        Contratacao contratacao = buscarEntidade(id);
        if (consultaSolicitacaoAtiva.existeParaContratacao(id)) {
            throw new ExcecaoRegraNegocio("contratacao-com-solicitacao-em-andamento",
                    "A contratacao nao pode ser cancelada enquanto houver solicitacao aberta ou em atendimento.");
        }
        String statusAnterior = contratacao.getStatus().name();
        Instant instante = Instant.now(relogio);
        contratacao.cancelar(instante);
        salvarTransicao(contratacao, statusAnterior, requisicao.motivo(), TipoResponsavel.OPERADOR, instante);
        return ContratacaoResposta.de(contratacao);
    }

    @Transactional(readOnly = true)
    public List<HistoricoStatusResposta> listarHistorico(UUID id) {
        buscarEntidade(id);
        return historicoRepository
                .findByTipoEntidadeAndEntidadeIdOrderByRegistradoEmAscIdAsc(TipoEntidadeHistorico.CONTRATACAO, id)
                .stream()
                .map(HistoricoStatusResposta::de)
                .toList();
    }

    private void salvarTransicao(Contratacao contratacao, String statusAnterior, String motivo,
            TipoResponsavel responsavel, Instant instante) {
        try {
            repository.saveAndFlush(contratacao);
        } catch (ObjectOptimisticLockingFailureException excecao) {
            throw new ExcecaoConflito("concorrencia-contratacao",
                    "A contratacao foi alterada simultaneamente. Consulte o estado atual e tente novamente.");
        }
        historicoRepository.save(HistoricoStatus.registrarContratacao(contratacao.getId(), statusAnterior,
                contratacao.getStatus().name(), motivo, responsavel, instante));
    }

    private Contratacao buscarEntidade(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(
                "contratacao-nao-encontrada", "Contratacao nao encontrada."));
    }
}
