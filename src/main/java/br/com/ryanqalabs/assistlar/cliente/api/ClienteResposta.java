package br.com.ryanqalabs.assistlar.cliente.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.cliente.dominio.StatusCliente;

public record ClienteResposta(
        UUID id,
        String nome,
        String email,
        LocalDate dataNascimento,
        StatusCliente status,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static ClienteResposta de(Cliente cliente) {
        return new ClienteResposta(cliente.getId(), cliente.getNome(), cliente.getEmail(),
                cliente.getDataNascimento(), cliente.getStatus(), cliente.getCriadoEm(), cliente.getAtualizadoEm());
    }
}
