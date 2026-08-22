# AssistLar — Frontend Quality Engineering

Estas regras complementam o `AGENTS.md` da raiz e valem para todo o diretório
`frontend/`. Em caso de conflito, prevalecem as regras da raiz.

## Linguagem e estrutura

- Use português no domínio, nos componentes, nos testes e nas mensagens para o
  usuário.
- Não use acentos em identificadores técnicos, nomes de arquivos, componentes,
  funções, atributos ou rotas.
- Use inglês apenas para estrutura e conceitos técnicos, como `features`,
  `shared`, `hooks`, `api`, `components`, `e2e` e `fixtures`.
- Organize o código por features e crie diretórios somente quando houver conteúdo
  e responsabilidade reais.
- `shared` nunca depende de `features`; uma feature não acessa detalhes internos
  de outra.

## Stack e dependências

- Use Node.js 24 LTS, npm, React, TypeScript estrito, Vite, Material UI, React
  Router e TanStack Query.
- Use versões exatas no `package.json` e preserve o `package-lock.json`.
- Antes de adicionar dependência não prevista, aplique a justificativa exigida no
  `AGENTS.md` da raiz.
- Não adicione Redux, Axios, React Hook Form, Zod, MSW ou bibliotecas de
  acessibilidade sem necessidade concreta e aprovação quando exigida.
- Não use abstrações para eliminar repetição pequena.

## API e estado

- O backend é a autoridade sobre regras de negócio.
- Use `VITE_API_URL`; não escreva URLs do backend dentro de componentes.
- Use a API `fetch` por meio da fronteira HTTP comum em `shared/api`.
- TanStack Query gerencia estado remoto; estado de formulário e interação fica
  local sempre que possível.
- Não use atualização otimista nas transições de domínio e não aplique retry
  automático em mutações.
- Trate `ProblemDetail` e falhas de rede sem expor detalhes internos.
- Nunca envie `tipoResponsavel` em payloads.
- Não invente autenticação, autorização, métricas, listagens ou dados ausentes na
  API.

## Acessibilidade e interface

- Use HTML semântico, landmarks, labels visíveis, nomes acessíveis e hierarquia
  correta de títulos.
- Garanta navegação por teclado, foco visível e foco adequado após erros ou
  diálogos.
- Não comunique estado somente por cor.
- Implemente estados observáveis de loading, vazio, sucesso, erro, conflito e
  regra de negócio.
- Considere responsividade desde o primeiro componente.

## Testes e validação

- Use Vitest e Testing Library para feedback rápido de funções, hooks,
  componentes e integração UI/API simulada.
- Prefira seletores por role, label e texto. Use `data-testid` apenas quando não
  houver seletor semântico apropriado.
- Não teste detalhes internos de implementação nem replique no navegador toda a
  matriz já coberta no backend.
- Cypress terá foco em Component Testing; Playwright será o E2E principal nos
  marcos correspondentes.
- Não persiga cobertura artificial nem defina quality gate antes de medir a
  cobertura real.

Antes de concluir uma alteração do frontend, execute:

```text
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
```

Não reduza testes, assertions ou validações para contornar falhas.

## Artefatos e Git

- Não versione `node_modules`, `dist`, `coverage`, relatórios temporários,
  screenshots de falha ou dados sensíveis.
- Revise o diff e o lockfile antes de cada commit.
- Preserve as regras de branch, commit, push, merge e release definidas no
  `AGENTS.md` da raiz.
