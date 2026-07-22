package br.com.ryanqalabs.assistlar.plano.infraestrutura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ryanqalabs.assistlar.plano.dominio.PlanoAssistencia;

public interface PlanoAssistenciaRepository extends JpaRepository<PlanoAssistencia, UUID> {

    @EntityGraph(attributePaths = "coberturas")
    List<PlanoAssistencia> findAllByAtivoTrueOrderByNomeAsc();

    @EntityGraph(attributePaths = "coberturas")
    Optional<PlanoAssistencia> findByIdAndAtivoTrue(UUID id);
}
