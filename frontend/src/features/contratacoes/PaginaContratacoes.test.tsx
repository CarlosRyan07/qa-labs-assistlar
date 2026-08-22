import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { criarContratacao, listarContratacoes } from './api'
import { PaginaContratacoes } from './PaginaContratacoes'

vi.mock('./api', () => ({
  criarContratacao: vi.fn(),
  listarContratacoes: vi.fn(),
}))

const clienteId = '30000000-0000-0000-0000-000000000001'
const planoId = '10000000-0000-0000-0000-000000000001'
const contratacaoId = '20000000-0000-0000-0000-000000000001'

function renderizarPagina(caminho = '/contratacoes/nova') {
  const clienteConsultas = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(
    [
      { path: '/contratacoes/nova', element: <PaginaContratacoes /> },
      { path: '/contratacoes/:id', element: <h1>Destino da contratação</h1> },
    ],
    { initialEntries: [caminho] },
  )

  render(
    <QueryClientProvider client={clienteConsultas}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )

  return router
}

describe('PaginaContratacoes', () => {
  afterEach(() => vi.clearAllMocks())

  beforeEach(() => {
    vi.mocked(listarContratacoes).mockResolvedValue({ itens: [], pagina: 0, tamanho: 20, totalItens: 0, totalPaginas: 0 })
  })

  it('preenche a jornada pela elegibilidade, cria a contratacao e navega para o detalhe', async () => {
    vi.mocked(criarContratacao).mockResolvedValue({
      id: contratacaoId,
      clienteId,
      planoId,
      status: 'PENDENTE',
      criadaEm: '2026-08-16T12:00:00Z',
      versao: 0,
    })
    const router = renderizarPagina(
      `/contratacoes/nova?clienteId=${clienteId}&planoId=${planoId}`,
    )

    expect(screen.getByRole('textbox', { name: 'UUID do cliente' })).toHaveValue(clienteId)
    expect(screen.getByRole('textbox', { name: 'UUID do plano' })).toHaveValue(planoId)
    fireEvent.click(screen.getByRole('button', { name: 'Criar contratação pendente' }))

    await waitFor(() => {
      expect(vi.mocked(criarContratacao).mock.calls[0]?.[0]).toEqual({ clienteId, planoId })
    })
    expect(await screen.findByRole('heading', { name: 'Destino da contratação' })).toBeVisible()
    expect(router.state.location.pathname).toBe(`/contratacoes/${contratacaoId}`)
  })

  it('impede uma consulta local com UUID invalido', () => {
    const router = renderizarPagina()

    fireEvent.change(screen.getByRole('textbox', { name: 'UUID da contratação' }), {
      target: { value: 'id-invalido' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Consultar contratação' }))

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Informe um UUID válido para consultar a contratação.',
    )
    expect(router.state.location.pathname).toBe('/contratacoes/nova')
  })

  it('apresenta o erro da API sem expor detalhes internos', async () => {
    vi.mocked(criarContratacao).mockRejectedValue(new Error('classe.interna: sql secreto'))
    renderizarPagina(`/contratacoes/nova?clienteId=${clienteId}&planoId=${planoId}`)

    fireEvent.click(screen.getByRole('button', { name: 'Criar contratação pendente' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Não foi possível criar a contratação',
    )
    expect(screen.queryByText(/sql secreto/i)).not.toBeInTheDocument()
  })
})
