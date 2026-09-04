# Cenários BDD do AssistLar

Este documento transforma riscos de negócio já automatizados em exemplos
Gherkin legíveis por QA, desenvolvimento e pessoas de produto. Ele é
documentação viva: não introduz Cucumber nem uma segunda suíte de testes.

O status **Automatizado** significa que o comportamento já é exercitado pela
classe indicada. A referência aponta para a fonte de verdade executável.

| ID | Cenário | Estado | Evidência automatizada |
|---|---|---|---|
| BDD-CLI-01 | Cadastro de cliente adulto | Automatizado | ClienteApiIT |
| BDD-ELE-01 | Elegibilidade informativa | Automatizado | ElegibilidadeApiIT |
| BDD-CON-01 | Jornada de contratação | Automatizado | ContratacaoApiIT |
| BDD-SOL-01 | Jornada de solicitação | Automatizado | SolicitacaoApiIT |
| BDD-SOL-02 | Cancelamento em atendimento | Automatizado | SolicitacaoApiIT, Cypress Component Testing |
| BDD-CON-02 | Proteção concorrente de contratação | Automatizado | ContratacaoConcorrenciaIT |
| BDD-SOL-03 | Proteção concorrente de limite | Automatizado | SolicitacaoConcorrenciaIT |

## BDD-CLI-01 — Cadastrar cliente adulto

~~~gherkin
Funcionalidade: Cadastro de clientes

  Cenário: Cadastrar cliente com idade mínima atendida
    Dado que informo nome, e-mail válido e data de nascimento de uma pessoa adulta
    Quando envio o cadastro de cliente
    Então recebo 201 Created
    E o cliente é criado com status ATIVO
    E a resposta informa o header Location
~~~

Estado: **Automatizado** por ClienteApiIT.

## BDD-ELE-01 — Consultar elegibilidade sem persistir resultado

~~~gherkin
Funcionalidade: Elegibilidade para contratação

  Cenário: Cliente ativo, adulto e sem contratação vigente
    Dado que existe um cliente ativo e maior de idade
    E existe um plano ativo
    Quando consulto a elegibilidade do cliente para o plano
    Então recebo 200 OK com elegivel igual a true
    E nenhuma contratação ou histórico é criado pela consulta
~~~

Estado: **Automatizado** por ElegibilidadeApiIT.

## BDD-CON-01 — Ativar e cancelar uma contratação

~~~gherkin
Funcionalidade: Contratação de plano

  Cenário: Criar, ativar e cancelar contratação elegível
    Dado que um cliente é elegível para um plano
    Quando crio a contratação
    Então ela inicia como PENDENTE
    Quando o operador ativa a contratação
    Então ela fica ATIVA
    Quando o operador a cancela sem solicitação em andamento
    Então ela fica CANCELADA
    E o histórico registra cada mudança de estado
~~~

Estado: **Automatizado** por ContratacaoApiIT.

## BDD-SOL-01 — Concluir uma assistência coberta

~~~gherkin
Funcionalidade: Solicitação de assistência residencial

  Cenário: Abrir e concluir serviço coberto
    Dado que existe uma contratação ATIVA com cobertura disponível
    Quando abro uma solicitação de eletricista
    Então a solicitação inicia como ABERTA
    Quando o operador inicia o atendimento
    Então ela fica EM_ATENDIMENTO
    Quando o operador conclui o atendimento
    Então ela fica CONCLUIDA
    E o histórico registra as transições
~~~

Estado: **Automatizado** por SolicitacaoApiIT e pela jornada E2E Playwright.

## BDD-SOL-02 — Exigir motivo no cancelamento em atendimento

~~~gherkin
Funcionalidade: Cancelamento de solicitação

  Cenário: Rejeitar cancelamento sem motivo após início do atendimento
    Dado que uma solicitação está EM_ATENDIMENTO
    Quando tento cancelá-la sem motivo
    Então recebo erro de regra de negócio
    E a solicitação permanece EM_ATENDIMENTO
    Quando informo um motivo não vazio
    Então o cancelamento é confirmado
~~~

Estado: **Automatizado** por SolicitacaoApiIT e pelo Component Testing da tela
de detalhe de solicitação.

## BDD-CON-02 — Impedir duas contratações vigentes simultâneas

~~~gherkin
Funcionalidade: Consistência de contratação

  Cenário: Concorrência ao criar contratação
    Dado que um cliente não possui contratação vigente
    Quando duas tentativas de contratação ocorrem simultaneamente
    Então exatamente uma tentativa é aceita
    E existe somente uma contratação PENDENTE ou ATIVA no banco
~~~

Estado: **Automatizado** por ContratacaoConcorrenciaIT, com barreira
determinística e PostgreSQL real.

## BDD-SOL-03 — Respeitar limite sob concorrência

~~~gherkin
Funcionalidade: Limite de utilização de assistência

  Cenário: Tentativas simultâneas não excedem o limite do plano
    Dado que uma contratação ativa possui limite disponível para eletricista
    Quando duas aberturas concorrentes ocorrem para o mesmo tipo de assistência
    Então o limite do plano não é ultrapassado
    E o banco permanece consistente após a rejeição
~~~

Estado: **Automatizado** por SolicitacaoConcorrenciaIT, com barreira
determinística, timeout e inspeção final do banco.

## Próximo passo BDD

Uma evolução futura pode tornar uma seleção desses cenários diretamente
executável por uma ferramenta BDD. Antes disso, deve haver justificativa para a
dependência e para o custo de manutenção, conforme o AGENTS.md.
