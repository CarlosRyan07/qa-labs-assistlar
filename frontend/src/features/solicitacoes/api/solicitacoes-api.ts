import { requisitarJson } from '../../../shared/api/http'

import type { HistoricoSolicitacao, Solicitacao, SolicitacaoCriacao } from '../tipos'

export interface PaginaSolicitacoes {
  itens: Solicitacao[]
  pagina: number
  tamanho: number
  totalItens: number
  totalPaginas: number
}

const recursoSolicitacoes = '/solicitacoes-assistencia'

export function listarSolicitacoes(contratacaoId: string): Promise<PaginaSolicitacoes> {
  return requisitarJson<PaginaSolicitacoes>(`${recursoSolicitacoes}?contratacaoId=${encodeURIComponent(contratacaoId)}`)
}

export function abrirSolicitacao(criacao: SolicitacaoCriacao): Promise<Solicitacao> {
  return requisitarJson<Solicitacao>(recursoSolicitacoes, {
    method: 'POST',
    corpo: criacao,
  })
}

export function consultarSolicitacao(id: string): Promise<Solicitacao> {
  return requisitarJson<Solicitacao>(`${recursoSolicitacoes}/${encodeURIComponent(id)}`)
}

export function iniciarSolicitacao(id: string): Promise<Solicitacao> {
  return requisitarJson<Solicitacao>(`${recursoSolicitacoes}/${encodeURIComponent(id)}/inicio`, {
    method: 'POST',
  })
}

export function concluirSolicitacao(id: string): Promise<Solicitacao> {
  return requisitarJson<Solicitacao>(`${recursoSolicitacoes}/${encodeURIComponent(id)}/conclusao`, {
    method: 'POST',
  })
}

export function cancelarSolicitacao(id: string, motivo?: string): Promise<Solicitacao> {
  return requisitarJson<Solicitacao>(`${recursoSolicitacoes}/${encodeURIComponent(id)}/cancelamento`, {
    method: 'POST',
    corpo: motivo ? { motivo } : {},
  })
}

export function consultarHistoricoSolicitacao(id: string): Promise<HistoricoSolicitacao[]> {
  return requisitarJson<HistoricoSolicitacao[]>(
    `${recursoSolicitacoes}/${encodeURIComponent(id)}/historico`,
  )
}
