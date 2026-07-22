package br.com.ryanqalabs.assistlar.suporte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@ResourceLock("assistlar-postgresql-compartilhado")
@Import(TempoFixoTestesConfiguracao.class)
public abstract class PostgreSqlTestContainer {

    protected static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer(ImagemPostgreSql.IMAGEM)
            .withDatabaseName("assistlar_testes")
            .withUsername("assistlar")
            .withPassword("assistlar");

    static {
        POSTGRESQL.start();
    }

    @DynamicPropertySource
    static void configurarBanco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Mantem os cenarios independentes sem reiniciar o container nem remover os
     * dados de referencia criados pelo Flyway. O ResourceLock serializa os testes
     * que compartilham este banco caso a execucao paralela seja habilitada.
     */
    @BeforeEach
    protected final void limparDadosMutaveis() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    historico_status,
                    solicitacao_assistencia,
                    contratacao,
                    cliente
                """);
    }
}
