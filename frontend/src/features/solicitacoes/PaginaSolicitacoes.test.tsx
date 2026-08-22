import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { createMemoryRouter, RouterProvider, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { abrirSolicitacao } from './api/solicitacoes-api'
import { PaginaSolicitacoes } from './PaginaSolicitacoes'

vi.mock('./api/solicitacoes-api', () => ({
  abrirSolicitacao: vi.fn(),
}))

const contratacaoId = '10000000-0000-4000-8000-000000000001'
const solicitacaoId = '20000000-0000-4000-8000-000000000002'

function Destino() {
  const localizacao = useLocation()

  return <p>Destino: {localizacao.pathname}</p>
}

function renderizarPagina(entrada = '/solicitacoes/nova') {
  const consultas = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(
    [
      { path: '/solicitacoes/nova', element: <PaginaSolicitacoes /> },
      { path: '/solicitacoes/:id', element: <Destino /> },
    ],
    { initialEntries: [entrada] },
  )

  render(
    <QueryClientProvider client={consultas}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
}

describe('PaginaSolicitacoes', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('prefill a contratacao da jornada, abre a solicitacao e navega para o detalhe', async () => {
    vi.mocked(abrirSolicitacao).mockResolvedValue({
      id: solicitacaoId,
      contratacaoId,
      tipoAssistencia: 'ELETRICISTA',
      descricaoProblema: 'Tomada sem energia.',
      status: 'ABERTA',
      abertaEm: '2026-08-16T12:00:00Z',
      versao: 0,
    })
    renderizarPagina(`/solicitacoes/nova?contratacaoId=${contratacaoId}`)

    expect(screen.getByRole('textbox', { name: 'UUID da contratação' })).toHaveValue(contratacaoId)
    fireEvent.change(screen.getByRole('combobox', { name: /Tipo de assistência/ }), {
      target: { value: 'ELETRICISTA' },
    })
    fireEvent.change(screen.getByRole('textbox', { name: 'Descrição do problema' }), {
      target: { value: '  Tomada sem energia.  ' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Abrir solicitação' }))

    await waitFor(() => {
      expect(vi.mocked(abrirSolicitacao).mock.calls[0]?.[0]).toEqual({
        contratacaoId,
        tipoAssistencia: 'ELETRICISTA',
        descricaoProblema: 'Tomada sem energia.',
      })
    })
    expect(await screen.findByText(`Destino: /solicitacoes/${solicitacaoId}`)).toBeVisible()
  })

  it('orienta quando o UUID informado para consulta nao e valido', () => {
    renderizarPagina()

    fireEvent.change(screen.getByRole('textbox', { name: 'UUID da solicitação' }), {
      target: { value: 'id-invalido' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Consultar solicitação' }))

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Informe um UUID válido para consultar a solicitação.',
    )
  })

  it('navega para uma solicitacao conhecida quando o UUID e valido', async () => {
    renderizarPagina()

    fireEvent.change(screen.getByRole('textbox', { name: 'UUID da solicitação' }), {
      target: { value: solicitacaoId },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Consultar solicitação' }))

    expect(await screen.findByText(`Destino: /solicitacoes/${solicitacaoId}`)).toBeVisible()
  })
})
