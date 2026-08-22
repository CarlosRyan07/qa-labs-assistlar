import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  ativarContratacao,
  cancelarContratacao,
  consultarContratacao,
  consultarHistoricoContratacao,
} from './api'
import { PaginaDetalheContratacao } from './PaginaDetalheContratacao'
import type { Contratacao, HistoricoContratacao } from './tipos'

vi.mock('./api', () => ({
  ativarContratacao: vi.fn(),
  cancelarContratacao: vi.fn(),
  consultarContratacao: vi.fn(),
  consultarHistoricoContratacao: vi.fn(),
}))

const contratacaoId = '20000000-0000-0000-0000-000000000001'
const contratacaoPendente: Contratacao = {
  id: contratacaoId,
  clienteId: '30000000-0000-0000-0000-000000000001',
  planoId: '10000000-0000-0000-0000-000000000001',
  status: 'PENDENTE',
  criadaEm: '2026-08-16T12:00:00Z',
  versao: 0,
}
const historicoInicial: HistoricoContratacao = {
  id: '40000000-0000-0000-0000-000000000001',
  statusAnterior: null,
  statusNovo: 'PENDENTE',
  tipoResponsavel: 'SISTEMA',
  registradoEm: '2026-08-16T12:00:00Z',
}

function renderizarPagina(caminho = `/contratacoes/${contratacaoId}`) {
  const clienteConsultas = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(
    [{ path: '/contratacoes/:id', element: <PaginaDetalheContratacao /> }],
    { initialEntries: [caminho] },
  )

  render(
    <QueryClientProvider client={clienteConsultas}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
}

describe('PaginaDetalheContratacao', () => {
  afterEach(() => vi.clearAllMocks())

  it('ativa uma contratacao pendente e atualiza detalhe e historico confirmados pela API', async () => {
    const contratacaoAtiva: Contratacao = {
      ...contratacaoPendente,
      status: 'ATIVA',
      ativadaEm: '2026-08-16T12:05:00Z',
      versao: 1,
    }
    vi.mocked(consultarContratacao).mockResolvedValue(contratacaoPendente)
    vi.mocked(consultarHistoricoContratacao)
      .mockResolvedValueOnce([historicoInicial])
      .mockResolvedValue([
        historicoInicial,
        {
          ...historicoInicial,
          id: '40000000-0000-0000-0000-000000000002',
          statusAnterior: 'PENDENTE',
          statusNovo: 'ATIVA',
          tipoResponsavel: 'OPERADOR',
        },
      ])
    vi.mocked(ativarContratacao).mockResolvedValue(contratacaoAtiva)
    renderizarPagina()

    expect(await screen.findByLabelText('Status: Pendente')).toBeVisible()
    expect(await screen.findByText('Responsável: Sistema', { exact: false })).toBeVisible()
    fireEvent.click(screen.getByRole('button', { name: 'Ativar contratação' }))

    await waitFor(() => expect(ativarContratacao).toHaveBeenCalledWith(contratacaoId))
    expect(await screen.findByLabelText('Status: Ativa')).toBeVisible()
    expect(
      screen.getByRole('link', { name: 'Abrir solicitação de assistência' }),
    ).toHaveAttribute('href', `/solicitacoes/nova?contratacaoId=${contratacaoId}`)
    await waitFor(() => expect(consultarHistoricoContratacao).toHaveBeenCalledTimes(2))
    expect(await screen.findByText('Pendente → Ativa')).toBeVisible()
  })

  it('cancela uma contratacao ativa com motivo opcional informado', async () => {
    const ativa: Contratacao = { ...contratacaoPendente, status: 'ATIVA', versao: 1 }
    vi.mocked(consultarContratacao).mockResolvedValue(ativa)
    vi.mocked(consultarHistoricoContratacao).mockResolvedValue([historicoInicial])
    vi.mocked(cancelarContratacao).mockResolvedValue({
      ...ativa,
      status: 'CANCELADA',
      canceladaEm: '2026-08-16T12:10:00Z',
      versao: 2,
    })
    renderizarPagina()

    fireEvent.change(await screen.findByRole('textbox', { name: 'Motivo do cancelamento' }), {
      target: { value: 'Mudança de necessidade' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Cancelar contratação' }))

    await waitFor(() => {
      expect(cancelarContratacao).toHaveBeenCalledWith(contratacaoId, 'Mudança de necessidade')
    })
    expect(await screen.findByLabelText('Status: Cancelada')).toBeVisible()
    expect(screen.queryByRole('button', { name: 'Cancelar contratação' })).not.toBeInTheDocument()
  })

  it('nao consulta a API quando o UUID da rota e invalido', () => {
    renderizarPagina('/contratacoes/nao-e-uuid')

    expect(screen.getByText('O UUID informado para a contratação não é válido.')).toBeVisible()
    expect(consultarContratacao).not.toHaveBeenCalled()
    expect(consultarHistoricoContratacao).not.toHaveBeenCalled()
  })

  it('mantem o detalhe visivel quando somente o historico falha', async () => {
    vi.mocked(consultarContratacao).mockResolvedValue(contratacaoPendente)
    vi.mocked(consultarHistoricoContratacao).mockRejectedValue(new Error('falha interna'))
    renderizarPagina()

    expect(await screen.findByLabelText('Status: Pendente')).toBeVisible()
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Não foi possível carregar o histórico',
    )
    expect(screen.getByRole('button', { name: 'Ativar contratação' })).toBeEnabled()
  })
})
