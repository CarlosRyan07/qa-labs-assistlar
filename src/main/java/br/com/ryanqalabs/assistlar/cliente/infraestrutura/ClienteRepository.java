package br.com.ryanqalabs.assistlar.cliente.infraestrutura;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Page<Cliente> findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nome, String email, Pageable pageable);
}
