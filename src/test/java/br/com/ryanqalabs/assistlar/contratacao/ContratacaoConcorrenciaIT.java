package br.com.ryanqalabs.assistlar.contratacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.cliente.infraestrutura.ClienteRepository;
import br.com.ryanqalabs.assistlar.suporte.PostgreSqlTestContainer;

@SpringBootTest
class ContratacaoConcorrenciaIT extends PostgreSqlTestContainer {

    private static final UUID PLANO_ESSENCIAL = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private Clock relogio;

    @Test
    void devePermitirSomenteUmaContratacaoVigenteEmInsercoesSimultaneas() throws Exception {
        Cliente cliente = clienteRepository.saveAndFlush(Cliente.cadastrar("Cliente Concorrente",
                UUID.randomUUID() + "@exemplo.com", LocalDate.of(1990, 1, 1), relogio));
        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<String> primeira = executor.submit(() -> inserirConcorrente(cliente.getId(), prontas, iniciar));
            Future<String> segunda = executor.submit(() -> inserirConcorrente(cliente.getId(), prontas, iniciar));

            assertThat(prontas.await(5, TimeUnit.SECONDS)).isTrue();
            iniciar.countDown();
            List<String> resultados = List.of(
                    primeira.get(10, TimeUnit.SECONDS),
                    segunda.get(10, TimeUnit.SECONDS));

            assertThat(resultados).containsExactlyInAnyOrder("SUCESSO", "CONFLITO");
        }

        Integer quantidade = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM contratacao
                WHERE cliente_id = ? AND status IN ('PENDENTE', 'ATIVA')
                """, Integer.class, cliente.getId());
        assertThat(quantidade).isEqualTo(1);
    }

    private String inserirConcorrente(UUID clienteId, CountDownLatch prontas, CountDownLatch iniciar)
            throws InterruptedException {
        prontas.countDown();
        if (!iniciar.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("As insercoes concorrentes nao foram liberadas a tempo.");
        }
        try {
            transactionTemplate.executeWithoutResult(status -> jdbcTemplate.update("""
                    INSERT INTO contratacao (
                        id, cliente_id, plano_assistencia_id, status, criada_em, versao
                    ) VALUES (?, ?, ?, 'PENDENTE', ?, 0)
                    """, UUID.randomUUID(), clienteId, PLANO_ESSENCIAL, OffsetDateTime.now(ZoneOffset.UTC)));
            return "SUCESSO";
        } catch (DataIntegrityViolationException excecao) {
            return "CONFLITO";
        }
    }
}
