import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import {
  ativarContratacao,
  cancelarContratacao,
  consultarContratacao,
  consultarHistoricoContratacao,
  criarContratacao,
} from './api'

describe('api de contratacoes', () => {
  const requisitar = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.stubGlobal('fetch', requisitar)
    requisitar.mockImplementation(
      () =>
        new Response(
          JSON.stringify({
            id: '20000000-0000-0000-0000-000000000001',
            clienteId: '30000000-0000-0000-0000-000000000001',
            planoId: '10000000-0000-0000-0000-000000000001',
            status: 'PENDENTE',
            criadaEm: '2026-08-16T12:00:00Z',
            versao: 0,
          }),
          { status: 200, headers: { 'Content-Type': 'application/json' } },
        ),
    )
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('consome os caminhos, verbos e payloads previstos no contrato', async () => {
    const id = '20000000-0000-0000-0000-000000000001'
    const criacao = {
      clienteId: '30000000-0000-0000-0000-000000000001',
      planoId: '10000000-0000-0000-0000-000000000001',
    }

    await criarContratacao(criacao)
    await consultarContratacao(id)
    await ativarContratacao(id)
    await cancelarContratacao(id, '  Solicitação do cliente  ')
    await consultarHistoricoContratacao(id)

    expect(requisitar.mock.calls.map(([url]) => url)).toEqual([
      '/api/contratacoes',
      `/api/contratacoes/${id}`,
      `/api/contratacoes/${id}/ativacao`,
      `/api/contratacoes/${id}/cancelamento`,
      `/api/contratacoes/${id}/historico`,
    ])
    expect(requisitar.mock.calls.map(([, opcoes]) => opcoes?.method)).toEqual([
      'POST',
      undefined,
      'POST',
      'POST',
      undefined,
    ])
    expect(requisitar.mock.calls[0]?.[1]?.body).toBe(JSON.stringify(criacao))
    expect(requisitar.mock.calls[3]?.[1]?.body).toBe(
      JSON.stringify({ motivo: 'Solicitação do cliente' }),
    )
    expect(requisitar.mock.calls[3]?.[1]?.body).not.toContain('tipoResponsavel')
  })

  it('envia objeto vazio quando o cancelamento nao possui motivo', async () => {
    const id = '20000000-0000-0000-0000-000000000001'

    await cancelarContratacao(id, '   ')

    expect(requisitar.mock.calls[0]?.[1]?.body).toBe('{}')
  })
})
