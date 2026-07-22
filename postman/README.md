# Postman — AssistLar

Esta pasta contém uma collection Postman 2.1 pronta para explorar e validar o MVP.

## Arquivos

- `AssistLar.postman_collection.json`: 31 requisições com verificações automatizadas;
- `AssistLar.local.postman_environment.json`: ambiente local com a variável `baseUrl`.

Nenhum arquivo contém credenciais, tokens ou dados pessoais.

## Preparar o ambiente

Inicie o PostgreSQL e execute a aplicação pelo Play da IDE:

```powershell
docker compose up -d postgres --wait
```

Confirme que o health responde `UP` em <http://localhost:8080/actuator/health>.

## Importar no Postman

1. Clique em **Import**.
2. Importe os dois arquivos desta pasta.
3. Selecione o ambiente **AssistLar - Local** no canto superior direito.
4. Abra a collection **Ryan QA Labs — AssistLar**.

A variável `baseUrl` também possui valor padrão na própria collection; o ambiente permite sobrescrevê-la sem editar as requisições.

## Executar

- **00 - Operacional** verifica health e contrato OpenAPI.
- **01 - Jornada principal** deve ser executada na ordem numérica.
- **02 - Cenários negativos de cliente** pode ser executada depois ou isoladamente.

No Collection Runner, execute a collection inteira na ordem apresentada. A jornada:

1. descobre os UUIDs dos planos pelo campo `codigo`;
2. cria um e-mail único e cadastra o cliente;
3. consulta, inativa, reativa e avalia elegibilidade;
4. cria e ativa uma contratação;
5. testa transição inválida e históricos;
6. cancela solicitações aberta e em atendimento;
7. conclui uma solicitação;
8. cancela a contratação;
9. valida idade mínima, nome curto e e-mail inválido.

Os scripts da aba **Tests** verificam status e regras de negócio. Os UUIDs retornados são armazenados automaticamente como variáveis da collection, evitando cópia manual.

Cada execução cria um cliente com e-mail único. Os dados válidos permanecem no banco local para inspeção no DBeaver; a contratação termina cancelada e não bloqueia uma nova execução.

## Execução isolada

Requisições que usam `clienteId`, `planoId`, `contratacaoId` ou `solicitacaoId` dependem das etapas anteriores. Para executá-las isoladamente, informe essas variáveis manualmente ou execute primeiro a jornada até a etapa que as cria.

O contrato OpenAPI continua sendo a fonte técnica da API. A collection representa jornadas selecionadas para exploração manual e demonstração do portfólio.
