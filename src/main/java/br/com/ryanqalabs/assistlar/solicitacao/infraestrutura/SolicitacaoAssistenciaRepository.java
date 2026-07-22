package br.com.ryanqalabs.assistlar.solicitacao.infraestrutura;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ryanqalabs.assistlar.plano.dominio.TipoAssistencia;
import br.com.ryanqalabs.assistlar.solicitacao.dominio.SolicitacaoAssistencia;
import br.com.ryanqalabs.assistlar.solicitacao.dominio.StatusSolicitacao;

public interface SolicitacaoAssistenciaRepository extends JpaRepository<SolicitacaoAssistencia, UUID> {

    @Override
    @EntityGraph(attributePaths = {"contratacao", "contratacao.cliente", "contratacao.planoAssistencia"})
    Optional<SolicitacaoAssistencia> findById(UUID id);

    boolean existsByContratacaoIdAndTipoAssistenciaAndStatusIn(UUID contratacaoId,
            TipoAssistencia tipoAssistencia, Collection<StatusSolicitacao> status);

    long countByContratacaoIdAndTipoAssistenciaAndStatusIn(UUID contratacaoId,
            TipoAssistencia tipoAssistencia, Collection<StatusSolicitacao> status);
}
