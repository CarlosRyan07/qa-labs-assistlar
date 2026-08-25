import { requisitarJson } from '../../../shared/api/http'

import type { Cliente, ClienteCadastro } from '../tipos'

export interface PaginaClientes {
  itens: Cliente[]
  pagina: number
  tamanho: number
  totalItens: number
  totalPaginas: number
}

export function listarClientes(busca = ''): Promise<PaginaClientes> {
  const parametros = busca ? `?busca=${encodeURIComponent(busca)}` : ''
  return requisitarJson<PaginaClientes>(`/clientes${parametros}`)
}

export function cadastrarCliente(cadastro: ClienteCadastro): Promise<Cliente> {
  return requisitarJson<Cliente>('/clientes', {
    method: 'POST',
    corpo: cadastro,
  })
}

export function consultarCliente(id: string): Promise<Cliente> {
  return requisitarJson<Cliente>(`/clientes/${encodeURIComponent(id)}`)
}

export function inativarCliente(id: string): Promise<Cliente> {
  return requisitarJson<Cliente>(`/clientes/${encodeURIComponent(id)}/inativacao`, {
    method: 'POST',
  })
}

export function reativarCliente(id: string): Promise<Cliente> {
  return requisitarJson<Cliente>(`/clientes/${encodeURIComponent(id)}/reativacao`, {
    method: 'POST',
  })
}
