import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { consultarCliente, inativarCliente, reativarCliente } from './api/clientes-api'
import { PaginaDetalheCliente } from './PaginaDetalheCliente'

vi.mock('./api/clientes-api', () => ({
  consultarCliente: vi.fn(),
  inativarCliente: vi.fn(),
  reativarCliente: vi.fn(),
}))

const idCliente = '8d8c18af-760d-4b37-938b-b7c11b68be34'

const clienteAtivo = {
  id: idCliente,
  nome: 'Ana Silva',
  email: 'ana@exemplo.com',
  dataNascimento: '1990-05-10',
  status: 'ATIVO' as const,
  criadoEm: '2026-08-16T12:00:00Z',
  atualizadoEm: '2026-08-16T12:00:00Z',
}

function renderizarPagina(caminho = `/clientes/${idCliente}`) {
  const clienteConsultas = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(
    [{ path: '/clientes/:id', element: <PaginaDetalheCliente /> }],
    { initialEntries: [caminho] },
  )

  render(
    <QueryClientProvider client={clienteConsultas}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
}

describe('PaginaDetalheCliente', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('consulta o cliente e reflete a inativacao confirmada pela API', async () => {
    vi.mocked(consultarCliente).mockResolvedValue(clienteAtivo)
    vi.mocked(inativarCliente).mockResolvedValue({ ...clienteAtivo, status: 'INATIVO' })
    renderizarPagina()

    expect(await screen.findByRole('heading', { name: 'Ana Silva' })).toBeVisible()
    fireEvent.click(screen.getByRole('button', { name: 'Inativar cliente' }))

    await waitFor(() => {
      expect(inativarCliente).toHaveBeenCalledWith(idCliente)
    })
    expect(await screen.findByRole('button', { name: 'Reativar cliente' })).toBeVisible()
    expect(screen.getByLabelText('Status: Inativo')).toBeVisible()
  })

  it('não consulta a API quando o UUID da rota é inválido', () => {
    renderizarPagina('/clientes/nao-e-um-uuid')

    expect(screen.getByText('O UUID informado para o cliente não é válido.')).toBeVisible()
    expect(consultarCliente).not.toHaveBeenCalled()
  })

  it('informa o erro de consulta sem expor detalhes internos', async () => {
    vi.mocked(consultarCliente).mockRejectedValue(new Error('falha interna detalhada'))
    renderizarPagina()

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Não foi possível carregar o cliente solicitado',
    )
    expect(screen.queryByText('falha interna detalhada')).not.toBeInTheDocument()
  })

  it('envia a reativacao para cliente inativo', async () => {
    vi.mocked(consultarCliente).mockResolvedValue({ ...clienteAtivo, status: 'INATIVO' })
    vi.mocked(reativarCliente).mockResolvedValue(clienteAtivo)
    renderizarPagina()

    fireEvent.click(await screen.findByRole('button', { name: 'Reativar cliente' }))

    await waitFor(() => {
      expect(reativarCliente).toHaveBeenCalledWith(idCliente)
    })
    expect(await screen.findByLabelText('Status: Ativo')).toBeVisible()
  })
})
