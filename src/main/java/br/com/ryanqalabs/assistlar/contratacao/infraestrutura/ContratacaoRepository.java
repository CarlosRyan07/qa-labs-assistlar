package br.com.ryanqalabs.assistlar.contratacao.infraestrutura;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ryanqalabs.assistlar.contratacao.dominio.Contratacao;

public interface ContratacaoRepository extends JpaRepository<Contratacao, UUID> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "planoAssistencia"})
    Optional<Contratacao> findById(UUID id);
}
