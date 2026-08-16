import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { requisitarJson } from './http'
import { ehErroHttp, ErroHttp } from './problema-api'

describe('requisitarJson', () => {
  const buscar = vi.fn()

  beforeEach(() => {
    buscar.mockReset()
    vi.stubGlobal('fetch', buscar)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('retorna o JSON de sucesso e monta a requisicao para a API', async () => {
    buscar.mockResolvedValue(
      new Response(JSON.stringify({ id: 'cliente-1' }), {
        status: 201,
        headers: { 'Content-Type': 'application/json' },
      }),
    )

    await expect(
      requisitarJson<{ id: string }>('/clientes', {
        method: 'POST',
        corpo: { nome: 'Ana' },
      }),
    ).resolves.toEqual({ id: 'cliente-1' })

    const [url, opcoes] = buscar.mock.calls[0] as [string, RequestInit]
    const headers = opcoes.headers as Headers

    expect(url).toBe('/api/clientes')
    expect(opcoes.body).toBe('{"nome":"Ana"}')
    expect(headers.get('Accept')).toBe('application/json')
    expect(headers.get('Content-Type')).toBe('application/json')
  })

  it('preserva o ProblemDetail estruturado em uma resposta de erro', async () => {
    buscar.mockResolvedValue(
      new Response(
        JSON.stringify({
          type: '/erros/dados-invalidos',
          title: 'Dados invalidos',
          status: 400,
          detail: 'Um ou mais campos estao invalidos.',
          erros: [{ campo: 'email', mensagem: 'deve ter formato de email' }],
        }),
        {
          status: 400,
          headers: { 'Content-Type': 'application/problem+json' },
        },
      ),
    )

    await expect(requisitarJson('/clientes')).rejects.toMatchObject({
      tipo: 'problema',
      status: 400,
      problema: {
        title: 'Dados invalidos',
        erros: [{ campo: 'email', mensagem: 'deve ter formato de email' }],
      },
    })
  })

  it('descarta o corpo de uma resposta HTTP nao estruturada', async () => {
    buscar.mockResolvedValue(
      new Response('<html>falha interna</html>', {
        status: 502,
        headers: { 'Content-Type': 'text/html' },
      }),
    )

    await expect(requisitarJson('/planos')).rejects.toMatchObject({
      tipo: 'http',
      status: 502,
      problema: undefined,
      message: 'A operacao nao pode ser concluida no momento.',
    })
  })

  it('normaliza falha de rede sem expor o erro original', async () => {
    buscar.mockRejectedValue(new Error('connect ECONNREFUSED 127.0.0.1'))

    try {
      await requisitarJson('/planos')
      throw new Error('A requisicao deveria falhar.')
    } catch (erro: unknown) {
      expect(ehErroHttp(erro)).toBe(true)
      expect(erro).toBeInstanceOf(ErroHttp)
      expect(erro).toMatchObject({
        tipo: 'rede',
        problema: undefined,
        message: 'Nao foi possivel comunicar com o servidor.',
      })
    }
  })

  it('aceita respostas sem conteudo e nao envia Content-Type sem corpo', async () => {
    buscar.mockResolvedValue(new Response(null, { status: 204 }))

    await expect(requisitarJson<void>('/clientes/cliente-1/inativacao', { method: 'POST' })).resolves.toBeUndefined()

    const [, opcoes] = buscar.mock.calls[0] as [string, RequestInit]
    const headers = opcoes.headers as Headers

    expect(headers.get('Accept')).toBe('application/json')
    expect(headers.get('Content-Type')).toBeNull()
  })
})
