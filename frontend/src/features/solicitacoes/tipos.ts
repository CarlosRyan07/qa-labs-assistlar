export type TipoAssistencia = 'ELETRICISTA' | 'ENCANADOR' | 'CHAVEIRO'

export type StatusSolicitacao = 'ABERTA' | 'EM_ATENDIMENTO' | 'CONCLUIDA' | 'CANCELADA'

export interface Solicitacao {
  id: string
  contratacaoId: string
  tipoAssistencia: TipoAssistencia
  descricaoProblema: string
  status: StatusSolicitacao
  motivoCancelamento?: string | null
  abertaEm: string
  iniciadaEm?: string | null
  concluidaEm?: string | null
  canceladaEm?: string | null
  versao: number
}

export interface SolicitacaoCriacao {
  contratacaoId: string
  tipoAssistencia: TipoAssistencia
  descricaoProblema: string
}

export interface HistoricoSolicitacao {
  id: string
  statusAnterior: string | null
  statusNovo: string
  motivo: string | null
  tipoResponsavel: 'CLIENTE' | 'OPERADOR' | 'SISTEMA'
  registradoEm: string
}
