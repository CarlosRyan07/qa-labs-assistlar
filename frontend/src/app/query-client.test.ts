import { describe, expect, it } from 'vitest'

import { criarClienteConsultas } from './query-client'

describe('criarClienteConsultas', () => {
  it('desabilita retentativas e refetch por foco para manter as telas deterministicas', () => {
    const cliente = criarClienteConsultas()

    expect(cliente.getDefaultOptions()).toMatchObject({
      queries: { retry: false, refetchOnWindowFocus: false },
      mutations: { retry: false },
    })
  })
})
