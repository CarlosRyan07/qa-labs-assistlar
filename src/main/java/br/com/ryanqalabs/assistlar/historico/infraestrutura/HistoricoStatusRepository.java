package br.com.ryanqalabs.assistlar.historico.infraestrutura;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ryanqalabs.assistlar.historico.dominio.HistoricoStatus;
import br.com.ryanqalabs.assistlar.historico.dominio.TipoEntidadeHistorico;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, UUID> {

    List<HistoricoStatus> findByTipoEntidadeAndEntidadeIdOrderByRegistradoEmAscIdAsc(
            TipoEntidadeHistorico tipoEntidade, UUID entidadeId);
}
