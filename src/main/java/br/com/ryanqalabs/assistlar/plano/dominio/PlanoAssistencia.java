package br.com.ryanqalabs.assistlar.plano.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "plano_assistencia")
public class PlanoAssistencia {

    @Id
    private UUID id;
    private String codigo;
    private String nome;
    private String descricao;
    private boolean ativo;
    private Instant criadoEm;

    @OneToMany(mappedBy = "planoAssistencia", fetch = FetchType.LAZY)
    private List<CoberturaAssistencia> coberturas = new ArrayList<>();

    protected PlanoAssistencia() {
    }

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public List<CoberturaAssistencia> getCoberturas() {
        return Collections.unmodifiableList(coberturas);
    }

    public boolean cobre(TipoAssistencia tipoAssistencia) {
        return coberturas.stream().anyMatch(cobertura -> cobertura.getTipoAssistencia() == tipoAssistencia);
    }

    public int limitePara(TipoAssistencia tipoAssistencia) {
        return coberturas.stream()
                .filter(cobertura -> cobertura.getTipoAssistencia() == tipoAssistencia)
                .mapToInt(CoberturaAssistencia::getLimiteUtilizacoes)
                .findFirst()
                .orElse(0);
    }
}
