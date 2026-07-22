package br.com.ryanqalabs.assistlar.plano.api;

import java.util.List;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.plano.dominio.PlanoAssistencia;

public record PlanoResposta(
        UUID id,
        String codigo,
        String nome,
        String descricao,
        List<CoberturaResposta> coberturas) {

    public static PlanoResposta de(PlanoAssistencia plano) {
        List<CoberturaResposta> coberturas = plano.getCoberturas().stream()
                .map(CoberturaResposta::de)
                .sorted((primeira, segunda) -> primeira.tipoAssistencia().compareTo(segunda.tipoAssistencia()))
                .toList();
        return new PlanoResposta(plano.getId(), plano.getCodigo(), plano.getNome(), plano.getDescricao(), coberturas);
    }
}
