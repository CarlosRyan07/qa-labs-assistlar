import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  cancelarSolicitacao,
  consultarHistoricoSolicitacao,
  consultarSolicitacao,
  concluirSolicitacao,
  iniciarSolicitacao,
} from './api/solicitacoes-api'
import { PaginaDetalheSolicitacao } from './PaginaDetalheSolicitacao'
import type { Solicitacao } from './tipos'

vi.mock('./api/solicitacoes-api', () => ({
  cancelarSolicitacao: vi.fn(),
  concluirSolicitacao: vi.fn(),
  consultarHistoricoSolicitacao: vi.fn(),
  consultarSolicitacao: vi.fn(),
  iniciarSolicitacao: vi.fn(),
}))

const solicitacaoId = '20000000-0000-4000-8000-000000000002'
const solicitacaoAberta: Solicitacao = {
  id: solicitacaoId,
  contratacaoId: '10000000-0000-4000-8000-000000000001',
  tipoAssistencia: 'ELETRICISTA',
  descricaoProblema: 'Tomada sem energia.',
  status: 'ABERTA',
  abertaEm: '2026-08-16T12:00:00Z',
  versao: 0,
}

function renderizarPagina(caminho = `/solicitacoes/${solicitacaoId}`) {
  const consultas = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(
    [{ path: '/solicitacoes/:id', element: <PaginaDetalheSolicitacao /> }],
    { initialEntries: [caminho] },
  )

  render(
    <QueryClientProvider client={consultas}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
}

describe('PaginaDetalheSolicitacao', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('inicia a solicitacao aberta e apresenta o estado confirmado pela API', async () => {
    const emAtendimento: Solicitacao = {
      ...solicitacaoAberta,
      status: 'EM_ATENDIMENTO',
      iniciadaEm: '2026-08-16T12:10:00Z',
      versao: 1,
    }
    vi.mocked(consultarSolicitacao).mockResolvedValue(solicitacaoAberta)
    vi.mocked(consultarHistoricoSolicitacao).mockResolvedValue([])
    vi.mocked(iniciarSolicitacao).mockResolvedValue(emAtendimento)
    renderizarPagina()

    fireEvent.click(await screen.findByRole('button', { name: 'Iniciar atendimento' }))

    await waitFor(() => {
      expect(iniciarSolicitacao).toHaveBeenCalledWith(solicitacaoId)
    })
    expect(await screen.findByLabelText('Status: Em atendimento')).toBeVisible()
    expect(screen.getByRole('button', { name: 'Concluir atendimento' })).toBeVisible()
    expect(screen.getByRole('status')).toHaveTextContent('Solicitação atualizada para Em atendimento.')
  })

  it('exige motivo na interface antes de cancelar uma solicitacao em atendimento', async () => {
    vi.mocked(consultarSolicitacao).mockResolvedValue({
      ...solicitacaoAberta,
      status: 'EM_ATENDIMENTO',
      iniciadaEm: '2026-08-16T12:10:00Z',
      versao: 1,
    })
    vi.mocked(consultarHistoricoSolicitacao).mockResolvedValue([])
    renderizarPagina()

    const formulario = await screen.findByRole('form', { name: 'Cancelar solicitação' })
    fireEvent.submit(formulario)

    expect(screen.getByText('Informe o motivo para cancelar uma solicitação em atendimento.')).toBeVisible()
    expect(cancelarSolicitacao).not.toHaveBeenCalled()
  })

  it('cancela em atendimento com motivo e nao envia tipoResponsavel', async () => {
    const emAtendimento: Solicitacao = {
      ...solicitacaoAberta,
      status: 'EM_ATENDIMENTO',
      iniciadaEm: '2026-08-16T12:10:00Z',
      versao: 1,
    }
    vi.mocked(consultarSolicitacao).mockResolvedValue(emAtendimento)
    vi.mocked(consultarHistoricoSolicitacao).mockResolvedValue([])
    vi.mocked(cancelarSolicitacao).mockResolvedValue({
      ...emAtendimento,
      status: 'CANCELADA',
      motivoCancelamento: 'Risco elétrico removido.',
      canceladaEm: '2026-08-16T12:20:00Z',
      versao: 2,
    })
    renderizarPagina()

    fireEvent.change(await screen.findByRole('textbox', { name: 'Motivo do cancelamento' }), {
      target: { value: '  Risco elétrico removido.  ' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Cancelar solicitação' }))

    await waitFor(() => {
      expect(cancelarSolicitacao).toHaveBeenCalledWith(solicitacaoId, 'Risco elétrico removido.')
    })
    expect(await screen.findByLabelText('Status: Cancelada')).toBeVisible()
    expect(cancelarSolicitacao).not.toHaveBeenCalledWith(
      expect.anything(),
      expect.objectContaining({ tipoResponsavel: expect.anything() }),
    )
  })

  it('conclui o atendimento e mostra o estado devolvido pela API', async () => {
    const emAtendimento: Solicitacao = {
      ...solicitacaoAberta,
      status: 'EM_ATENDIMENTO',
      iniciadaEm: '2026-08-16T12:10:00Z',
      versao: 1,
    }
    vi.mocked(consultarSolicitacao).mockResolvedValue(emAtendimento)
    vi.mocked(consultarHistoricoSolicitacao).mockResolvedValue([])
    vi.mocked(concluirSolicitacao).mockResolvedValue({
      ...emAtendimento,
      status: 'CONCLUIDA',
      concluidaEm: '2026-08-16T12:20:00Z',
      versao: 2,
    })
    renderizarPagina()

    fireEvent.click(await screen.findByRole('button', { name: 'Concluir atendimento' }))

    await waitFor(() => expect(concluirSolicitacao).toHaveBeenCalledWith(solicitacaoId))
    expect(await screen.findByLabelText('Status: Concluída')).toBeVisible()
    expect(screen.queryByRole('form', { name: 'Cancelar solicitação' })).not.toBeInTheDocument()
  })

  it('cancela uma solicitacao aberta sem motivo', async () => {
    vi.mocked(consultarSolicitacao).mockResolvedValue(solicitacaoAberta)
    vi.mocked(consultarHistoricoSolicitacao).mockResolvedValue([])
    vi.mocked(cancelarSolicitacao).mockResolvedValue({
      ...solicitacaoAberta,
      status: 'CANCELADA',
      canceladaEm: '2026-08-16T12:20:00Z',
      versao: 1,
    })
    renderizarPagina()

    fireEvent.click(await screen.findByRole('button', { name: 'Cancelar solicitação' }))

    await waitFor(() => expect(cancelarSolicitacao).toHaveBeenCalledWith(solicitacaoId, undefined))
    expect(await screen.findByLabelText('Status: Cancelada')).toBeVisible()
  })

  it('apresenta o historico com estado, responsavel e motivo retornados pela API', async () => {
    vi.mocked(consultarSolicitacao).mockResolvedValue(solicitacaoAberta)
    vi.mocked(consultarHistoricoSolicitacao).mockResolvedValue([
      {
        id: '30000000-0000-4000-8000-000000000003',
        statusAnterior: null,
        statusNovo: 'ABERTA',
        motivo: 'Solicitação criada.',
        tipoResponsavel: 'CLIENTE',
        registradoEm: '2026-08-16T12:00:00Z',
      },
    ])
    renderizarPagina()

    const historico = await screen.findByRole('list', { name: 'Histórico da solicitação' })
    expect(historico).toHaveTextContent('Criação → Aberta')
    expect(historico).toHaveTextContent('responsável: cliente')
    expect(historico).toHaveTextContent('Motivo: Solicitação criada.')
  })

  it('nao consulta a API quando o UUID da rota e invalido', () => {
    renderizarPagina('/solicitacoes/id-invalido')

    expect(screen.getByText('O UUID informado para a solicitação não é válido.')).toBeVisible()
    expect(consultarSolicitacao).not.toHaveBeenCalled()
    expect(consultarHistoricoSolicitacao).not.toHaveBeenCalled()
  })
})
