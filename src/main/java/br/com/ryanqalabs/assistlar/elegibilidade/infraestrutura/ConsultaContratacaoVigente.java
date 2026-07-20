package br.com.ryanqalabs.assistlar.elegibilidade.infraestrutura;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultaContratacaoVigente {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaContratacaoVigente(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeParaCliente(UUID clienteId) {
        Boolean existe = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM contratacao
                    WHERE cliente_id = ?
                      AND status IN ('PENDENTE', 'ATIVA')
                )
                """, Boolean.class, clienteId);
        return Boolean.TRUE.equals(existe);
    }
}
