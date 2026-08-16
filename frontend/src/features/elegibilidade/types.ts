export type MotivoInelegibilidade =
  | 'CLIENTE_INATIVO'
  | 'CLIENTE_MENOR_DE_IDADE'
  | 'PLANO_INATIVO'
  | 'CLIENTE_POSSUI_CONTRATACAO_VIGENTE'

export interface ResultadoElegibilidade {
  clienteId: string
  planoId: string
  elegivel: boolean
  motivos: MotivoInelegibilidade[]
}
