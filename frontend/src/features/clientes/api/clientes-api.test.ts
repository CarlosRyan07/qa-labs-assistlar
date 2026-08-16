import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { cadastrarCliente, consultarCliente, inativarCliente, reativarCliente } from './clientes-api'

describe('api de clientes', () => {
  const requisitar = vi.fn()

  beforeEach(() => {
    vi.stubGlobal('fetch', requisitar)
    requisitar.mockImplementation(
      () => new Response(
        JSON.stringify({
          id: '8d8c18af-760d-4b37-938b-b7c11b68be34',
          nome: 'Ana Silva',
          email: 'ana@exemplo.com',
          dataNascimento: '1990-05-10',
          status: 'ATIVO',
          criadoEm: '2026-08-16T12:00:00Z',
          atualizadoEm: '2026-08-16T12:00:00Z',
        }),
        { status: 200, headers: { 'Content-Type': 'application/json' } },
      ),
    )
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('consome os caminhos e verbos previstos no contrato', async () => {
    const id = '8d8c18af-760d-4b37-938b-b7c11b68be34'

    await cadastrarCliente({ nome: 'Ana Silva', email: 'ana@exemplo.com', dataNascimento: '1990-05-10' })
    await consultarCliente(id)
    await inativarCliente(id)
    await reativarCliente(id)

    expect(requisitar.mock.calls.map(([url]) => url)).toEqual([
      '/api/clientes',
      `/api/clientes/${id}`,
      `/api/clientes/${id}/inativacao`,
      `/api/clientes/${id}/reativacao`,
    ])
    expect(requisitar.mock.calls.map(([, opcoes]) => opcoes?.method)).toEqual([
      'POST',
      undefined,
      'POST',
      'POST',
    ])
    expect(requisitar.mock.calls[0]?.[1]?.body).toBe(
      '{"nome":"Ana Silva","email":"ana@exemplo.com","dataNascimento":"1990-05-10"}',
    )
  })
})
