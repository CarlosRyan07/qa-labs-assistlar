package br.com.ryanqalabs.assistlar.elegibilidade.dominio;

import java.time.LocalDate;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.cliente.dominio.StatusCliente;

public record ContextoElegibilidade(
        UUID clienteId,
        StatusCliente statusCliente,
        LocalDate dataNascimento,
        UUID planoId,
        boolean planoAtivo,
        boolean possuiContratacaoVigente) {
}
