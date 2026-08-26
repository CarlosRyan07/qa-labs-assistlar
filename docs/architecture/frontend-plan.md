# Plano do frontend — AssistLar v0.2.0

## Objetivo e fontes da auditoria

A aplicação Web deverá expor, com acessibilidade e feedback claro, apenas as
capacidades reais do backend AssistLar. Este plano foi elaborado a partir dos
controllers, DTOs, casos de uso, entidades, migrations, tratamento de erros,
testes automatizados, collection Postman e documentação da `v0.1.0`.

O backend e o contrato publicado em `/v3/api-docs` permanecem como fontes da
verdade. O frontend não será autoridade sobre regras de negócio e não terá
autenticação fictícia.

## Resultado da auditoria

### Contrato HTTP disponível

| Método | Caminho | Entrada | Resultado principal |
|---|---|---|---|
| `POST` | `/api/clientes` | `ClienteCadastroRequisicao` | `201`, `Location` e `ClienteResposta` |
| `GET` | `/api/clientes/{id}` | UUID no caminho | `200` e `ClienteResposta` |
| `POST` | `/api/clientes/{id}/inativacao` | sem corpo | `200` e cliente `INATIVO` |
| `POST` | `/api/clientes/{id}/reativacao` | sem corpo | `200` e cliente `ATIVO` |
| `GET` | `/api/planos` | sem entrada | `200` e lista de planos ativos |
| `GET` | `/api/planos/{id}` | UUID no caminho | `200` e plano ativo |
| `GET` | `/api/elegibilidades` | `clienteId` e `planoId` | `200` e resultado, inclusive quando inelegível |
| `POST` | `/api/contratacoes` | `ContratacaoCriacaoRequisicao` | `201`, `Location` e contratação `PENDENTE` |
| `GET` | `/api/contratacoes/{id}` | UUID no caminho | `200` e contratação |
| `POST` | `/api/contratacoes/{id}/ativacao` | sem corpo | `200` e contratação `ATIVA` |
| `POST` | `/api/contratacoes/{id}/cancelamento` | objeto com `motivo` opcional | `200` e contratação `CANCELADA` |
| `GET` | `/api/contratacoes/{id}/historico` | UUID no caminho | `200` e histórico ordenado |
| `POST` | `/api/solicitacoes-assistencia` | `SolicitacaoCriacaoRequisicao` | `201`, `Location` e solicitação `ABERTA` |
| `GET` | `/api/solicitacoes-assistencia/{id}` | UUID no caminho | `200` e solicitação |
| `POST` | `/api/solicitacoes-assistencia/{id}/inicio` | sem corpo | `200` e solicitação `EM_ATENDIMENTO` |
| `POST` | `/api/solicitacoes-assistencia/{id}/conclusao` | sem corpo | `200` e solicitação `CONCLUIDA` |
| `POST` | `/api/solicitacoes-assistencia/{id}/cancelamento` | objeto com `motivo` opcional | `200` e solicitação `CANCELADA` |
| `GET` | `/api/solicitacoes-assistencia/{id}/historico` | UUID no caminho | `200` e histórico ordenado |

Não existem métodos `PUT`, `PATCH` ou `DELETE` no contrato atual. As mudanças de
estado são ações explícitas via `POST`.

### Entradas

```text
ClienteCadastroRequisicao
  nome: string obrigatório, 3 a 120 caracteres
  email: string obrigatório, formato de e-mail, máximo 254 caracteres
  dataNascimento: data ISO obrigatória

ContratacaoCriacaoRequisicao
  clienteId: UUID obrigatório
  planoId: UUID obrigatório

ContratacaoCancelamentoRequisicao
  motivo: string opcional, máximo 500 caracteres

SolicitacaoCriacaoRequisicao
  contratacaoId: UUID obrigatório
  tipoAssistencia: ELETRICISTA | ENCANADOR | CHAVEIRO
  descricaoProblema: string obrigatória, máximo 500 caracteres

SolicitacaoCancelamentoRequisicao
  motivo: string opcional, máximo 500 caracteres
```

Mesmo quando o motivo for opcional, os endpoints de cancelamento esperam um
objeto JSON. A Web enviará `{}` ou `{ "motivo": null }`, conforme o caso.
`tipoResponsavel` nunca será enviado em um payload.

### Saídas e estados

- cliente: `ATIVO` ou `INATIVO`;
- contratação: `PENDENTE`, `ATIVA` ou `CANCELADA`;
- solicitação: `ABERTA`, `EM_ATENDIMENTO`, `CONCLUIDA` ou `CANCELADA`;
- assistência: `ELETRICISTA`, `ENCANADOR` ou `CHAVEIRO`;
- responsável no histórico: `CLIENTE`, `OPERADOR` ou `SISTEMA`, definido pelo
  backend;
- timestamps técnicos: strings ISO-8601 em UTC;
- identificadores: UUIDs;
- `versao`: dado informativo retornado em contratações e solicitações. A API não
  recebe versão, `ETag` ou `If-Match` do consumidor.

### Regras que afetam a experiência Web

- cadastro aceita clientes entre 18 e 120 anos, inclusive;
- e-mail é normalizado e único sem diferenciar maiúsculas;
- elegibilidade retorna motivos acumuláveis e não persiste resultado;
- cliente inativo, plano inativo ou contratação vigente impedem contratação;
- só pode existir uma contratação `PENDENTE` ou `ATIVA` por cliente;
- contratação não pode ser cancelada com solicitação aberta ou em atendimento;
- solicitação exige contratação ativa, cobertura e limite disponível;
- não pode haver duas solicitações abertas/em atendimento do mesmo tipo na mesma
  contratação;
- solicitação `EM_ATENDIMENTO` exige motivo não vazio para cancelamento;
- solicitação cancelada deixa de consumir limite;
- conflitos de transição e concorrência são visíveis como `409`.

O frontend pode validar formato, obrigatoriedade e limites simples para melhorar
a experiência. O backend continuará revalidando todas as regras.

## Limitações da v0.1.0 relevantes para a Web

### Ausência de endpoints de coleção

Somente planos possuem listagem. O backend não oferece:

- lista de clientes;
- lista ou busca de contratações por cliente;
- lista ou busca de solicitações por contratação;
- agregações para um dashboard operacional.

Consequentemente, a Web pode cadastrar recursos, navegar para o UUID retornado e
consultar um UUID conhecido, mas não pode implementar honestamente as listagens
pedidas nem seletores completos de clientes e contratações.

### Comunicação entre origens

O backend não possui configuração CORS. Durante o desenvolvimento, o Vite deverá
encaminhar `/api` para `http://localhost:8080`, mantendo as chamadas do navegador
na mesma origem. Uma futura imagem Web poderá aplicar o mesmo princípio com um
proxy reverso. Não será adicionada uma liberação CORS ampla apenas para contornar
o ambiente local.

### OpenAPI parcial para respostas de erro

O contrato contém os 18 caminhos, mas apenas o cadastro de cliente descreve de
forma completa seus códigos `201`, `400`, `409`, `422` e o conteúdo
`application/problem+json`. A implementação e os testes comprovam o tratamento
global, porém a documentação das demais operações ainda não detalha todos os
erros possíveis.

### Outros limites

- não há autenticação nem autorização;
- não há paginação porque não há endpoints de coleção, exceto planos;
- não há endpoint de limpeza de massa E2E;
- não há ambiente Web público nesta fase;
- dados criados por testes E2E precisam ser únicos e o banco da CI deve ser
  descartável.

## Páginas e rotas propostas

| Rota Web | Responsabilidade | Viabilidade com a API atual |
|---|---|---|
| `/` | navegação, estado operacional e resumo de planos/coberturas | parcial; sem métricas de clientes ou atendimentos |
| `/clientes` | cadastrar cliente e consultar por UUID | viável; listagem completa indisponível |
| `/clientes/:id` | detalhes, inativação e reativação | viável |
| `/planos` | listar planos e coberturas | viável |
| `/planos/:id` | detalhe do plano | viável |
| `/elegibilidade` | informar cliente, selecionar plano e avaliar | parcial; cliente precisa ser conhecido/informado |
| `/contratacoes/nova` | criar contratação e consultar por UUID | viável; listagem completa indisponível |
| `/contratacoes/:id` | detalhe, ativação, cancelamento e histórico | viável |
| `/solicitacoes/nova` | abrir solicitação e consultar por UUID | viável; listagem completa indisponível |
| `/solicitacoes/:id` | detalhe, transições, cancelamento e histórico | viável |

A navegação para detalhes usará os UUIDs presentes nas URLs. Depois de uma
criação, a Web navegará diretamente para o recurso retornado. Não será criado um
cadastro fictício em `localStorage` para simular listagens do servidor.

## Jornada principal

```text
Cadastrar cliente
        ↓
Avaliar elegibilidade para um plano listado
        ↓
Criar contratação PENDENTE
        ↓
Ativar contratação
        ↓
Abrir solicitação ABERTA
        ↓
Iniciar atendimento
        ↓
Concluir atendimento
        ↓
Consultar histórico
```

Os UUIDs retornados em cada criação serão propagados por navegação e estado
efêmero da jornada, sem se tornarem mecanismo de autorização.

## Arquitetura frontend

```text
frontend/src/
├── app/                  # composição, providers e rotas
├── features/
│   ├── clientes/
│   ├── planos/
│   ├── elegibilidade/
│   ├── contratacoes/
│   └── solicitacoes/
├── shared/
│   ├── api/              # cliente HTTP e ProblemDetail
│   ├── components/       # componentes sem conhecimento do domínio
│   ├── types/
│   └── utils/
└── main.tsx
```

Diretórios internos serão criados somente quando houver mais de um elemento ou
uma responsabilidade concreta. Não haverá um diretório global `services/`.

### Estado e comunicação

- TanStack Query gerenciará estado remoto, cache, loading, erro e invalidação;
- React manterá estado local de formulários e interação;
- não será usado Redux;
- o cliente HTTP comum usará `fetch`, evitando uma dependência adicional como
  Axios sem necessidade;
- chamadas específicas ficarão dentro de cada feature;
- `VITE_API_URL` definirá a base da API e não será acessada diretamente pelos
  componentes;
- o valor local recomendado é `/api`, encaminhado pelo proxy do Vite ao backend;
- `API_PROXY_TARGET`, usado somente pela configuração do Vite, apontará para
  `http://localhost:8080` por padrão e não será exposto ao bundle;
- `/actuator` e `/v3` só serão incluídos no proxy se a Web realmente consumir
  health ou o contrato durante o desenvolvimento;
- mutações bem-sucedidas atualizarão ou invalidarão somente as queries afetadas;
- mutações não terão retry automático nem atualização otimista, pois conflitos
  `409` são parte observável do domínio;
- nenhuma regra complexa de domínio será duplicada no cache do navegador.

## Estratégia para tipos e OpenAPI

Duas alternativas foram avaliadas:

### Tipos e cliente HTTP manuais

- baixo custo inicial para um contrato pequeno e estável;
- nomes e nulabilidade ficam explícitos próximos às features;
- não adiciona gerador, código produzido ou etapa extra de build;
- exige revisão consciente quando o contrato mudar.

### Geração a partir do OpenAPI

- reduz trabalho manual e parte do risco de divergência;
- adiciona dependência, configuração, política para código gerado e acoplamento à
  disponibilidade de um contrato reproduzível;
- pode gerar uma superfície maior que as 18 rotas realmente consumidas.

### Decisão inicial

Usar tipos TypeScript e funções HTTP manuais no início da `v0.2.0`. O contrato é
pequeno, a decisão é reversível e evita adicionar complexidade antes de existir
repetição real. Os testes verificarão serialização, desserialização e tratamento
de erros nas fronteiras relevantes.

Se o contrato crescer ou divergências se repetirem, a geração será reavaliada e
documentada em ADR antes de introduzir a nova dependência.

## Tratamento de erros

A API usa `application/problem+json` com a forma observável:

```text
type: string
title: string
status: number
detail: string
instance: string
timestamp: string
erros: Array<{ campo: string, mensagem: string }>
```

O cliente deverá:

- reconhecer `ProblemDetail` sem confiar que toda falha de rede terá esse corpo;
- associar `erros` aos campos quando possível e manter um resumo acessível;
- diferenciar `400`, `404`, `409` e `422` na mensagem apresentada;
- orientar nova consulta após conflitos de concorrência;
- nunca exibir stack trace, SQL ou detalhes técnicos inexistentes no contrato;
- fornecer fallback compreensível para indisponibilidade, timeout ou resposta não
  estruturada.

## UX, acessibilidade e testabilidade

- landmarks, títulos hierárquicos e HTML semântico;
- labels e instruções associadas aos campos;
- foco levado ao resumo de erro após falha de submissão;
- confirmação textual e não apenas por cor;
- operações destrutivas ou irreversíveis com contexto e confirmação proporcional;
- botões desabilitados durante mutação para evitar duplicidade acidental;
- estados de loading, vazio, sucesso, conflito e regra de negócio;
- navegação completa por teclado e foco visível;
- layout responsivo desde o primeiro componente;
- consultas de teste por role, label e texto;
- `data-testid` somente quando não houver seletor semântico adequado.

Uma biblioteca automática de acessibilidade não será adicionada no Marco 1 sem
justificativa de dependência. A semântica e os fluxos por teclado serão cobertos
desde o início; automação especializada será avaliada no marco de testes.

## Estratégia de testes da v0.2.0

| Nível | Ferramenta | Risco principal |
|---|---|---|
| funções, hooks e componentes | Vitest e Testing Library | transformação, validação simples, estados e interação acessível |
| integração UI/API simulada | Vitest, Testing Library e mocks na fronteira HTTP | loading, sucesso e `ProblemDetail` sem backend real |
| componentes no navegador | Cypress Component Testing | comportamento real do componente, estilos e eventos do navegador |
| smoke Web | Cypress E2E, suíte pequena | inicialização e fluxo mínimo sem duplicar Playwright |
| jornadas críticas | Playwright | integração Full Stack, estados, erros e histórico |
| compatibilidade | projetos Playwright | Chromium, Firefox, WebKit e perfil mobile selecionado |
| regressão visual | Playwright, telas estáveis | mudanças visuais relevantes em poucos pontos de alto valor |

Vitest e Testing Library fornecerão feedback rápido. Cypress terá foco em
Component Testing. Playwright será a principal ferramenta E2E. A mesma matriz de
casos não será reproduzida integralmente nas três camadas.

Testes E2E criarão e-mails únicos, descobrirão planos pelo `codigo` e encerrarão
contratações quando a jornada permitir. No CI, o banco será efêmero. Não será
criado endpoint de limpeza exclusivo para testes na API pública.

## Marcos e critérios de aceite

### Marco 1 — Fundação

- React, TypeScript estrito, Vite, Material UI, React Router e TanStack Query;
- `frontend/AGENTS.md`;
- infraestrutura mínima de Vitest e Testing Library para que as features já
  nasçam testáveis;
- ESLint e scripts `lint`, `typecheck`, `test` e `build`;
- `VITE_API_URL` e proxy local;
- shell acessível e responsivo, ainda sem regras fictícias;
- `npm ci`, lint, typecheck, testes existentes e build aprovados.

### Marco 2 — Jornada funcional

- features implementadas na ordem da jornada principal;
- consulta por UUID onde a API não oferece coleção;
- histórico e transições apresentados conforme o estado retornado;
- testes de comportamento adicionados junto com cada feature;
- validação após cada feature.

### Marco 3 — Testes de frontend

- consolidação da suíte Vitest e Testing Library e cobertura dos gaps de risco;
- métricas reais coletadas antes de propor quality gate;
- nenhum percentual escolhido apenas para apresentação.

### Marco 4 — Cypress

- Component Testing dos componentes de maior risco;
- pequena suíte smoke, sem duplicar a regressão E2E.

### Marco 5 — Playwright

- jornada crítica, cenário negativo de cancelamento em atendimento,
  cross-browser e mobile;
- Compose E2E isolado, com banco efêmero, dados sintéticos únicos e limpeza
  obrigatória dos containers e volumes ao final;
- Chromium no comando rápido; Firefox, WebKit e perfil mobile Chromium no
  comando completo;
- traces, screenshots e vídeos apenas em falha, com relatório HTML ignorado
  pelo Git;
- regressão visual permanece adiada para telas que tenham se estabilizado.

### Consolidação

- frontend e E2E na CI sem reduzir gates do backend;
- documentação, README, CHANGELOG e evidências reais atualizados;
- Docker do frontend avaliado somente após a aplicação funcionar.

## Riscos e decisões

### Decisão reavaliada: momento de introdução dos testes rápidos

A proposta original adicionava Vitest e Testing Library somente no Marco 3,
embora o Marco 1 já exigisse um script `test` e o Marco 2 exigisse validação após
cada feature. Isso criaria funcionalidades sem a infraestrutura automatizada
necessária para testá-las no momento em que fossem implementadas.

Vitest e Testing Library serão configurados minimamente no Marco 1. O Marco 2
adicionará testes junto das features, e o Marco 3 consolidará cobertura, gaps e
um eventual quality gate baseado em métricas observadas. As ferramentas já
faziam parte do escopo aprovado; apenas sua ordem foi antecipada.

### Decisão: jornada guiada sem novos endpoints de coleção

A `v0.2.0` preservará a API atual. As páginas de clientes, contratações e
solicitações oferecerão criação e consulta por UUID, enquanto o dashboard ficará
limitado à navegação da jornada e aos dados reais de planos e coberturas.

Os UUIDs retornados serão mantidos durante a jornada e usados nas rotas de
detalhe. A Web não criará listagens locais que possam divergir do banco nem
apresentará totais sem fonte no backend.

Endpoints paginados de coleção ficam como possível evolução separada. Essa
necessidade só será reavaliada se a implementação real demonstrar que a jornada
guiada prejudica suficientemente a navegação ou a retomada de recursos. Qualquer
evolução deverá ter autorização própria, contrato OpenAPI e testes de backend.

### Riscos adicionais

- tipos manuais podem divergir do OpenAPI se mudanças não forem coordenadas;
- respostas de erro das operações além de clientes estão incompletas no OpenAPI;
- a ausência de autenticação limita o caráter público de uma implantação;
- E2E concorrente pode colidir se não usar dados únicos e banco isolado;
- regressão visual precoce pode gerar ruído enquanto o design estiver mudando;
- duas suítes E2E completas em Cypress e Playwright gerariam manutenção duplicada;
- Docker e CI multi-camadas adicionados antes da jornada funcionar aumentariam o
  tempo de feedback sem reduzir risco imediato.

## Itens explicitamente fora deste marco

- implementação React;
- mudanças no backend ou em suas regras;
- CORS permissivo;
- geração automática de cliente OpenAPI;
- Redux, Axios, React Hook Form ou Zod sem necessidade demonstrada;
- Cypress, Playwright, regressão visual e workflows correspondentes;
- Docker do frontend;
- qualquer conteúdo de `performance-tests/`;
- tag, release, merge ou push.

## Estado após o Marco 2

A jornada funcional foi implementada preservando os 18 caminhos existentes da
API. Clientes, contratações e solicitações oferecem criação e consulta por UUID;
planos usam a listagem real do backend. As telas de detalhe apresentam somente
transições válidas para o estado observado e recarregam detalhe e histórico após
as ações, sem atualização otimista.

As páginas são carregadas sob demanda por rota. A suíte rápida acompanha as
features com Vitest e Testing Library, enquanto as regras completas continuam
sob responsabilidade dos testes do backend. Listagens ausentes, autenticação,
Cypress, Playwright e novos endpoints permanecem fora do Marco 2.

## Estado após o Marco 3

A suíte Vitest e Testing Library foi consolidada com testes de componentes,
formulários, transições, fronteiras HTTP e estados seguros de erro. A medição
com o provedor V8 registrou 87,62% de statements, 86,24% de branches, 80,70% de
functions e 89,09% de lines, em 61 testes distribuídos por 18 arquivos na
validação de consolidação da v0.2.0.

Com essa linha de base observada, o quality gate local passou a exigir 80% de
statements, 70% de branches, 80% de functions e 80% de lines. Ele considera o
código fonte e exclui apenas o bootstrap estrutural `main.tsx` e a declaração
gerada `vite-env.d.ts`, além dos specs Cypress `*.cy.tsx` que não são
executados pelo Vitest. Os números serão reavaliados quando Cypress Component
Testing e Playwright adicionarem camadas diferentes de evidência; métricas Java
e TypeScript continuarão separadas.

No ambiente Windows do projeto, o Vitest usa `forks`, um worker e execução
serial. A configuração foi escolhida depois de confirmar que o pool de threads
executava os cenários, mas não encerrava o processo de modo confiável. É um
trade-off explícito de duração por repetibilidade e poderá ser reavaliado na CI.

## Estado após o Marco 4

O Cypress Component Testing foi configurado com React e Vite, executando em
Electron. A suíte contém três cenários: foco observável do atalho para o
conteúdo principal e duas verificações de cancelamento em atendimento
(mensagem obrigatória e payload sem `tipoResponsavel`).

O Component Testing não reproduz a jornada E2E nem as regras completas já
cobertas pelo backend. Ele é a camada complementar para eventos, foco, campos
Material UI e requisições interceptadas no navegador. O Playwright continua
reservado para a jornada Full Stack do Marco 5.

Durante a configuração foi identificado que `ELECTRON_RUN_AS_NODE` impede o
Electron de iniciar quando a variável é herdada pelo processo. O script local
do Cypress a remove somente para sua execução; nenhuma variável é exposta ao
código da aplicação.

## Estado após o Marco 5

O Playwright foi configurado como a suíte E2E principal. A jornada dourada
cadastro de cliente → elegibilidade → contratação → ativação → solicitação →
início → conclusão é executada pelo navegador contra o backend e PostgreSQL
reais. Um cenário negativo complementar confirma que o cancelamento em
atendimento exige motivo e atualiza o histórico após a confirmação da API.

O comando rápido usa Chromium. A matriz completa também valida Firefox, WebKit
e o perfil mobile Chromium. Cada execução sobe um Compose específico do E2E na
porta local `18080`, com volume de banco efêmero e dados sintéticos únicos; o
script remove containers, rede e volume mesmo se um teste falhar. Traces,
screenshots, vídeos e relatório HTML são preservados somente como artefatos
locais em caso de falha e permanecem ignorados pelo Git.

O Vitest exclui `e2e/**` da descoberta de testes para não coletar specs do
Playwright. Essa separação mantém o quality gate baseado exclusivamente no
código fonte e nos testes unitários/componentes, sem reduzir os limites já
estabelecidos.

### Atualizacao da validacao cross-browser e visual

A jornada Playwright foi validada em Chromium, Firefox, WebKit e mobile
Chromium. A execucao completa registrou nove testes aprovados e tres cenarios
visuais ignorados fora do Chromium, pois a baseline visual e mantida somente
nessa engine para reduzir diferencas de renderizacao. As jornadas funcionais
continuam obrigatorias em todos os projetos da matriz.
