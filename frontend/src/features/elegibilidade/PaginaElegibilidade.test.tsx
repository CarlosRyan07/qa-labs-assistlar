import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { listarPlanos } from '../planos'
import { consultarElegibilidade } from './api'
import { PaginaElegibilidade } from './PaginaElegibilidade'

vi.mock('../planos', () => ({ listarPlanos: vi.fn() }))
vi.mock('./api', () => ({ consultarElegibilidade: vi.fn() }))

const plano = {
  id: '10000000-0000-0000-0000-000000000001',
  codigo: 'ESSENCIAL',
  nome: 'Plano Essencial',
  descricao: 'Plano inicial',
  coberturas: [],
}

function renderizar() {
  const cliente = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  render(
    <QueryClientProvider client={cliente}>
      <MemoryRouter>
        <PaginaElegibilidade />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('elegibilidade', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(listarPlanos).mockResolvedValue([plano])
  })

  it('avalia um cliente para o plano selecionado e orienta a contratação', async () => {
    vi.mocked(consultarElegibilidade).mockResolvedValue({
      clienteId: '20000000-0000-0000-0000-000000000001',
      planoId: plano.id,
      elegivel: true,
      motivos: [],
    })
    renderizar()

    fireEvent.change(screen.getByRole('textbox', { name: /UUID do cliente/ }), {
      target: { value: '20000000-0000-0000-0000-000000000001' },
    })
    const seletorPlano = screen.getByRole('combobox', { name: /Plano/ })
    await waitFor(() => expect(seletorPlano).toBeEnabled())
    fireEvent.change(seletorPlano, { target: { value: plano.id } })
    fireEvent.click(screen.getByRole('button', { name: 'Avaliar elegibilidade' }))

    expect(await screen.findByText('Cliente elegível')).toBeVisible()
    expect(screen.getByRole('link', { name: 'Criar contratação' })).toHaveAttribute(
      'href',
      `/contratacoes/nova?clienteId=20000000-0000-0000-0000-000000000001&planoId=${plano.id}`,
    )
  })

  it('apresenta todos os motivos devolvidos quando o cliente não é elegível', async () => {
    vi.mocked(consultarElegibilidade).mockResolvedValue({
      clienteId: '20000000-0000-0000-0000-000000000001',
      planoId: plano.id,
      elegivel: false,
      motivos: ['CLIENTE_INATIVO', 'CLIENTE_POSSUI_CONTRATACAO_VIGENTE'],
    })
    renderizar()

    fireEvent.change(screen.getByRole('textbox', { name: /UUID do cliente/ }), {
      target: { value: '20000000-0000-0000-0000-000000000001' },
    })
    const seletorPlano = screen.getByRole('combobox', { name: /Plano/ })
    await waitFor(() => expect(seletorPlano).toBeEnabled())
    fireEvent.change(seletorPlano, { target: { value: plano.id } })
    fireEvent.click(screen.getByRole('button', { name: 'Avaliar elegibilidade' }))

    expect(await screen.findByText('Cliente não elegível')).toBeVisible()
    expect(screen.getByText('O cliente está inativo.')).toBeVisible()
    expect(screen.getByText('O cliente já possui uma contratação pendente ou ativa.')).toBeVisible()
  })

  it('refaz a avaliação quando o mesmo cliente e plano são enviados novamente', async () => {
    const clienteId = '20000000-0000-0000-0000-000000000001'
    vi.mocked(consultarElegibilidade)
      .mockResolvedValueOnce({ clienteId, planoId: plano.id, elegivel: true, motivos: [] })
      .mockResolvedValueOnce({
        clienteId,
        planoId: plano.id,
        elegivel: false,
        motivos: ['CLIENTE_POSSUI_CONTRATACAO_VIGENTE'],
      })
    renderizar()

    fireEvent.change(screen.getByRole('textbox', { name: /UUID do cliente/ }), {
      target: { value: clienteId },
    })
    const seletorPlano = screen.getByRole('combobox', { name: /Plano/ })
    await waitFor(() => expect(seletorPlano).toBeEnabled())
    fireEvent.change(seletorPlano, { target: { value: plano.id } })

    fireEvent.click(screen.getByRole('button', { name: 'Avaliar elegibilidade' }))
    expect(await screen.findByText('Cliente elegível')).toBeVisible()
    fireEvent.click(screen.getByRole('button', { name: 'Avaliar elegibilidade' }))

    expect(await screen.findByText('Cliente não elegível')).toBeVisible()
    expect(consultarElegibilidade).toHaveBeenCalledTimes(2)
  })

  it('remove o resultado anterior quando os parâmetros são editados', async () => {
    vi.mocked(consultarElegibilidade).mockResolvedValue({
      clienteId: '20000000-0000-0000-0000-000000000001',
      planoId: plano.id,
      elegivel: true,
      motivos: [],
    })
    renderizar()

    const campoCliente = screen.getByRole('textbox', { name: /UUID do cliente/ })
    fireEvent.change(campoCliente, { target: { value: '20000000-0000-0000-0000-000000000001' } })
    const seletorPlano = screen.getByRole('combobox', { name: /Plano/ })
    await waitFor(() => expect(seletorPlano).toBeEnabled())
    fireEvent.change(seletorPlano, { target: { value: plano.id } })
    fireEvent.click(screen.getByRole('button', { name: 'Avaliar elegibilidade' }))
    expect(await screen.findByText('Cliente elegível')).toBeVisible()

    fireEvent.change(campoCliente, { target: { value: '30000000-0000-0000-0000-000000000003' } })

    expect(screen.queryByText('Cliente elegível')).not.toBeInTheDocument()
  })
})
