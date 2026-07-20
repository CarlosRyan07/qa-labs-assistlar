package br.com.ryanqalabs.assistlar.plano.dominio;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cobertura_assistencia")
public class CoberturaAssistencia {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_assistencia_id", nullable = false)
    private PlanoAssistencia planoAssistencia;

    @Enumerated(EnumType.STRING)
    private TipoAssistencia tipoAssistencia;

    private int limiteUtilizacoes;

    protected CoberturaAssistencia() {
    }

    public UUID getId() {
        return id;
    }

    public TipoAssistencia getTipoAssistencia() {
        return tipoAssistencia;
    }

    public int getLimiteUtilizacoes() {
        return limiteUtilizacoes;
    }
}
