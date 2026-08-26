# Changelog

Todas as alterações relevantes deste projeto serão documentadas neste arquivo.

## [Unreleased]

## [0.2.0] - 2026-08-26

### Adicionado

- aplicação Web v0.2.0 em jornada guiada sobre o contrato real do backend;
- listagens paginadas de clientes, contratações e solicitações para apoiar a jornada Web;
- cenários BDD documentados em Gherkin, vinculados à automação já existente;
- testes rápidos com Vitest e Testing Library;
- Cypress Component Testing para componentes de maior risco;
- Playwright E2E com jornada dourada, cenário negativo, cross-browser e perfil mobile;
- regressão visual no Chromium com baselines Windows e Linux versionadas;
- ambientes E2E efêmeros com Compose e PostgreSQL descartável;
- jobs de qualidade frontend e E2E no GitHub Actions, com artefatos de cobertura e evidências.

### Qualidade

- 84 testes automatizados no backend: 54 rápidos e 30 de integração/API;
- 61 testes Vitest aprovados;
- 3 testes Cypress Component aprovados;
- 9 testes Playwright aprovados na matriz completa, com 3 cenários visuais ignorados fora do Chromium;
- 90,75% de instruções e 72,83% de branches no backend;
- 87,62% de statements, 86,24% de branches, 80,70% de functions e 89,09% de lines no frontend;
- cenário baseline de carga leve com k6 para health e consulta de planos.

## [0.1.0] - 2026-07-23

### Adicionado

- API REST para clientes, planos, elegibilidade, contratações e solicitações;
- histórico transacional de mudanças de estado;
- contrato OpenAPI 3.1 e Swagger UI;
- ambiente reproduzível com Docker Compose;
- collection Postman com jornadas positivas e negativas;
- pipeline de integração contínua com GitHub Actions;
- evidências visuais e documentação técnica.

### Qualidade

- 51 testes rápidos;
- 30 testes de integração e API;
- 81 testes automatizados no total;
- testes com PostgreSQL real e Testcontainers;
- testes de concorrência determinísticos;
- quality gate com JaCoCo;
- 97,07% de cobertura de instruções;
- 95,71% de cobertura de branches.

### Limitações conhecidas

- não possui autenticação;
- não possui interface web;
- não possui integração com serviços externos;
- não está hospedado em ambiente cloud;
- não possui testes de interface ou performance nesta versão.
