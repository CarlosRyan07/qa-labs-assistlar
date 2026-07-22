# Catálogo de cenários de teste

Este catálogo destaca riscos do domínio e onde eles são exercitados. Os testes permanecem independentes e usam dados determinísticos ou UUIDs gerados por cenário.

## Cliente

| ID | Cenário | Nível |
|---|---|---|
| CLI-01 | rejeitar cliente menor de 18 anos, inclusive nascido hoje | unitário/API |
| CLI-02 | aceitar exatamente 18 anos | unitário |
| CLI-03 | aceitar exatamente 120 anos | unitário/API |
| CLI-04 | rejeitar 120 anos e um dia | unitário/API |
| CLI-05 | rejeitar data futura | unitário/API |
| CLI-06 | normalizar e-mail e impedir duplicidade case-insensitive | integração/API |
| CLI-07 | rejeitar nome com menos de 3 caracteres úteis | unitário/API |
| CLI-08 | inativar e reativar | unitário/API |

## Elegibilidade

| ID | Cenário | Nível |
|---|---|---|
| ELE-01 | cliente ativo e adulto elegível | unitário/API |
| ELE-02 | manter defesa de elegibilidade para menor presente em dado legado | unitário/integração |
| ELE-03 | acumular cliente inativo, menor, plano inativo e contratação vigente | unitário |
| ELE-04 | retornar `200` com `elegivel=false` | controller/API |
| ELE-05 | consultar sem persistir contratação ou histórico | integração |

## Contratação

| ID | Cenário | Nível |
|---|---|---|
| CON-01 | criar contratação `PENDENTE` com histórico inicial | API |
| CON-02 | ativar pendente | unitário/API |
| CON-03 | cancelar pendente ou ativa | unitário/API |
| CON-04 | rejeitar transição inválida | unitário/controller |
| CON-05 | permitir nova contratação após cancelamento | API |
| CON-06 | impedir cancelamento com solicitação ativa | integração/API |
| CON-07 | duas inserções simultâneas geram um sucesso e um conflito | concorrência/banco |

## Solicitação e limites

| ID | Cenário | Nível |
|---|---|---|
| SOL-01 | abrir serviço coberto em contratação ativa | API |
| SOL-02 | rejeitar contratação pendente | API |
| SOL-03 | rejeitar chaveiro no ESSENCIAL por cobertura ausente | API |
| SOL-04 | impedir duplicidade em andamento do mesmo tipo | API/banco |
| SOL-05 | percorrer `ABERTA → EM_ATENDIMENTO → CONCLUIDA` | unitário/API |
| SOL-06 | cancelar aberta sem motivo | unitário/API |
| SOL-07 | exigir motivo para cancelar em atendimento | unitário/API |
| SOL-08 | cancelamento liberar consumo | API |
| SOL-09 | ESSENCIAL esgotar no primeiro uso concluído | API |
| SOL-10 | COMPLETO aceitar dois usos e rejeitar o terceiro | API |
| SOL-11 | aberturas simultâneas não ultrapassarem limite | concorrência |
| SOL-12 | dois inícios simultâneos produzirem uma transição | concorrência/optimistic locking |

## Contrato e operação

| ID | Cenário | Nível |
|---|---|---|
| API-01 | OpenAPI conter todos os caminhos do MVP | contrato/API |
| API-02 | schemas de entrada não aceitarem `tipoResponsavel` | contrato/controller |
| API-03 | erros não exporem detalhes internos | controller/API |
| OPS-01 | Flyway aplicar do zero em PostgreSQL 17.10 | integração |
| OPS-02 | health exposto e info não exposto | API |
| OPS-03 | imagem e Compose iniciarem aplicação saudável | container |
