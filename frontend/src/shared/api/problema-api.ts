export interface ErroValidacao {
  campo: string
  mensagem: string
}

export interface ProblemaApi {
  type?: string
  title: string
  status: number
  detail?: string
  instance?: string
  timestamp?: string
  erros?: ErroValidacao[]
}

export type TipoErroHttp = 'problema' | 'http' | 'rede'

interface OpcoesErroHttp {
  tipo: TipoErroHttp
  status?: number
  problema?: ProblemaApi
}

export class ErroHttp extends Error {
  readonly tipo: TipoErroHttp
  readonly status: number | undefined
  readonly problema: ProblemaApi | undefined

  constructor({ tipo, status, problema }: OpcoesErroHttp) {
    super(mensagemSegura(tipo, problema))
    this.name = 'ErroHttp'
    this.tipo = tipo
    this.status = status
    this.problema = problema
  }
}

export function ehProblemaApi(valor: unknown): valor is ProblemaApi {
  if (!ehObjeto(valor) || typeof valor.title !== 'string' || typeof valor.status !== 'number') {
    return false
  }

  return (
    ehTextoOpcional(valor.type) &&
    ehTextoOpcional(valor.detail) &&
    ehTextoOpcional(valor.instance) &&
    ehTextoOpcional(valor.timestamp) &&
    (valor.erros === undefined || (Array.isArray(valor.erros) && valor.erros.every(ehErroValidacao)))
  )
}

export function ehErroHttp(valor: unknown): valor is ErroHttp {
  return valor instanceof ErroHttp
}

function ehErroValidacao(valor: unknown): valor is ErroValidacao {
  return ehObjeto(valor) && typeof valor.campo === 'string' && typeof valor.mensagem === 'string'
}

function ehObjeto(valor: unknown): valor is Record<string, unknown> {
  return typeof valor === 'object' && valor !== null
}

function ehTextoOpcional(valor: unknown): boolean {
  return valor === undefined || typeof valor === 'string'
}

function mensagemSegura(tipo: TipoErroHttp, problema: ProblemaApi | undefined): string {
  if (tipo === 'problema' && problema?.detail) {
    return problema.detail
  }

  if (tipo === 'rede') {
    return 'Nao foi possivel comunicar com o servidor.'
  }

  return 'A operacao nao pode ser concluida no momento.'
}
