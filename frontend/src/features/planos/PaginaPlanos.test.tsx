import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { consultarPlano, listarPlanos } from './api'
import { PaginaPlanoDetalhe } from './PaginaPlanoDetalhe'
import { PaginaPlanos } from './PaginaPlanos'

vi.mock('./api', () => ({ listarPlanos: vi.fn(), consultarPlano: vi.fn() }))

const plano = {
  id: '10000000-0000-0000-0000-000000000001',
  codigo: 'ESSENCIAL',
  nome: 'Plano Essencial',
  descricao: 'Coberturas essenciais para a residência.',
  coberturas: [
    { tipoAssistencia: 'ELETRICISTA' as const, limiteUtilizacoes: 1 },
    { tipoAssistencia: 'ENCANADOR' as const, limiteUtilizacoes: 1 },
  ],
}

function renderizar(elemento: React.ReactNode, rota = '/') {
  const cliente = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  render(
    <QueryClientProvider client={cliente}>
      <MemoryRouter initialEntries={[rota]}>{elemento}</MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('planos', () => {
  beforeEach(() => vi.clearAllMocks())

  it('lista somente os planos devolvidos pela API e suas coberturas disponíveis', async () => {
    vi.mocked(listarPlanos).mockResolvedValue([plano])

    renderizar(<PaginaPlanos />)

    expect(screen.getByRole('status')).toHaveTextContent('Carregando planos')
    expect(await screen.findByRole('heading', { name: 'Plano Essencial' })).toBeVisible()
    expect(screen.getByText('2 coberturas disponíveis')).toBeVisible()
    expect(screen.getByRole('link', { name: 'Ver detalhes de Plano Essencial' })).toHaveAttribute(
      'href',
      `/planos/${plano.id}`,
    )
  })

  it('mostra o detalhe e o limite de cada cobertura', async () => {
    vi.mocked(consultarPlano).mockResolvedValue(plano)

    renderizar(
      <Routes>
        <Route path="/planos/:planoId" element={<PaginaPlanoDetalhe />} />
      </Routes>,
      `/planos/${plano.id}`,
    )

    expect(await screen.findByRole('heading', { level: 1, name: 'Plano Essencial' })).toBeVisible()
    expect(screen.getByText('ELETRICISTA')).toBeVisible()
    expect(screen.getAllByText('Limite de 1 utilização(ões)')).toHaveLength(2)
  })

  it('apresenta erro observável quando a listagem falha', async () => {
    vi.mocked(listarPlanos).mockRejectedValue(new Error('indisponível'))

    renderizar(<PaginaPlanos />)

    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível carregar os planos')
  })
})
