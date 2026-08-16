# ADR 0001 — Estrutura do repositório para a aplicação Web

## Status

Aceito para a evolução `v0.2.0`.

## Contexto

O AssistLar `v0.1.0` mantém o backend Java na raiz do repositório. Essa
estrutura já possui build Maven, Docker, migrations, testes, CI, documentação e
uma release publicada. A evolução `v0.2.0` adicionará uma aplicação React e sua
estratégia de Quality Engineering sem alterar o comportamento do backend
existente.

A nova estrutura precisa:

- preservar o histórico e os comandos reproduzíveis da `v0.1.0`;
- permitir builds e quality gates independentes para backend e frontend;
- manter próximas as jornadas que atravessam todo o produto;
- comportar testes E2E e, futuramente, testes de performance;
- evitar uma reorganização extensa sem benefício funcional.

## Opções consideradas

### Manter o backend na raiz e criar `frontend/`

O Maven, o Dockerfile e o código Java permanecem onde estão. A aplicação Web
recebe seu próprio manifesto, configurações, testes e instruções dentro de
`frontend/`.

### Mover o backend para `backend/`

Produziria uma árvore visualmente simétrica, mas alteraria caminhos de build,
Docker, CI, documentação e evidências já estabilizados. O grande diff teria
pouco valor para o usuário do produto.

### Separar backend e frontend em repositórios diferentes

Isolaria ciclos de build, porém aumentaria a coordenação de versões, contratos,
ambiente Full Stack, evidências e jornadas E2E para um projeto mantido por uma
única pessoa.

## Decisão

Manter o backend na raiz e criar a aplicação Web em `frontend/`, no mesmo
repositório.

A estrutura será evoluída apenas quando houver conteúdo real:

```text
qa-labs-assistlar/
├── src/                         # backend existente
├── pom.xml
├── Dockerfile
├── compose.yaml
├── AGENTS.md
├── frontend/                    # criado no Marco 1
│   ├── AGENTS.md
│   ├── package.json
│   ├── src/
│   │   ├── app/
│   │   ├── features/
│   │   └── shared/
│   ├── cypress/                 # criado quando o Component Testing começar
│   └── e2e/                     # criado quando o Playwright começar
├── docs/
│   ├── architecture/
│   └── adr/
└── .github/workflows/
```

O futuro diretório `performance-tests/` não será criado durante a `v0.2.0`. Ele
só aparecerá quando o trabalho real da `v0.3.0` começar.

Dentro do frontend, a organização será orientada a features. Estrutura técnica
usa inglês e o domínio AssistLar usa português. `shared` não poderá depender de
`features`, e uma feature não acessará detalhes internos de outra.

## Consequências

### Benefícios

- preserva integralmente a estrutura e os comandos da release `v0.1.0`;
- mantém contrato, aplicação Web, testes E2E e documentação no mesmo histórico;
- permite configurar CI por caminhos e responsabilidades sem acoplar os builds;
- reduz o custo de executar e explicar a jornada Full Stack;
- evita movimentação de arquivos sem ganho de qualidade.

### Trade-offs

- a raiz não será simétrica entre `backend/` e `frontend/`;
- ferramentas Node precisarão executar com `frontend/` como diretório de trabalho;
- mudanças de contrato exigirão revisão coordenada do backend e do cliente Web;
- o repositório terá duas cadeias de build, Maven e npm.

### Limites

- nenhum diretório vazio será criado antecipadamente;
- o backend não será movido apenas para melhorar a aparência da árvore;
- Dockerização do frontend será avaliada depois que a Web estiver funcional;
- workflows de frontend, E2E e performance serão adicionados somente nos marcos
  correspondentes.
