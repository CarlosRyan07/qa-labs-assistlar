package br.com.ryanqalabs.assistlar.contratacao.dominio;

import java.time.Instant;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.plano.dominio.PlanoAssistencia;
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
@Table(name = "contratacao")
public class Contratacao {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_assistencia_id", nullable = false)
    private PlanoAssistencia planoAssistencia;

    @Enumerated(EnumType.STRING)
    private StatusContratacao status;

    private Instant criadaEm;
    private Instant ativadaEm;
    private Instant canceladaEm;

    @Version
    private long versao;

    protected Contratacao() {
    }

    private Contratacao(UUID id, Cliente cliente, PlanoAssistencia planoAssistencia, Instant criadaEm) {
        this.id = id;
        this.cliente = cliente;
        this.planoAssistencia = planoAssistencia;
        this.status = StatusContratacao.PENDENTE;
        this.criadaEm = criadaEm;
    }

    public static Contratacao criar(Cliente cliente, PlanoAssistencia planoAssistencia, Instant instante) {
        return new Contratacao(UUID.randomUUID(), cliente, planoAssistencia, instante);
    }

    public void ativar(Instant instante) {
        if (status != StatusContratacao.PENDENTE) {
            throw new ExcecaoConflito("transicao-contratacao-invalida",
                    "Somente uma contratacao pendente pode ser ativada.");
        }
        status = StatusContratacao.ATIVA;
        ativadaEm = instante;
    }

    public void cancelar(Instant instante) {
        if (status == StatusContratacao.CANCELADA) {
            throw new ExcecaoConflito("transicao-contratacao-invalida", "A contratacao ja esta cancelada.");
        }
        status = StatusContratacao.CANCELADA;
        canceladaEm = instante;
    }

    public UUID getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public PlanoAssistencia getPlanoAssistencia() {
        return planoAssistencia;
    }

    public StatusContratacao getStatus() {
        return status;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getAtivadaEm() {
        return ativadaEm;
    }

    public Instant getCanceladaEm() {
        return canceladaEm;
    }

    public long getVersao() {
        return versao;
    }
}
