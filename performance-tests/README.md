# Testes de carga do AssistLar

Os cenários desta pasta usam k6 para observar o comportamento dos endpoints de
consulta do AssistLar sob uma carga leve e controlada. O objetivo inicial é
aprender a modelar carga, thresholds e evidências sem transformar o ambiente
local em um teste destrutivo.

O cenário não cria clientes, contratações ou solicitações. Ele consulta apenas
o health check e a listagem de planos, evitando poluir o banco e mantendo a
execução repetível.

## Pré-requisitos

- Docker Desktop com o Engine ativo;
- Docker Compose;
- aplicação AssistLar iniciada pelo Compose.

O k6 é executado pela imagem oficial `grafana/k6:0.55.2`. Assim, não é
necessário instalar k6 globalmente no Windows, no Node.js ou no Maven.

## Executar localmente no Windows

Na raiz do repositório:

```powershell
docker compose up --build --wait

docker run --rm -i `
  --add-host=host.docker.internal:host-gateway `
  -e BASE_URL=http://host.docker.internal:8080 `
  -v "${PWD}\performance-tests:/scripts:ro" `
  grafana/k6:0.55.2 run /scripts/k6/carga-assistlar.js

docker compose down
```

O `host.docker.internal` permite que o container do k6 acesse a aplicação
publicada na máquina host pelo Docker Desktop.

## Executar no Linux ou macOS

```bash
docker compose up --build --wait

docker run --rm -i \
  --add-host=host.docker.internal:host-gateway \
  -e BASE_URL=http://host.docker.internal:8080 \
  -v "$PWD/performance-tests:/scripts:ro" \
  grafana/k6:0.55.2 run /scripts/k6/carga-assistlar.js

docker compose down
```

O comando encerra com falha quando um threshold não é atendido. A saída do k6
é a evidência principal nesta primeira versão; relatórios brutos não são
versionados.

## Perfil e limites

O perfil padrão é uma carga leve: até 5 usuários virtuais, com duração curta e
pausa entre iterações. Para apontar para outro ambiente controlado, altere
`BASE_URL`, por exemplo:

```powershell
-e BASE_URL=http://host.docker.internal:8080
```

Não execute este cenário contra produção sem autorização, janela de teste,
limites acordados e observabilidade suficiente.
