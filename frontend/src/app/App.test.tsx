import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { listarPlanos } from '../features/planos'
import { AplicacaoProviders } from './providers'
import { rotas } from './router'

vi.mock('../features/planos', () => ({ listarPlanos: vi.fn() }))

const plano = {
  id: '10000000-0000-0000-0000-000000000001',
  codigo: 'ESSENCIAL',
  nome: 'Plano Essencial',
  descricao: 'Plano inicial',
  coberturas: [
    { tipoAssistencia: 'ELETRICISTA' as const, limiteUtilizacoes: 1 },
    { tipoAssistencia: 'ENCANADOR' as const, limiteUtilizacoes: 1 },
  ],
}

function renderizarAplicacao(caminho = '/') {
  const router = createMemoryRouter(rotas, { initialEntries: [caminho] })

  render(
    <AplicacaoProviders>
      <RouterProvider router={router} />
    </AplicacaoProviders>,
  )
}

describe('fundacao da aplicacao', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(listarPlanos).mockResolvedValue([plano])
  })

  it('apresenta identidade, conteudo principal e acesso por teclado', () => {
    renderizarAplicacao()

    expect(screen.getByText('Ryan QA Labs — AssistLar')).toBeInTheDocument()
    expect(screen.getByRole('main')).toHaveAttribute('id', 'conteudo-principal')
    expect(screen.getByRole('link', { name: 'Pular para o conteúdo' })).toHaveAttribute(
      'href',
      '#conteudo-principal',
    )
    expect(
      screen.getByRole('heading', {
        level: 1,
        name: 'Assistência residencial com uma jornada clara e testável',
      }),
    ).toBeVisible()
    expect(screen.getByRole('navigation', { name: 'Navegação principal' })).toBeInTheDocument()
  })

  it('apresenta os três princípios da fundação', () => {
    renderizarAplicacao()

    expect(screen.getByRole('heading', { name: 'Jornada guiada' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Contrato preservado' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Qualidade desde a fundação' })).toBeVisible()
  })

  it('resume somente planos e coberturas devolvidos pela API', async () => {
    renderizarAplicacao()

    expect(screen.getByText('Carregando resumo dos planos...')).toBeVisible()
    expect(await screen.findByRole('heading', { level: 3, name: 'Plano Essencial' })).toBeVisible()
    expect(screen.getByText('2 coberturas ativas')).toBeVisible()
    expect(screen.getByRole('link', { name: 'Consultar coberturas de Plano Essencial' })).toHaveAttribute(
      'href',
      `/planos/${plano.id}`,
    )
  })

  it('apresenta estado vazio sem inventar planos', async () => {
    vi.mocked(listarPlanos).mockResolvedValue([])
    renderizarAplicacao()

    expect(await screen.findByText('Nenhum plano ativo está disponível neste momento.')).toBeVisible()
    expect(screen.queryByRole('heading', { level: 3, name: 'Plano Essencial' })).not.toBeInTheDocument()
  })

  it('apresenta erro seguro quando o resumo não pode ser carregado', async () => {
    vi.mocked(listarPlanos).mockRejectedValue(new Error('SQL interno'))
    renderizarAplicacao()

    const alerta = await screen.findByRole('alert')
    expect(alerta).toHaveTextContent('Não foi possível carregar o resumo')
    expect(alerta).not.toHaveTextContent('SQL interno')
  })

  it('orienta o retorno quando a rota não existe', () => {
    renderizarAplicacao('/rota-inexistente')

    expect(screen.getByRole('heading', { level: 1, name: 'Página não encontrada' })).toBeVisible()
    expect(screen.getByRole('link', { name: 'Voltar ao início' })).toHaveAttribute('href', '/')
  })
})
