package br.com.ryanqalabs.assistlar.contratacao.infraestrutura;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultaSolicitacaoAtiva {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaSolicitacaoAtiva(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeParaContratacao(UUID contratacaoId) {
        Boolean existe = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM solicitacao_assistencia
                    WHERE contratacao_id = ?
                      AND status IN ('ABERTA', 'EM_ATENDIMENTO')
                )
                """, Boolean.class, contratacaoId);
        return Boolean.TRUE.equals(existe);
    }
}
