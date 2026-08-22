export type StatusContratacao = 'PENDENTE' | 'ATIVA' | 'CANCELADA'

export type TipoResponsavel = 'CLIENTE' | 'OPERADOR' | 'SISTEMA'

export interface Contratacao {
  id: string
  clienteId: string
  planoId: string
  status: StatusContratacao
  criadaEm: string
  ativadaEm?: string | null
  canceladaEm?: string | null
  versao: number
}

export interface ContratacaoCriacao {
  clienteId: string
  planoId: string
}

export interface HistoricoContratacao {
  id: string
  statusAnterior?: string | null
  statusNovo: string
  motivo?: string | null
  tipoResponsavel: TipoResponsavel
  registradoEm: string
}
