# Evidências reproduzíveis

Evidências não são relatórios binários versionados. Elas são comandos e resultados verificáveis por qualquer pessoa após clonar o projeto.

## Ambiente

```bash
./mvnw --version
docker version
docker compose version
```

Critério: Maven Wrapper usando Java 21 e Docker disponível.

Baseline local de 20/07/2026: JDK 21 confirmado, Docker Engine 24.0.6 e Docker Compose 2.23.0.

## Suíte rápida

```bash
./mvnw test
```

Critério: todos os testes `*Test` aprovados.

Baseline local: 48 testes rápidos aprovados.

## Suíte completa

```bash
./mvnw verify
```

Critérios:

- testes unitários, HTTP, integração, API, banco e concorrência aprovados;
- PostgreSQL `postgres:17.10-alpine3.24` iniciado pelo Testcontainers;
- JaCoCo acima de 80% de instruções e 70% de branches;
- relatório disponível em `target/site/jacoco/index.html`.

Baseline local: 48 testes rápidos e 26 testes de integração/API aprovados, com 97,11% de instruções e 98,39% de branches cobertos.

## Container

```bash
docker compose up -d --build --wait
curl http://localhost:8080/actuator/health
curl http://localhost:8080/v3/api-docs
docker compose down
```

Critérios: serviços saudáveis, health `UP`, OpenAPI acessível e containers removidos após a validação.

Baseline local: aplicação e PostgreSQL ficaram `healthy`; health retornou `UP`, OpenAPI 3.1 publicou 18 caminhos, Swagger respondeu `200` e o Actuator não expôs `/info` (`404`).

## Migrations

`MigracoesBancoIT` inicia um banco vazio e verifica:

- três migrations aplicadas com sucesso;
- dois planos e cinco coberturas;
- ausência de cobertura `CHAVEIRO` no ESSENCIAL.

## Concorrência

- `ContratacaoConcorrenciaIT`: duas inserções simultâneas deixam uma única contratação vigente.
- `SolicitacaoConcorrenciaIT`: aberturas simultâneas respeitam o limite e inícios simultâneos geram uma única transição.

Os testes usam `CountDownLatch`, timeout e inspeção do estado final do banco, sem `sleep`.

## Jornada principal

A jornada documentada foi reproduzida no ambiente do Compose: cliente elegível, contratação `PENDENTE → ATIVA` e solicitação `ABERTA → EM_ATENDIMENTO → CONCLUIDA`, com os três registros no histórico.
