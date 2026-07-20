package br.com.ryanqalabs.assistlar.elegibilidade.api;

import java.util.List;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.elegibilidade.dominio.MotivoInelegibilidade;
import br.com.ryanqalabs.assistlar.elegibilidade.dominio.ResultadoElegibilidade;

public record ElegibilidadeResposta(
        UUID clienteId,
        UUID planoId,
        boolean elegivel,
        List<MotivoInelegibilidade> motivos) {

    public static ElegibilidadeResposta de(ResultadoElegibilidade resultado) {
        return new ElegibilidadeResposta(resultado.clienteId(), resultado.planoId(),
                resultado.elegivel(), resultado.motivos());
    }
}
