import { afterEach, describe, expect, it, vi } from 'vitest'

import { requisitarJson } from '../../../shared/api/http'
import {
  abrirSolicitacao,
  cancelarSolicitacao,
  concluirSolicitacao,
  consultarHistoricoSolicitacao,
  consultarSolicitacao,
  iniciarSolicitacao,
} from './solicitacoes-api'

vi.mock('../../../shared/api/http', () => ({
  requisitarJson: vi.fn(),
}))

describe('solicitacoes-api', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('abre uma solicitacao enviando somente os campos aceitos pelo contrato', () => {
    const criacao = {
      contratacaoId: '10000000-0000-4000-8000-000000000001',
      tipoAssistencia: 'ELETRICISTA' as const,
      descricaoProblema: 'Tomada sem energia.',
    }

    void abrirSolicitacao(criacao)

    expect(requisitarJson).toHaveBeenCalledWith('/solicitacoes-assistencia', {
      method: 'POST',
      corpo: criacao,
    })
    expect(requisitarJson).not.toHaveBeenCalledWith(
      expect.anything(),
      expect.objectContaining({ corpo: expect.objectContaining({ tipoResponsavel: expect.anything() }) }),
    )
  })

  it('consulta a solicitacao e o historico pelo UUID codificado', () => {
    void consultarSolicitacao('id com espaco')
    void consultarHistoricoSolicitacao('id com espaco')

    expect(requisitarJson).toHaveBeenNthCalledWith(1, '/solicitacoes-assistencia/id%20com%20espaco')
    expect(requisitarJson).toHaveBeenNthCalledWith(
      2,
      '/solicitacoes-assistencia/id%20com%20espaco/historico',
    )
  })

  it('envia as transicoes sem atualizacao otimista ou versao no payload', () => {
    void iniciarSolicitacao('solicitacao-1')
    void concluirSolicitacao('solicitacao-1')
    void cancelarSolicitacao('solicitacao-1', 'Cliente solicitou o cancelamento.')

    expect(requisitarJson).toHaveBeenNthCalledWith(1, '/solicitacoes-assistencia/solicitacao-1/inicio', {
      method: 'POST',
    })
    expect(requisitarJson).toHaveBeenNthCalledWith(
      2,
      '/solicitacoes-assistencia/solicitacao-1/conclusao',
      { method: 'POST' },
    )
    expect(requisitarJson).toHaveBeenNthCalledWith(
      3,
      '/solicitacoes-assistencia/solicitacao-1/cancelamento',
      {
        method: 'POST',
        corpo: { motivo: 'Cliente solicitou o cancelamento.' },
      },
    )
  })

  it('envia objeto vazio ao cancelar uma solicitacao aberta sem motivo', () => {
    void cancelarSolicitacao('solicitacao-1')

    expect(requisitarJson).toHaveBeenCalledWith(
      '/solicitacoes-assistencia/solicitacao-1/cancelamento',
      {
        method: 'POST',
        corpo: {},
      },
    )
  })
})
