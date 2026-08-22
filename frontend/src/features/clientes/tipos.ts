export type StatusCliente = 'ATIVO' | 'INATIVO'

export interface Cliente {
  id: string
  nome: string
  email: string
  dataNascimento: string
  status: StatusCliente
  criadoEm: string
  atualizadoEm: string
}

export interface ClienteCadastro {
  nome: string
  email: string
  dataNascimento: string
}
