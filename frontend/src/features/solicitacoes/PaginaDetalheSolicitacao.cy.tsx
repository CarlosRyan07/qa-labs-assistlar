import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { mount } from 'cypress/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

import { PaginaDetalheSolicitacao } from './PaginaDetalheSolicitacao'

const solicitacaoId = '20000000-0000-4000-8000-000000000002'

function montarPagina() {
  const consultas = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })

  mount(
    <QueryClientProvider client={consultas}>
      <MemoryRouter initialEntries={[`/solicitacoes/${solicitacaoId}`]}>
        <Routes>
          <Route element={<PaginaDetalheSolicitacao />} path="/solicitacoes/:id" />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('PaginaDetalheSolicitacao no navegador', () => {
  it('exige motivo antes de cancelar uma solicitacao em atendimento', () => {
    cy.intercept('GET', `/api/solicitacoes-assistencia/${solicitacaoId}`, {
      body: {
        id: solicitacaoId,
        contratacaoId: '10000000-0000-4000-8000-000000000001',
        tipoAssistencia: 'ELETRICISTA',
        descricaoProblema: 'Tomada sem energia.',
        status: 'EM_ATENDIMENTO',
        abertaEm: '2026-08-16T12:00:00Z',
        iniciadaEm: '2026-08-16T12:10:00Z',
        versao: 1,
      },
    }).as('consultarSolicitacao')
    cy.intercept('GET', `/api/solicitacoes-assistencia/${solicitacaoId}/historico`, { body: [] }).as(
      'consultarHistorico',
    )

    montarPagina()
    cy.wait(['@consultarSolicitacao', '@consultarHistorico'])

    cy.contains('button', 'Cancelar solicitação').click()

    cy.contains('Informe o motivo para cancelar uma solicitação em atendimento.').should('be.visible')
    cy.get('@consultarSolicitacao.all').should('have.length', 1)
  })

  it('envia somente o motivo e apresenta o estado confirmado ao cancelar', () => {
    cy.intercept('GET', `/api/solicitacoes-assistencia/${solicitacaoId}`, {
      body: {
        id: solicitacaoId,
        contratacaoId: '10000000-0000-4000-8000-000000000001',
        tipoAssistencia: 'ELETRICISTA',
        descricaoProblema: 'Tomada sem energia.',
        status: 'EM_ATENDIMENTO',
        abertaEm: '2026-08-16T12:00:00Z',
        iniciadaEm: '2026-08-16T12:10:00Z',
        versao: 1,
      },
    })
    cy.intercept('GET', `/api/solicitacoes-assistencia/${solicitacaoId}/historico`, { body: [] })
    cy.intercept('POST', `/api/solicitacoes-assistencia/${solicitacaoId}/cancelamento`, (requisicao) => {
      expect(requisicao.body).to.deep.equal({ motivo: 'Risco removido.' })
      expect(requisicao.body).not.to.have.property('tipoResponsavel')
      requisicao.reply({
        body: {
          id: solicitacaoId,
          contratacaoId: '10000000-0000-4000-8000-000000000001',
          tipoAssistencia: 'ELETRICISTA',
          descricaoProblema: 'Tomada sem energia.',
          status: 'CANCELADA',
          abertaEm: '2026-08-16T12:00:00Z',
          iniciadaEm: '2026-08-16T12:10:00Z',
          canceladaEm: '2026-08-16T12:20:00Z',
          motivoCancelamento: 'Risco removido.',
          versao: 2,
        },
      })
    }).as('cancelarSolicitacao')

    montarPagina()
    cy.get('textarea[required]').type('  Risco removido.  ')
    cy.contains('button', 'Cancelar solicitação').click()

    cy.wait('@cancelarSolicitacao')
    cy.get('[aria-label="Status: Cancelada"]').should('be.visible')
    cy.get('[role="status"]').should('contain.text', 'Solicitação atualizada para Cancelada.')
  })
})
