# Changelog

Todas as alterações relevantes deste projeto serão documentadas neste arquivo.

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
