# Ryan QA Labs — AssistLar

Plataforma ficticia de assistencias residenciais criada para aprender e demonstrar praticas de Quality Engineering.

> Projeto autoral e educacional. Nao representa nem reproduz sistemas, dados ou regras de empresas reais.

## Requisitos

- JDK 21
- Docker com suporte a containers Linux
- Git

O projeto exige exatamente o JDK 21. Confirme antes de executar:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
./mvnw.cmd --version
```

Em Linux ou macOS:

```bash
export JAVA_HOME=/caminho/para/jdk-21
./mvnw --version
```

A saida deve informar `Java version: 21`. Configure a mesma JDK na IDE.

## Comandos

```bash
./mvnw test
./mvnw verify
./mvnw spring-boot:run
```

No Windows, substitua `./mvnw` por `./mvnw.cmd`.

Com a aplicacao iniciada:

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

## Escopo do MVP

O MVP cobre clientes, planos de assistencia, elegibilidade, contratacoes, solicitacoes, limites de utilizacao e historico de estados. Autenticacao, frontend, performance e integracoes externas pertencem a evolucoes futuras.

## Licenca

Distribuido sob a licenca MIT.
