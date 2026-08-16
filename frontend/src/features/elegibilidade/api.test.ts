import { beforeEach, describe, expect, it, vi } from 'vitest'

import { requisitarJson } from '../../shared/api/http'
import { consultarElegibilidade } from './api'

vi.mock('../../shared/api/http', () => ({ requisitarJson: vi.fn() }))

describe('api de elegibilidade', () => {
  beforeEach(() => vi.clearAllMocks())

  it('consulta a elegibilidade com os UUIDs codificados como parametros', () => {
    consultarElegibilidade('cliente com espaco', 'plano&especial')

    expect(requisitarJson).toHaveBeenCalledWith(
      '/elegibilidades?clienteId=cliente+com+espaco&planoId=plano%26especial',
    )
  })
})
