import { ambiente } from '../config/ambiente'
import { ehProblemaApi, ErroHttp, type ProblemaApi } from './problema-api'

export interface OpcoesRequisicao extends Omit<RequestInit, 'body' | 'headers'> {
  corpo?: unknown
  headers?: HeadersInit
}

export async function requisitarJson<T>(
  caminho: string,
  opcoes: OpcoesRequisicao = {},
): Promise<T> {
  const { corpo, headers: headersIniciais, ...opcoesFetch } = opcoes
  const possuiCorpo = corpo !== undefined
  const headers = criarHeaders(headersIniciais, possuiCorpo)

  let resposta: Response

  try {
    resposta = await fetch(construirUrl(caminho), {
      ...opcoesFetch,
      body: possuiCorpo ? JSON.stringify(corpo) : undefined,
      headers,
    })
  } catch {
    throw new ErroHttp({ tipo: 'rede' })
  }

  if (!resposta.ok) {
    const problema = await extrairProblema(resposta)

    throw new ErroHttp({
      tipo: problema ? 'problema' : 'http',
      status: resposta.status,
      problema,
    })
  }

  if (resposta.status === 204 || resposta.status === 205 || resposta.headers.get('content-length') === '0') {
    return undefined as T
  }

  return (await resposta.json()) as T
}

function construirUrl(caminho: string): string {
  const base = ambiente.apiUrl.replace(/\/+$/, '')
  const recurso = caminho.replace(/^\/+/, '')

  return recurso ? `${base}/${recurso}` : base
}

function criarHeaders(headersIniciais: HeadersInit | undefined, possuiCorpo: boolean): Headers {
  const headers = new Headers(headersIniciais)

  headers.set('Accept', 'application/json')

  if (possuiCorpo) {
    headers.set('Content-Type', 'application/json')
  } else {
    headers.delete('Content-Type')
  }

  return headers
}

async function extrairProblema(resposta: Response): Promise<ProblemaApi | undefined> {
  const contentType = resposta.headers.get('content-type') ?? ''

  if (!contentType.includes('application/problem+json') && !contentType.includes('application/json')) {
    return undefined
  }

  try {
    const conteudo: unknown = await resposta.json()

    return ehProblemaApi(conteudo) ? conteudo : undefined
  } catch {
    return undefined
  }
}
