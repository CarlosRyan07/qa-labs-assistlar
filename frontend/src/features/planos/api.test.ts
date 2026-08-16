import { beforeEach, describe, expect, it, vi } from 'vitest'

import { requisitarJson } from '../../shared/api/http'
import { consultarPlano, listarPlanos } from './api'

vi.mock('../../shared/api/http', () => ({ requisitarJson: vi.fn() }))

describe('api de planos', () => {
  beforeEach(() => vi.clearAllMocks())

  it('consulta a colecao e o detalhe pelos caminhos do contrato', () => {
    listarPlanos()
    consultarPlano('10000000-0000-0000-0000-000000000001')

    expect(requisitarJson).toHaveBeenNthCalledWith(1, '/planos')
    expect(requisitarJson).toHaveBeenNthCalledWith(2, '/planos/10000000-0000-0000-0000-000000000001')
  })
})
