import { requisitarJson } from '../../shared/api/http'

import type { Contratacao, ContratacaoCriacao, HistoricoContratacao } from './tipos'

export interface PaginaContratacoes {
  itens: Contratacao[]
  pagina: number
  tamanho: number
  totalItens: number
  totalPaginas: number
}

export function listarContratacoes(clienteId: string): Promise<PaginaContratacoes> {
  return requisitarJson<PaginaContratacoes>(`/contratacoes?clienteId=${encodeURIComponent(clienteId)}`)
}

export function criarContratacao(dados: ContratacaoCriacao): Promise<Contratacao> {
  return requisitarJson<Contratacao>('/contratacoes', {
    method: 'POST',
    corpo: dados,
  })
}

export function consultarContratacao(id: string): Promise<Contratacao> {
  return requisitarJson<Contratacao>(`/contratacoes/${encodeURIComponent(id)}`)
}

export function ativarContratacao(id: string): Promise<Contratacao> {
  return requisitarJson<Contratacao>(`/contratacoes/${encodeURIComponent(id)}/ativacao`, {
    method: 'POST',
  })
}

export function cancelarContratacao(id: string, motivo?: string): Promise<Contratacao> {
  const motivoNormalizado = motivo?.trim()

  return requisitarJson<Contratacao>(`/contratacoes/${encodeURIComponent(id)}/cancelamento`, {
    method: 'POST',
    corpo: { motivo: motivoNormalizado || undefined },
  })
}

export function consultarHistoricoContratacao(id: string): Promise<HistoricoContratacao[]> {
  return requisitarJson<HistoricoContratacao[]>(
    `/contratacoes/${encodeURIComponent(id)}/historico`,
  )
}
