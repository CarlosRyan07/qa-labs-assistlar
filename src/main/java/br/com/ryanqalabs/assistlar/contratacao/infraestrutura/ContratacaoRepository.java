package br.com.ryanqalabs.assistlar.contratacao.infraestrutura;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.ryanqalabs.assistlar.contratacao.dominio.Contratacao;

public interface ContratacaoRepository extends JpaRepository<Contratacao, UUID> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "planoAssistencia"})
    Optional<Contratacao> findById(UUID id);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select contratacao from Contratacao contratacao where contratacao.id = :id")
    Optional<Contratacao> buscarPorIdComBloqueio(@Param("id") UUID id);
}
