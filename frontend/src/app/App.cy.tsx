import { mount } from 'cypress/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

import { App } from './App'

describe('App no navegador', () => {
  it('mantem o atalho para o conteudo principal focalizavel no navegador', () => {
    mount(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<App />} path="/">
            <Route element={<h1>Conteúdo da página</h1>} index />
          </Route>
        </Routes>
      </MemoryRouter>,
    )

    cy.get('main#conteudo-principal').should('exist')
    cy.contains('a', 'Pular para o conteúdo')
      .focus()
      .should('have.focus')
      .and('have.attr', 'href', '#conteudo-principal')
  })
})
