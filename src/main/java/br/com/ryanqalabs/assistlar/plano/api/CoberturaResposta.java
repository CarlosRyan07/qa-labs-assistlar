package br.com.ryanqalabs.assistlar.plano.api;

import br.com.ryanqalabs.assistlar.plano.dominio.CoberturaAssistencia;
import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;

public record CoberturaResposta(TipoAssistencia tipoAssistencia, int limiteUtilizacoes) {

    static CoberturaResposta de(CoberturaAssistencia cobertura) {
        return new CoberturaResposta(cobertura.getTipoAssistencia(), cobertura.getLimiteUtilizacoes());
    }
}
