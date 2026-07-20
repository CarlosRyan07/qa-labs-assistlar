package br.com.ryanqalabs.assistlar.historico.dominio;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "historico_status")
public class HistoricoStatus {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TipoEntidadeHistorico tipoEntidade;

    private UUID entidadeId;
    private String statusAnterior;
    private String statusNovo;
    private String motivo;

    @Enumerated(EnumType.STRING)
    private TipoResponsavel tipoResponsavel;

    private Instant registradoEm;

    protected HistoricoStatus() {
    }

    private HistoricoStatus(UUID id, TipoEntidadeHistorico tipoEntidade, UUID entidadeId,
            String statusAnterior, String statusNovo, String motivo,
            TipoResponsavel tipoResponsavel, Instant registradoEm) {
        this.id = id;
        this.tipoEntidade = tipoEntidade;
        this.entidadeId = entidadeId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.motivo = normalizarMotivo(motivo);
        this.tipoResponsavel = tipoResponsavel;
        this.registradoEm = registradoEm;
    }

    public static HistoricoStatus registrarContratacao(UUID contratacaoId, String statusAnterior,
            String statusNovo, String motivo, TipoResponsavel tipoResponsavel, Instant instante) {
        return new HistoricoStatus(UUID.randomUUID(), TipoEntidadeHistorico.CONTRATACAO, contratacaoId,
                statusAnterior, statusNovo, motivo, tipoResponsavel, instante);
    }

    private static String normalizarMotivo(String motivo) {
        return motivo == null || motivo.isBlank() ? null : motivo.strip();
    }

    public UUID getId() {
        return id;
    }

    public TipoEntidadeHistorico getTipoEntidade() {
        return tipoEntidade;
    }

    public UUID getEntidadeId() {
        return entidadeId;
    }

    public String getStatusAnterior() {
        return statusAnterior;
    }

    public String getStatusNovo() {
        return statusNovo;
    }

    public String getMotivo() {
        return motivo;
    }

    public TipoResponsavel getTipoResponsavel() {
        return tipoResponsavel;
    }

    public Instant getRegistradoEm() {
        return registradoEm;
    }
}
