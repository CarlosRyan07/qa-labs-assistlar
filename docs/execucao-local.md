# Execução local do AssistLar

## Pré-requisitos

- JDK 21;
- Docker configurado para containers Linux;
- Git.

O Maven Enforcer exige o JDK 21. Confirme se `JAVA_HOME` e o Maven Wrapper apontam para a versão correta antes de iniciar.

Windows PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\mvnw.cmd --version
```

Linux ou macOS:

```bash
export JAVA_HOME=/caminho/para/jdk-21
./mvnw --version
```

A saída do Maven Wrapper deve informar `Java version: 21`. Configure também o JDK 21 como SDK do projeto na IDE.

## Opção 1 — aplicação e PostgreSQL com Docker Compose

Inicie a aplicação e o PostgreSQL em containers:

```bash
docker compose up --build --wait
```

O Compose constrói a imagem do AssistLar, inicia os dois serviços e aguarda os health checks. Confirme que os containers estão saudáveis com:

```bash
docker compose ps
```

Depois da validação, encerre o ambiente:

```bash
docker compose down
```

## Opção 2 — PostgreSQL no Docker e aplicação local

Inicie somente o PostgreSQL:

```bash
docker compose up -d postgres --wait
```

Execute a aplicação pelo Maven:

```bash
./mvnw spring-boot:run
```

No Windows, use:

```powershell
.\mvnw.cmd spring-boot:run
```

Para executar pela IDE, configure o projeto com o JDK 21 e inicie a classe `AssistLarApplication` pelo comando de execução da própria IDE. Mantenha o PostgreSQL disponível enquanto a aplicação estiver em execução.

## Endereços locais

Com a aplicação iniciada:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- contrato OpenAPI: <http://localhost:8080/v3/api-docs>
- Actuator Health: <http://localhost:8080/actuator/health>

O endpoint de health deve responder com o estado `UP`. Swagger e OpenAPI são endereços locais e não representam uma implantação pública da aplicação.

## Reinício automático durante o desenvolvimento

O Spring Boot DevTools reinicia o contexto da aplicação quando uma alteração compilada chega ao classpath. Ao executar pela IDE, mantenha o PostgreSQL ligado, salve a alteração e deixe a IDE recompilar o projeto. O console deverá mostrar uma nova inicialização do `AssistLarApplication`, sem exigir Stop/Play manual.

Se salvar o arquivo não provocar o reinício, habilite a compilação automática da IDE. Alterações feitas somente em testes podem não reiniciar a aplicação.

Migrations já aplicadas não devem ser editadas. Novas alterações na estrutura ou nos dados de referência devem ser feitas em uma nova migration.

## Solução de problemas básicos

### JDK diferente de 21

Execute o Maven Wrapper com `--version` e confirme `Java version: 21`. Se necessário, ajuste `JAVA_HOME` e o SDK configurado na IDE.

### Docker indisponível

Confirme que o Docker está iniciado e configurado para containers Linux:

```bash
docker version
docker compose version
```

### PostgreSQL ainda não saudável

Confira o estado do serviço:

```bash
docker compose ps
```

Aguarde o PostgreSQL ficar `healthy` antes de iniciar a aplicação local.

### Porta 8080 ocupada

Encerre a aplicação ou o processo local que já utiliza a porta 8080 antes de iniciar o AssistLar.

### Aplicação não reinicia após uma alteração

Confirme que a IDE recompilou o código e que a compilação automática está habilitada. Alterações restritas aos testes podem não acionar o DevTools.

### Migration já aplicada foi modificada

Não edite uma migration que já foi aplicada. Preserve o arquivo existente e crie uma nova migration para a mudança necessária.

## Encerramento do ambiente

Interrompa a aplicação executada pelo Maven ou pela IDE e encerre os containers do projeto com:

```bash
docker compose down
```

O comando não utiliza opções de remoção de volumes ou outros comandos destrutivos.
