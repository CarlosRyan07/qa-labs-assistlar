package br.com.ryanqalabs.assistlar.cliente.infraestrutura;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    boolean existsByEmailIgnoreCase(String email);
}
