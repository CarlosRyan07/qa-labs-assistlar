package br.com.ryanqalabs.assistlar;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;

@SpringBootTest
class MigracoesBancoIT extends PostgreSqlTestContainer {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void deveAplicarTodasAsMigracoesSemPendencias() {
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().applied()).hasSize(3);
    }

    @Test
    void deveCadastrarPlanosComCincoCoberturas() {
        Long planos = jdbcClient.sql("SELECT COUNT(*) FROM plano_assistencia").query(Long.class).single();
        Long coberturas = jdbcClient.sql("SELECT COUNT(*) FROM cobertura_assistencia").query(Long.class).single();

        assertThat(planos).isEqualTo(2);
        assertThat(coberturas).isEqualTo(5);
    }

    @Test
    void deveManterChaveiroAusenteNoPlanoEssencial() {
        Long coberturas = jdbcClient.sql("""
                SELECT COUNT(*)
                  FROM cobertura_assistencia c
                  JOIN plano_assistencia p ON p.id = c.plano_assistencia_id
                 WHERE p.codigo = 'ESSENCIAL'
                   AND c.tipo_assistencia = 'CHAVEIRO'
                """).query(Long.class).single();

        assertThat(coberturas).isZero();
    }
}
