# Ryan QA Labs — AssistLar

[![CI](https://github.com/CarlosRyan07/qa-labs-assistlar/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/CarlosRyan07/qa-labs-assistlar/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/CarlosRyan07/qa-labs-assistlar?label=release)](https://github.com/CarlosRyan07/qa-labs-assistlar/releases/latest)
![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot 4.1.0](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL 17](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
[![Licença MIT](https://img.shields.io/github/license/CarlosRyan07/qa-labs-assistlar)](LICENSE)

Plataforma fictícia de assistências residenciais desenvolvida para demonstrar práticas de Quality Engineering em uma aplicação autoral, controlada e próxima de um produto real.

> Este projeto é exclusivamente educacional. Não representa nem reproduz sistemas, dados, nomes ou regras de empresas reais.

## Resultados em números

- **88 testes automatizados no backend**
- **55 testes rápidos e 33 de integração/API**
- **61 testes Vitest e 3 testes Cypress no frontend**
- **9 testes Playwright aprovados na matriz E2E completa**
- **97,31% de cobertura de instruções no backend**
- **98,91% de cobertura de branches no backend**
- **87,62% de statements e 86,24% de branches no frontend**
- **31 requisições na collection Postman**
- **18 caminhos documentados no OpenAPI 3.1**
- **204 requisições no workload k6, sem falhas ou iterações descartadas**
- **Release atual: v0.2.0**

## Acesso rápido

- [Releases do projeto](https://github.com/CarlosRyan07/qa-labs-assistlar/releases)
- [Evidências reproduzíveis](docs/evidencias.md)
- [Estratégia de testes](#estratégia-de-testes) e [catálogo de cenários](docs/cenarios-de-teste.md)
- [Cenários BDD documentados](docs/cenarios-bdd.md)
- [Arquitetura do AssistLar](docs/arquitetura.md)
- [Guia de execução local](docs/execucao-local.md)
- [Estratégia de performance com k6](performance-tests/README.md)
- [Collection Postman](postman/README.md) — requer a aplicação local em execução para enviar as requisições
- [Swagger UI](http://localhost:8080/swagger-ui.html) e [contrato OpenAPI](http://localhost:8080/v3/api-docs) — disponíveis somente com a aplicação local em execução

## O que o AssistLar demonstra

- regras de negócio testáveis e estados com transições explícitas;
- testes unitários, de controller, integração, API, banco e concorrência;
- PostgreSQL real nos testes com Testcontainers, sem H2;
- migrations versionadas e validadas com Flyway;
- erros REST consistentes com `ProblemDetail`;
- contrato OpenAPI verificado automaticamente;
- quality gate de cobertura com JaCoCo;
- ambiente reproduzível com Docker Compose.
- interface React com testes de unidade, componentes, E2E e regressão visual;
- estratégia de performance com smoke test, workloads simultâneos e thresholds por endpoint no k6.

## Domínio do MVP

O AssistLar permite cadastrar clientes, consultar planos, avaliar elegibilidade, criar e ativar contratações e solicitar serviços de eletricista, encanador ou chaveiro.

Os planos são carregados por migration:

| Plano | Eletricista | Encanador | Chaveiro |
|---|---:|---:|---:|
| ESSENCIAL | 1 | 1 | Não coberto |
| COMPLETO | 2 | 2 | 1 |

Regras centrais:

- cliente precisa ter entre 18 e 120 anos para ser cadastrado;
- nome do cliente deve ter entre 3 e 120 caracteres úteis;
- data de nascimento futura ou idade superior a 120 anos é inválida;
- só pode existir uma contratação `PENDENTE` ou `ATIVA` por cliente;
- contratação nasce `PENDENTE` e é ativada pelo operador;
- solicitação exige contratação ativa, cobertura e limite disponível;
- uma solicitação `EM_ATENDIMENTO` exige motivo para ser cancelada;
- solicitações `ABERTA`, `EM_ATENDIMENTO` e `CONCLUIDA` consomem limite; `CANCELADA` libera o consumo;
- mudanças de estado geram histórico na mesma transação.

## Arquitetura

Monólito modular em um único módulo Maven, com camadas usadas somente quando necessárias.

```mermaid
flowchart LR
    API[API REST] --> APP[Casos de uso]
    APP --> DOM[Domínio]
    APP --> INFRA[Persistência]
    INFRA --> PG[(PostgreSQL)]
    APP --> HIST[Histórico de status]
```

Pacote-base: `br.com.ryanqalabs.assistlar`.

Módulos: `cliente`, `plano`, `elegibilidade`, `contratacao`, `solicitacao`, `historico` e `compartilhado`. Veja [a documentação de arquitetura](docs/arquitetura.md).

### Estrutura do repositório

```text
📦 qa-labs-assistlar/
├── 📁 .github/workflows/             # Pipeline de CI com quality gates
├── 📁 docs/                          # Arquitetura, ADRs, cenários e evidências
│   ├── 📁 adr/                       # Decisões arquiteturais registradas
│   ├── 📁 architecture/              # Planejamento técnico da evolução Web
│   └── 📁 assets/                    # Evidências visuais selecionadas
├── 📁 frontend/                      # Aplicação React e automação Web
│   ├── 📁 e2e/                       # Jornadas e regressão visual Playwright
│   │   └── 📄 compose.e2e.yml        # Ambiente efêmero dos testes E2E
│   └── 📁 src/
│       ├── 📁 app/                   # Composição, rotas e layout
│       ├── 📁 features/              # Clientes, planos, contratações e solicitações
│       ├── 📁 shared/                # API, componentes e utilitários compartilhados
│       └── 📁 test/                  # Configuração e suporte dos testes rápidos
├── 📁 performance-tests/
│   └── 📁 k6/                        # Smoke test e workload de carga
├── 📁 postman/                       # Collection, ambiente e guia de execução
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/br/com/ryanqalabs/assistlar/
│   │   │   ├── 📁 cliente/           # Cadastro e ciclo de vida
│   │   │   ├── 📁 plano/             # Planos e coberturas
│   │   │   ├── 📁 elegibilidade/     # Regras para contratação
│   │   │   ├── 📁 contratacao/       # Adesão e transições
│   │   │   ├── 📁 solicitacao/       # Assistências, limites e concorrência
│   │   │   ├── 📁 historico/         # Registro transacional de estados
│   │   │   └── 📁 compartilhado/     # Erros e contratos comuns
│   │   └── 📁 resources/db/migration # Migrations Flyway
│   └── 📁 test/java/                 # JUnit, MockMvc, REST Assured e Testcontainers
├── 📄 compose.yaml                   # Ambiente local da aplicação
├── 📄 Dockerfile                     # Build multi-stage do backend
└── 📄 pom.xml                        # Build, dependências e quality gate Java
```

## Stack

- Java 21 e Spring Boot 4.1.0;
- Maven Wrapper;
- PostgreSQL `17.10-alpine3.24`;
- Flyway e Spring Data JPA;
- Springdoc OpenAPI 3.0.3;
- JUnit 5, Mockito e MockMvc;
- Testcontainers 2.0.5 e REST Assured 6.0.0;
- JaCoCo e Maven Enforcer;
- GitHub Actions;
- Docker e Docker Compose.

### Evolução Web v0.2.0

A aplicação Web fica em [`frontend/`](frontend/README.md) e preserva o contrato
existente do backend como fonte da verdade. Ela usa React, TypeScript, Vite,
Material UI, React Router e TanStack Query, com Vitest e Testing Library para
feedback rápido, Cypress Component Testing e Playwright para jornadas E2E Full
Stack.

Os comandos e critérios estão documentados no [guia do frontend](frontend/README.md).
O Playwright executa uma jornada dourada e um cenário negativo contra Spring
Boot e PostgreSQL reais em um Compose efêmero, removido ao final da execução.

## Pré-requisitos

- JDK 21;
- Docker com containers Linux;
- Git.

O build aceita exclusivamente o JDK 21. Confirme a versão com `./mvnw --version` ou `.\mvnw.cmd --version` no Windows.

## Execução

Aplicação e PostgreSQL em containers:

```bash
docker compose up --build --wait
```

Somente PostgreSQL no Docker e aplicação pela IDE ou Maven:

```bash
docker compose up -d postgres --wait
./mvnw spring-boot:run
```

No Windows, use `.\mvnw.cmd` no lugar de `./mvnw`.

Com a aplicação iniciada:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI: <http://localhost:8080/v3/api-docs> — contrato JSON da API, usado pelo Swagger, por ferramentas e por testes de contrato;
- Health: <http://localhost:8080/actuator/health> — indicador operacional que informa se a aplicação e suas dependências estão disponíveis.

Para encerrar os containers:

```bash
docker compose down
```

Consulte o [guia de execução local](docs/execucao-local.md) para configuração da IDE, reinício automático com DevTools e solução de problemas.

As credenciais do Compose são apenas locais e não devem ser usadas em outro ambiente.

## API

| Recurso | Operações principais |
|---|---|
| `/api/clientes` | cadastrar, consultar, inativar e reativar |
| `/api/planos` | listar e consultar |
| `/api/elegibilidades` | avaliar cliente e plano sem persistir resultado |
| `/api/contratacoes` | criar, consultar, ativar, cancelar e consultar histórico |
| `/api/solicitacoes-assistencia` | abrir, consultar, iniciar, concluir, cancelar e consultar histórico |

Criações retornam `201` e `Location`. Erros seguem `application/problem+json`: `400` para entrada inválida, `404` para recurso inexistente, `409` para conflito e `422` para regra de negócio.

No cadastro de clientes:

| Situação | Status | Identificador do problema |
|---|---:|---|
| nome, e-mail, formato ou data inválida | `400` | `/erros/dados-invalidos` ou código específico da data |
| e-mail já cadastrado | `409` | `/erros/email-ja-cadastrado` |
| cliente menor de 18 anos | `422` | `/erros/idade-minima-nao-atendida` |

Erros de validação informam o campo e a mensagem no array `erros`. Exemplo: `{"campo":"nome","mensagem":"O nome deve ter entre 3 e 120 caracteres."}`.

O MVP não possui autenticação. UUID é identificador, nunca mecanismo de autorização. Uma [jornada manual reproduzível](docs/jornada-principal.md) complementa o Swagger.

### Postman

A pasta [`postman`](postman/README.md) contém uma collection importável com 31 requisições, ambiente local, captura automática de UUIDs e verificações de status e regras de negócio. Ela cobre a jornada principal e cenários negativos sem exigir cópia manual dos identificadores.

## Estratégia de testes

A suíte foi organizada em camadas para equilibrar feedback rápido, fidelidade ao ambiente real e cobertura dos riscos de negócio.

| Camada | Ferramentas | O que é validado |
|---|---|---|
| Domínio e regras | JUnit 5 e AssertJ | Idade, elegibilidade, cobertura, limites e transições de estado |
| Controllers | `@WebMvcTest`, MockMvc e Mockito | Rotas, payloads, status HTTP, headers, validações e respostas `ProblemDetail`, sem iniciar servidor real |
| Integração | Spring Boot Test, Testcontainers, PostgreSQL e Flyway | Mapeamentos JPA, migrations, constraints, transações, histórico e persistência real |
| API | REST Assured e `@SpringBootTest` em porta aleatória | Jornadas HTTP completas atravessando controller, aplicação, domínio e banco |
| Concorrência | JUnit 5, `CountDownLatch` e PostgreSQL | Unicidade, locking, conflitos simultâneos e consistência final do banco |
| Contrato | REST Assured e Springdoc OpenAPI | Disponibilidade do contrato OpenAPI 3.1 e presença dos caminhos públicos esperados |
| Cobertura | JaCoCo | Quality gate de instruções e branches no código relevante |
| Performance | k6 | Smoke test, carga simultânea, percentis, taxa de erros e iterações descartadas |
| Testes manuais | Postman e Swagger UI | Exploração reproduzível da API, fluxos positivos e respostas negativas |

Decisões da estratégia:

- MockMvc testa rapidamente a camada HTTP; os casos de uso chamados pelos controllers são isolados com `@MockitoBean`;
- REST Assured é reservado às jornadas críticas e aos testes de contrato, evitando duplicar toda a suíte do MockMvc;
- os testes de integração utilizam a mesma imagem PostgreSQL `postgres:17.10-alpine3.24` adotada no Docker Compose;
- H2 não é utilizado, reduzindo diferenças entre o comportamento dos testes e o banco da aplicação;
- entidades e regras de domínio são exercitadas diretamente, sem mocks desnecessários;
- antes de cada teste de integração, apenas os dados mutáveis são limpos; migrations, planos e coberturas de referência são preservados;
- testes concorrentes usam barreiras determinísticas e timeout, nunca `Thread.sleep`;
- Surefire executa os 55 testes rápidos, enquanto Failsafe complementa a execução com 33 testes de integração/API.

## Mapeamento da suíte de testes

Os identificadores abaixo representam riscos e comportamentos verificáveis, não
uma relação de um ID para cada método automatizado. O detalhamento está no
[catálogo de cenários](docs/cenarios-de-teste.md) e nos
[cenários BDD](docs/cenarios-bdd.md).

| IDs | Área ou objetivo | Tipos principais | Status |
|---|---|---|---|
| `CLI-01`–`CLI-10` | cadastro, idade, nome, e-mail, estado, busca e paginação | unitário e API | ✅ Aprovado |
| `ELE-01`–`ELE-05` | elegibilidade e ausência de efeito colateral | unitário e API | ✅ Aprovado |
| `CON-01`–`CON-08` | contratação, transições, histórico e concorrência | unitário, API e banco | ✅ Aprovado |
| `SOL-01`–`SOL-13` | cobertura, limites, cancelamento e concorrência | unitário, API e banco | ✅ Aprovado |
| `API-01`–`API-03` | OpenAPI, payloads e segurança das respostas de erro | contrato e API | ✅ Aprovado |
| `OPS-01`–`OPS-03` | Flyway, health check e containers | integração e operação | ✅ Aprovado |
| `BDD-CLI/ELE/CON/SOL` | sete cenários críticos escritos em Gherkin | documentação BDD vinculada à automação | ✅ Coberto |
| `WEB-UNIT` | 61 verificações de componentes, hooks e páginas | Vitest e Testing Library | ✅ Aprovado |
| `CMP-01`–`CMP-03` | foco, validação e cancelamento na interface | Cypress Component | ✅ Aprovado |
| `E2E-01`–`E2E-03` | jornada principal, regra negativa e regressão visual | Playwright Full Stack | ✅ Aprovado |
| `PERF-01`–`PERF-03` | smoke, carga simultânea e thresholds | k6 | ✅ Aprovado |

## Testes e quality gate

```bash
./mvnw test
./mvnw verify
```

### Convenção de nomes do Maven

O projeto usa a convenção do Maven para separar testes rápidos de testes que dependem de infraestrutura:

| Sufixo da classe | Executor Maven | Finalidade | Exemplos |
|---|---|---|---|
| `*Test` | Surefire | Testes unitários, de domínio e da camada HTTP rápida com MockMvc | `ClienteTest`, `ClienteControllerTest` |
| `*IT` | Failsafe | Testes de integração com banco, migrations e concorrência | `MigracoesBancoIT`, `ContratacaoConcorrenciaIT` |
| `*ApiIT` | Failsafe | Especialização de `*IT` para jornadas pela API REST | `ClienteApiIT`, `OpenApiApiIT` |

`ApiIT` não é um padrão separado do Maven: essas classes também terminam em `IT` e, por isso, são encontradas pelo Failsafe. O nome adicional apenas deixa explícito que o teste atravessa a interface HTTP.

- `mvn test`: o Surefire executa somente a suíte rápida `*Test`;
- `mvn verify`: executa os `*Test` pelo Surefire e depois os `*IT`/`*ApiIT` pelo Failsafe, usando PostgreSQL real iniciado pelo Testcontainers;
- essa separação permite obter feedback rápido durante o desenvolvimento e ainda manter uma validação completa antes de commits e entregas;
- JaCoCo: mínimo de 80% de instruções e 70% de branches no código relevante;
- relatório local: `target/site/jacoco/index.html` após `verify`.

Exemplo de diagnóstico de uma integração específica no PowerShell:

```powershell
.\mvnw.cmd test-compile "-Dit.test=SolicitacaoConcorrenciaIT" failsafe:integration-test failsafe:verify
```

A execução específica não substitui o `mvn verify` obrigatório. Consulte o [catálogo de cenários](docs/cenarios-de-teste.md) e as [evidências reproduzíveis](docs/evidencias.md).

## Integração contínua

A pipeline valida Pull Requests direcionados à `main` e executa novamente após o merge, mantendo a branch principal verificada. Ela também executa em novos pushes na `main` e pode ser acionada manualmente pelo GitHub Actions. O job utiliza Java 21 Temurin e o Maven Wrapper para executar `clean verify` em um runner Linux com Docker disponível.

Durante a validação, o PostgreSQL real é iniciado pelo Testcontainers, sem service container ou banco alternativo. O Maven executa os testes rápidos pelo Surefire, os testes de integração e API pelo Failsafe e o quality gate de cobertura pelo JaCoCo.

Ao final de cada execução, mesmo em caso de falha, os relatórios do Surefire, Failsafe e JaCoCo são disponibilizados no artefato `quality-reports` por 14 dias. A configuração está em [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

![Pipeline de qualidade aprovada no Pull Request e na branch main](docs/assets/github-actions-ci-aprovada.png)

## Evidências do MVP

### API documentada

O contrato OpenAPI 3.1 publica 18 caminhos para clientes, planos, elegibilidade, contratações, solicitações e históricos.

![Swagger UI com os recursos do AssistLar](docs/assets/swagger-api.png)

### Testes manuais e de API

A collection Postman possui 31 requisições, com jornadas positivas, cenários negativos, variáveis dinâmicas e scripts de validação.

![Jornada principal executada no Postman](docs/assets/postman-jornada-principal.png)

![Resposta ProblemDetail validada no Postman](docs/assets/postman-problem-detail.png)

### Suíte automatizada

A suíte completa do backend possui 88 testes: 55 rápidos e 33 de integração/API.

### Cobertura

Na validação da v0.2.0, o JaCoCo registrou 97,31% de instruções e 98,91% de branches, acima do quality gate configurado (80% e 70%, respectivamente).

![Relatório JaCoCo da validação v0.2.0](docs/assets/jacoco-cobertura-v020.png)

### Interface Web

A interface React consome o backend real e apresenta planos, clientes,
contratações e solicitações sem dados simulados.

![Tela inicial do frontend AssistLar v0.2.0](docs/assets/frontend-tela-inicial-v020.png)

### Qualidade do frontend

O Vitest registrou 61 testes aprovados e cobertura acima dos limites definidos
para statements, branches, functions e lines.

![Relatório Vitest da validação v0.2.0](docs/assets/vitest-cobertura-v020.png)

### Jornada E2E com Playwright

O registro abaixo mostra a jornada aprovada no Playwright UI, contra o
ambiente E2E isolado com Spring Boot e PostgreSQL reais. Ele evidencia a
execução automatizada de cadastro, elegibilidade, ativação de contratação e
conclusão da assistência.

![Jornada E2E do AssistLar executada pelo Playwright](docs/assets/playwright-jornada-v020.png)

### Performance com k6

O smoke test validou disponibilidade e contrato antes da carga. Em seguida, o
workload realizou 204 requisições simultâneas sobre health, planos e clientes,
com 0% de falhas, 100% dos checks aprovados e nenhuma iteração descartada.
Todos os thresholds por endpoint foram atendidos.

![Dashboard da execução de carga do AssistLar com k6](docs/assets/k6-carga-v020.png)

A imagem foi exportada pelo dashboard nativo do k6 1.2.3. Os comandos para
reproduzir o relatório estão na [estratégia de performance](performance-tests/README.md).

### Ambiente reproduzível

A aplicação e o PostgreSQL são iniciados pelo Docker Compose com health checks.

![Aplicação e PostgreSQL saudáveis](docs/assets/docker-compose-healthy.png)

Os comandos, critérios e resultados completos estão nas [evidências reproduzíveis](docs/evidencias.md).

## Decisões de qualidade

- datas civis usam `Clock` e `America/Sao_Paulo`;
- timestamps técnicos usam `Instant` e persistência UTC;
- transições usam optimistic locking;
- conferência de limite usa bloqueio pessimista da contratação;
- constraints parciais protegem invariantes também sob concorrência;
- testes concorrentes usam barreiras e timeout, nunca espera arbitrária;
- `tipoResponsavel` é definido pelo caso de uso e rejeitado nos payloads.

## Limites atuais do produto

- autenticação e autorização;
- testes especializados de acessibilidade;
- notificações e integrações externas;
- rede de prestadores, geolocalização e agendamento;
- pagamentos, sinistros, corretor e vigência;
- implantação em cloud e arquitetura de microsserviços.

Próximas evoluções incluem automação especializada de acessibilidade,
segurança básica e a evolução da estratégia de performance com novos workloads
de negócio. Os cenários atuais de smoke e carga controlada com k6 estão em
[`performance-tests/`](performance-tests/).

## Repositório e licença

O AssistLar é o primeiro projeto público do **Ryan QA Labs**, iniciativa criada para transformar experiência profissional em projetos autorais, reproduzíveis e documentados.

Distribuído sob a [licença MIT](LICENSE).
