package br.com.ryanqalabs.assistlar.suporte;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class IsolamentoBancoIT extends PostgreSqlTestContainer {

    private static final UUID CLIENTE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID PLANO_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CONTRATACAO_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID SOLICITACAO_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID HISTORICO_ID = UUID.fromString("60000000-0000-0000-0000-000000000001");
    private static final OffsetDateTime INSTANTE = OffsetDateTime.parse("2026-07-20T15:00:00Z");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RepeatedTest(2)
    void deveLimparDadosMutaveisAntesDeCadaCenarioEPreservarReferencias() {
        assertThat(quantidade("cliente")).isZero();
        assertThat(quantidade("contratacao")).isZero();
        assertThat(quantidade("solicitacao_assistencia")).isZero();
        assertThat(quantidade("historico_status")).isZero();
        assertThat(quantidade("plano_assistencia")).isEqualTo(2);
        assertThat(quantidade("cobertura_assistencia")).isEqualTo(5);
        assertThat(quantidade("flyway_schema_history")).isEqualTo(3);

        inserirHierarquiaMutavel();

        assertThat(quantidade("cliente")).isOne();
        assertThat(quantidade("contratacao")).isOne();
        assertThat(quantidade("solicitacao_assistencia")).isOne();
        assertThat(quantidade("historico_status")).isOne();
    }

    private void inserirHierarquiaMutavel() {
        jdbcTemplate.update("""
                INSERT INTO cliente
                    (id, nome, email, data_nascimento, status, criado_em, atualizado_em)
                VALUES (?, 'Cliente Isolamento', 'isolamento@exemplo.com', '1990-01-01', 'ATIVO', ?, ?)
                """, CLIENTE_ID, INSTANTE, INSTANTE);
        jdbcTemplate.update("""
                INSERT INTO contratacao
                    (id, cliente_id, plano_assistencia_id, status, criada_em, ativada_em, versao)
                VALUES (?, ?, ?, 'ATIVA', ?, ?, 0)
                """, CONTRATACAO_ID, CLIENTE_ID, PLANO_ID, INSTANTE, INSTANTE);
        jdbcTemplate.update("""
                INSERT INTO solicitacao_assistencia
                    (id, contratacao_id, tipo_assistencia, descricao_problema, status, aberta_em, versao)
                VALUES (?, ?, 'ELETRICISTA', 'Cenario de isolamento', 'ABERTA', ?, 0)
                """, SOLICITACAO_ID, CONTRATACAO_ID, INSTANTE);
        jdbcTemplate.update("""
                INSERT INTO historico_status
                    (id, tipo_entidade, entidade_id, status_novo, tipo_responsavel, registrado_em)
                VALUES (?, 'SOLICITACAO_ASSISTENCIA', ?, 'ABERTA', 'CLIENTE', ?)
                """, HISTORICO_ID, SOLICITACAO_ID, INSTANTE);
    }

    private int quantidade(String tabela) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tabela, Integer.class);
    }
}
