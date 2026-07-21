# Arquitetura do AssistLar

## Visão geral

O AssistLar é um monólito modular. A escolha reduz custo operacional para uma pessoa, mantém transações locais e ainda permite demonstrar separação de responsabilidades.

Cada módulo usa apenas as camadas necessárias:

- `api`: controllers e DTOs de entrada e saída;
- `aplicacao`: orquestração transacional dos casos de uso;
- `dominio`: entidades, enums e regras de estado;
- `infraestrutura`: repositories e consultas específicas.

DTOs não são entidades JPA e o mapeamento é manual. Controllers não executam regras de negócio.

## Módulos

| Módulo | Responsabilidade |
|---|---|
| `cliente` | cadastro, consulta e estado do cliente |
| `plano` | leitura de planos e coberturas criados por migration |
| `elegibilidade` | avaliação não persistida e motivos acumuláveis |
| `contratacao` | adesão, ativação, cancelamento e unicidade vigente |
| `solicitacao` | atendimento, cobertura, limite e transições |
| `historico` | registro simples e append-only das mudanças de estado |
| `compartilhado` | tempo, erros e configuração transversal mínima |

## Persistência

- UUID nativo do PostgreSQL é o único identificador.
- Enums são persistidos como texto.
- Tabelas e colunas usam `snake_case`.
- Flyway é a única fonte da estrutura e da carga de planos.
- Hibernate usa `ddl-auto=validate`.
- Timestamps são `TIMESTAMPTZ` e o acesso Hibernate usa UTC.

## Consistência e concorrência

As validações na aplicação oferecem mensagens de negócio; o banco mantém a defesa final:

- índice único parcial impede mais de uma contratação `PENDENTE/ATIVA` por cliente;
- índice único parcial impede duas solicitações `ABERTA/EM_ATENDIMENTO` do mesmo tipo e contratação;
- `@Version` detecta transições simultâneas;
- a abertura de solicitação bloqueia a contratação antes de contar o consumo de limite.

Não existe retry automático. O conflito permanece visível para a API e para os testes.

## Tempo

Regras de idade recebem `Clock` e calculam a data civil em `America/Sao_Paulo`. Timestamps técnicos usam `Instant`. O fuso global da JVM não é alterado como mecanismo de negócio.

## Segurança do MVP

Não há autenticação nesta versão. UUID não concede acesso. Payloads desconhecidos são rejeitados e `tipoResponsavel` nunca integra DTOs de entrada. Respostas de erro não contêm stack trace, SQL ou classes internas.
