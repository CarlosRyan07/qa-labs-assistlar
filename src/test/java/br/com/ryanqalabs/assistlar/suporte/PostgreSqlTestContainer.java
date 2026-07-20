package br.com.ryanqalabs.assistlar.suporte;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

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
}
