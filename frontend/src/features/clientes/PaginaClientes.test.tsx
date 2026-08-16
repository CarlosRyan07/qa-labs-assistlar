import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { createMemoryRouter, RouterProvider, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { cadastrarCliente } from './api/clientes-api'
import { PaginaClientes } from './PaginaClientes'

vi.mock('./api/clientes-api', () => ({
  cadastrarCliente: vi.fn(),
}))

function Destino() {
  const localizacao = useLocation()

  return <p>Destino: {localizacao.pathname}</p>
}

function renderizarPagina() {
  const clienteConsultas = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(
    [
      { path: '/clientes', element: <PaginaClientes /> },
      { path: '/clientes/:id', element: <Destino /> },
    ],
    { initialEntries: ['/clientes'] },
  )

  render(
    <QueryClientProvider client={clienteConsultas}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
}

describe('PaginaClientes', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('cadastra o cliente e navega para o detalhe retornado pela API', async () => {
    vi.mocked(cadastrarCliente).mockResolvedValue({
      id: '8d8c18af-760d-4b37-938b-b7c11b68be34',
      nome: 'Ana Silva',
      email: 'ana@exemplo.com',
      dataNascimento: '1990-05-10',
      status: 'ATIVO',
      criadoEm: '2026-08-16T12:00:00Z',
      atualizadoEm: '2026-08-16T12:00:00Z',
    })
    renderizarPagina()

    fireEvent.change(screen.getByRole('textbox', { name: /Nome/ }), { target: { value: 'Ana Silva' } })
    fireEvent.change(screen.getByRole('textbox', { name: /E-mail/ }), {
      target: { value: 'ana@exemplo.com' },
    })
    fireEvent.change(screen.getByLabelText(/Data de nascimento/), { target: { value: '1990-05-10' } })
    fireEvent.click(screen.getByRole('button', { name: 'Cadastrar cliente' }))

    await waitFor(() => {
      expect(vi.mocked(cadastrarCliente).mock.calls[0]?.[0]).toEqual({
        nome: 'Ana Silva',
        email: 'ana@exemplo.com',
        dataNascimento: '1990-05-10',
      })
    })
    expect(await screen.findByText('Destino: /clientes/8d8c18af-760d-4b37-938b-b7c11b68be34')).toBeVisible()
  })

  it('orienta quando o UUID informado para consulta não é válido', () => {
    renderizarPagina()

    fireEvent.change(screen.getByRole('textbox', { name: /UUID do cliente/ }), {
      target: { value: 'nao-e-um-uuid' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Consultar cliente' }))

    expect(screen.getByRole('alert')).toHaveTextContent('Informe um UUID válido para consultar o cliente.')
  })

  it('navega para a consulta usando um UUID válido', async () => {
    renderizarPagina()
    const id = '8d8c18af-760d-4b37-938b-b7c11b68be34'

    fireEvent.change(screen.getByRole('textbox', { name: /UUID do cliente/ }), { target: { value: id } })
    fireEvent.click(screen.getByRole('button', { name: 'Consultar cliente' }))

    expect(await screen.findByText(`Destino: /clientes/${id}`)).toBeVisible()
  })
})
