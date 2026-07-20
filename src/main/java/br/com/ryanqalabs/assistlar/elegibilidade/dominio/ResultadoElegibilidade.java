package br.com.ryanqalabs.assistlar.elegibilidade.dominio;

import java.util.List;
import java.util.UUID;

public record ResultadoElegibilidade(
        UUID clienteId,
        UUID planoId,
        boolean elegivel,
        List<MotivoInelegibilidade> motivos) {

    public ResultadoElegibilidade {
        motivos = List.copyOf(motivos);
    }
}
