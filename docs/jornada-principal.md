# Jornada principal reproduzível

A forma mais simples de explorar a jornada é pelo Swagger UI em <http://localhost:8080/swagger-ui.html>. Os exemplos abaixo também funcionam com `curl`; substitua os UUIDs retornados a cada etapa.

## 1. Iniciar o ambiente

```bash
docker compose up --build --wait
```

## 2. Listar os planos

```bash
curl http://localhost:8080/api/planos
```

Use o UUID do plano cujo campo `codigo` seja `COMPLETO` nas etapas seguintes.

## 3. Cadastrar um cliente

```bash
curl -i -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Cliente Demonstracao","email":"cliente.demo@example.com","dataNascimento":"1990-05-10"}'
```

Guarde o `id` retornado.

Se repetir a jornada sem recriar o banco, use outro e-mail para não violar a unicidade esperada do cadastro.

## 4. Consultar a elegibilidade

```bash
curl "http://localhost:8080/api/elegibilidades?clienteId=UUID_CLIENTE&planoId=UUID_PLANO"
```

O resultado esperado é `elegivel=true`.

## 5. Criar e ativar a contratação

```bash
curl -i -X POST http://localhost:8080/api/contratacoes \
  -H "Content-Type: application/json" \
  -d '{"clienteId":"UUID_CLIENTE","planoId":"UUID_PLANO"}'

curl -X POST http://localhost:8080/api/contratacoes/UUID_CONTRATACAO/ativacao
```

## 6. Abrir, iniciar e concluir uma solicitação

```bash
curl -i -X POST http://localhost:8080/api/solicitacoes-assistencia \
  -H "Content-Type: application/json" \
  -d '{"contratacaoId":"UUID_CONTRATACAO","tipoAssistencia":"ELETRICISTA","descricaoProblema":"Tomada sem energia"}'

curl -X POST http://localhost:8080/api/solicitacoes-assistencia/UUID_SOLICITACAO/inicio
curl -X POST http://localhost:8080/api/solicitacoes-assistencia/UUID_SOLICITACAO/conclusao
```

## 7. Consultar o histórico

```bash
curl http://localhost:8080/api/solicitacoes-assistencia/UUID_SOLICITACAO/historico
```

O histórico deve conter `ABERTA`, `EM_ATENDIMENTO` e `CONCLUIDA`, com responsáveis definidos internamente.

## 8. Encerrar

```bash
docker compose down
```
