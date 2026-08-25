# Frontend do AssistLar

Aplicação Web da evolução `v0.2.0`, construída como uma jornada guiada sobre o
contrato existente do backend.

## Ambiente

- Node.js `24.18.0` LTS;
- npm `11.16.0`;
- React `19.2.8`;
- TypeScript `5.9.3`;
- Vite `8.2.1`;
- Material UI `9.3.1`;
- React Router `7.18.2`;
- TanStack Query `5.101.4`;
- Vitest `4.1.10` e Testing Library.
- Cypress `15.20.1` para Component Testing.
- Playwright `1.62.1` para jornadas E2E Full Stack.

Confirme o ambiente:

```bash
node --version
npm --version
```

## Configuração

Copie `.env.example` para `.env.local` somente quando precisar sobrescrever os
valores locais.

```text
VITE_API_URL=/api
API_PROXY_TARGET=http://localhost:8080
```

`VITE_API_URL` é exposta à aplicação. `API_PROXY_TARGET` é lida somente pela
configuração do Vite e encaminha `/api` para o backend durante o desenvolvimento.

## Comandos

Execute dentro de `frontend/`:

```bash
npm ci
npm run dev
npm run lint
npm run typecheck
npm run test
npm run test:coverage
npm run test:component
npm run test:e2e
npm run test:e2e:full
npm run build
```

Para utilizar a API durante o desenvolvimento, mantenha o PostgreSQL e o backend
do AssistLar em execução. O frontend fica disponível por padrão em
<http://localhost:5173>.

## Limite funcional atual

A API `v0.1.0` lista apenas planos. A Web será uma jornada guiada: recursos
criados são acessados pelo UUID retornado, e recursos conhecidos podem ser
consultados por UUID. O frontend não simula listagens ou métricas ausentes no
backend.

## Jornada funcional

A navegação acompanha as capacidades reais da API:

| Rota | Operações |
|---|---|
| `/clientes` | cadastrar cliente e iniciar consulta por UUID |
| `/clientes/:id` | consultar, inativar, reativar e seguir para elegibilidade |
| `/planos` | listar planos ativos e suas coberturas |
| `/planos/:id` | consultar detalhes e limites de utilização |
| `/elegibilidade` | avaliar cliente e plano sem persistir resultado |
| `/contratacoes/nova` | criar contratação pendente ou consultar por UUID |
| `/contratacoes/:id` | ativar, cancelar e consultar histórico |
| `/solicitacoes/nova` | abrir solicitação ou consultar por UUID |
| `/solicitacoes/:id` | iniciar, concluir, cancelar e consultar histórico |

As páginas funcionais são carregadas sob demanda. TanStack Query controla estado
remoto, loading e atualização após transições; o backend continua sendo a fonte
de verdade para elegibilidade, cobertura, limites e conflitos.

Os testes rápidos usam Vitest e Testing Library. Eles verificam comportamento
observável, navegação, formulários, estados, transições, histórico, falhas de
rede e respostas `ProblemDetail`, sem duplicar a matriz de regras já coberta no
backend.

`npm run test:coverage` gera as métricas de statements, branches, functions e
lines em `coverage/`. O relatório HTML pode ser aberto por
`coverage/index.html`; ele é um artefato local e não é versionado. A medição
considera o código de `src/`; ficam fora apenas o bootstrap estrutural
(`src/main.tsx`), a declaração gerada pelo Vite (`src/vite-env.d.ts`) e os
arquivos de teste Cypress (`*.cy.tsx`), que pertencem a outra suíte.

O quality gate inicial exige ao menos 80% de statements, 70% de branches, 80%
de functions e 80% de lines. Os limites foram definidos após a primeira medição
real do Marco 3, não para mascarar lacunas de teste.

No Windows, a suíte usa um processo isolado e execução serial para evitar que o
pool de threads do Vitest permaneça aberto após os testes. A prioridade atual é
um resultado determinístico; o paralelismo será reavaliado quando houver uma
evidência de estabilidade no ambiente de CI.

## Component Testing

O Cypress executa poucos componentes de maior risco em Electron real. Nesta
fase, ele confirma o atalho focalizável do shell e o cancelamento de uma
solicitação em atendimento, incluindo a mensagem obrigatória e o payload sem
`tipoResponsavel`. A matriz completa de regras permanece nos testes do backend,
e as jornadas integradas permanecerão sob responsabilidade do Playwright no
Marco 5.

Use `npm run test:component` para execução headless ou
`npm run test:component:open` para abrir o Cypress interativamente. O wrapper
local remove `ELECTRON_RUN_AS_NODE` apenas do processo do Cypress, pois essa
variável impede a inicialização do Electron quando herdada pelo terminal.

## Testes E2E

O Playwright é a camada E2E principal. `npm run test:e2e` sobe uma instância
isolada do Compose, com PostgreSQL descartável e backend exposto apenas em
`http://127.0.0.1:18080`; depois inicia o Vite com proxy local e executa a
jornada crítica no Chromium. Ao final, inclusive quando há falha, o script
remove os containers e o volume desse projeto isolado.

`npm run test:e2e:full` executa a mesma suíte em Chromium, Firefox, WebKit e no
perfil mobile Chromium. Antes da primeira execução completa, baixe os
navegadores com `npx playwright install`.

Cada cenário usa e-mails sintéticos e únicos. A jornada principal é conduzida
inteiramente pela interface; apenas a precondição do cenário negativo é criada
pela API, para concentrar a evidência da UI no cancelamento em atendimento. Em
falhas, Playwright preserva trace, screenshot e vídeo em `test-results/` e gera
o relatório HTML em `playwright-report/`; ambos são artefatos locais ignorados
pelo Git.

### Regressao visual

A regressao visual cobre a pagina inicial com uma baseline versionada em
`e2e/regressao-visual.spec.ts-snapshots/`. Para atualizar a imagem de referencia
apos uma mudanca visual intencional, execute `npm run test:e2e -- --grep
"regressao visual" --update-snapshots`; para validar a imagem existente, use o
mesmo comando sem `--update-snapshots`. Snapshots sao revisados como codigo e
nao devem ser atualizados para mascarar alteracoes acidentais.
