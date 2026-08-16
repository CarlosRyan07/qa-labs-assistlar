export type TipoAssistencia = 'ELETRICISTA' | 'ENCANADOR' | 'CHAVEIRO'

export interface CoberturaAssistencia {
  tipoAssistencia: TipoAssistencia
  limiteUtilizacoes: number
}

export interface PlanoAssistencia {
  id: string
  codigo: string
  nome: string
  descricao: string
  coberturas: CoberturaAssistencia[]
}
