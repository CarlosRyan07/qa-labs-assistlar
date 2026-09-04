# Estratégia de performance do AssistLar

Os testes desta pasta usam k6 para avaliar a disponibilidade e o tempo de
resposta das consultas mais frequentes do AssistLar sob carga local controlada.
Eles não representam um SLA de produção nem um teste de capacidade máxima.

## Fluxo recomendado

1. Execute o smoke test para confirmar que ambiente e contrato estão íntegros.
2. Execute o workload de carga somente após o smoke passar.
3. Compare erros, percentis e iterações descartadas com os thresholds.
4. Registre como evidência somente resultados obtidos em ambiente identificado.

| Script | Objetivo | Perfil |
|---|---|---|
| `smoke-assistlar.js` | detectar indisponibilidade ou quebra básica de contrato | 1 VU, 3 iterações |
| `carga-assistlar.js` | observar consultas simultâneas de health, planos e clientes | até 5 VUs em rampa e 2 req/s na listagem |

Os cenários são somente de leitura. Assim, as execuções são repetíveis e não
criam clientes, contratações ou solicitações no banco.

## Thresholds

Os valores são objetivos iniciais para o ambiente local, escolhidos para
detectar regressões evidentes sem apresentá-los como capacidade de produção:

| Indicador | Limite | Motivo |
|---|---:|---|
| requisições com falha | menor que 1% na carga; zero no smoke | preservar estabilidade funcional |
| checks aprovados | maior que 99% na carga; 100% no smoke | validar status e estrutura mínima |
| health p95 | menor que 250 ms | endpoint simples de disponibilidade |
| planos p95 / p99 | menor que 500 ms / 1 s | consulta pequena e estável |
| clientes p95 / p99 | menor que 750 ms / 1,2 s | consulta paginada com acesso ao banco |
| iterações descartadas | zero | confirmar que a taxa solicitada foi sustentada |

Um threshold reprovado encerra o k6 com código diferente de zero. Percentis
devem ser comparados entre execuções equivalentes; um resultado isolado não
prova escalabilidade.

## Pré-requisitos

- aplicação AssistLar disponível em `http://localhost:8080`;
- PostgreSQL disponível para a aplicação;
- k6 local ou Docker Desktop com o Engine ativo.

## Executar com k6 instalado

Na raiz do repositório, em PowerShell:

```powershell
& "$env:LOCALAPPDATA\Programs\k6\k6.exe" run performance-tests\k6\smoke-assistlar.js
& "$env:LOCALAPPDATA\Programs\k6\k6.exe" run performance-tests\k6\carga-assistlar.js
```

Se `k6` estiver no `PATH`, os mesmos testes podem ser executados com:

```powershell
k6 run performance-tests\k6\smoke-assistlar.js
k6 run performance-tests\k6\carga-assistlar.js
```

## Executar com Docker

Com a aplicação já iniciada, execute no Windows:

```powershell
docker run --rm -i `
  --add-host=host.docker.internal:host-gateway `
  -e BASE_URL=http://host.docker.internal:8080 `
  -v "${PWD}\performance-tests:/scripts:ro" `
  grafana/k6:0.55.2 run /scripts/k6/smoke-assistlar.js

docker run --rm -i `
  --add-host=host.docker.internal:host-gateway `
  -e BASE_URL=http://host.docker.internal:8080 `
  -v "${PWD}\performance-tests:/scripts:ro" `
  grafana/k6:0.55.2 run /scripts/k6/carga-assistlar.js
```

No Linux ou macOS, use o mesmo volume com
`-v "$PWD/performance-tests:/scripts:ro"`.

## Outro ambiente controlado

A variável `BASE_URL` troca o destino sem modificar o script:

```powershell
$env:BASE_URL = "http://localhost:8080"
k6 run performance-tests\k6\smoke-assistlar.js
Remove-Item Env:BASE_URL
```

Não execute esses workloads contra produção sem autorização, janela de teste,
limites acordados e observabilidade suficiente.

## Como interpretar a saída

- `http_req_failed`: proporção de requisições HTTP com erro;
- `http_req_duration`: tempo total da requisição, observado em percentis;
- `checks`: proporção das validações funcionais aprovadas;
- `dropped_iterations`: iterações que o k6 não conseguiu iniciar na taxa pedida;
- `vus` e `iterations`: concorrência utilizada e trabalho concluído.

Relatórios brutos devem ser gravados em `performance-tests/results/`, pasta
ignorada pelo Git. A versão do k6 e as condições do ambiente devem acompanhar
qualquer evidência publicada.
