import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { describe, expect, it } from 'vitest'

import { AplicacaoProviders } from './providers'
import { rotas } from './router'

function renderizarAplicacao(caminho = '/') {
  const router = createMemoryRouter(rotas, { initialEntries: [caminho] })

  render(
    <AplicacaoProviders>
      <RouterProvider router={router} />
    </AplicacaoProviders>,
  )
}

describe('fundacao da aplicacao', () => {
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
  })

  it('apresenta os três princípios da fundação', () => {
    renderizarAplicacao()

    expect(screen.getByRole('heading', { name: 'Jornada guiada' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Contrato preservado' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Qualidade desde a fundação' })).toBeVisible()
  })

  it('orienta o retorno quando a rota não existe', () => {
    renderizarAplicacao('/rota-inexistente')

    expect(screen.getByRole('heading', { level: 1, name: 'Página não encontrada' })).toBeVisible()
    expect(screen.getByRole('link', { name: 'Voltar ao início' })).toHaveAttribute('href', '/')
  })
})
