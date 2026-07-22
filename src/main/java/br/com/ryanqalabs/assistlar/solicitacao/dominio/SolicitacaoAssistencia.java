package br.com.ryanqalabs.assistlar.solicitacao.dominio;

import java.time.Instant;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoRegraNegocio;
import br.com.ryanqalabs.assistlar.contratacao.dominio.Contratacao;
import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "solicitacao_assistencia")
public class SolicitacaoAssistencia {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contratacao_id", nullable = false)
    private Contratacao contratacao;

    @Enumerated(EnumType.STRING)
    private TipoAssistencia tipoAssistencia;

    private String descricaoProblema;

    @Enumerated(EnumType.STRING)
    private StatusSolicitacao status;

    private String motivoCancelamento;
    private Instant abertaEm;
    private Instant iniciadaEm;
    private Instant concluidaEm;
    private Instant canceladaEm;

    @Version
    private long versao;

    protected SolicitacaoAssistencia() {
    }

    private SolicitacaoAssistencia(UUID id, Contratacao contratacao, TipoAssistencia tipoAssistencia,
            String descricaoProblema, Instant abertaEm) {
        this.id = id;
        this.contratacao = contratacao;
        this.tipoAssistencia = tipoAssistencia;
        this.descricaoProblema = descricaoProblema.strip();
        this.status = StatusSolicitacao.ABERTA;
        this.abertaEm = abertaEm;
    }

    public static SolicitacaoAssistencia abrir(Contratacao contratacao, TipoAssistencia tipoAssistencia,
            String descricaoProblema, Instant instante) {
        return new SolicitacaoAssistencia(UUID.randomUUID(), contratacao, tipoAssistencia, descricaoProblema, instante);
    }

    public void iniciar(Instant instante) {
        if (status != StatusSolicitacao.ABERTA) {
            throw transicaoInvalida("Somente uma solicitacao aberta pode iniciar atendimento.");
        }
        status = StatusSolicitacao.EM_ATENDIMENTO;
        iniciadaEm = instante;
    }

    public void concluir(Instant instante) {
        if (status != StatusSolicitacao.EM_ATENDIMENTO) {
            throw transicaoInvalida("Somente uma solicitacao em atendimento pode ser concluida.");
        }
        status = StatusSolicitacao.CONCLUIDA;
        concluidaEm = instante;
    }

    public void cancelar(String motivo, Instant instante) {
        if (status != StatusSolicitacao.ABERTA && status != StatusSolicitacao.EM_ATENDIMENTO) {
            throw transicaoInvalida("Somente uma solicitacao aberta ou em atendimento pode ser cancelada.");
        }
        if (status == StatusSolicitacao.EM_ATENDIMENTO && (motivo == null || motivo.isBlank())) {
            throw new ExcecaoRegraNegocio("motivo-cancelamento-obrigatorio",
                    "O motivo e obrigatorio para cancelar uma solicitacao em atendimento.");
        }
        status = StatusSolicitacao.CANCELADA;
        motivoCancelamento = motivo == null || motivo.isBlank() ? null : motivo.strip();
        canceladaEm = instante;
    }

    private ExcecaoConflito transicaoInvalida(String mensagem) {
        return new ExcecaoConflito("transicao-solicitacao-invalida", mensagem);
    }

    public UUID getId() {
        return id;
    }

    public Contratacao getContratacao() {
        return contratacao;
    }

    public TipoAssistencia getTipoAssistencia() {
        return tipoAssistencia;
    }

    public String getDescricaoProblema() {
        return descricaoProblema;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public Instant getAbertaEm() {
        return abertaEm;
    }

    public Instant getIniciadaEm() {
        return iniciadaEm;
    }

    public Instant getConcluidaEm() {
        return concluidaEm;
    }

    public Instant getCanceladaEm() {
        return canceladaEm;
    }

    public long getVersao() {
        return versao;
    }
}
