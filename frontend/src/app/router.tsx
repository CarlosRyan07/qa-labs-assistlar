import { createBrowserRouter, type RouteObject } from 'react-router-dom'

import { App } from './App'
import { PaginaInicial } from './PaginaInicial'
import { PaginaNaoEncontrada } from './PaginaNaoEncontrada'

export const rotas: RouteObject[] = [
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <PaginaInicial /> },
      {
        path: 'clientes',
        lazy: async () => ({ Component: (await import('../features/clientes/PaginaClientes')).PaginaClientes }),
      },
      {
        path: 'clientes/:id',
        lazy: async () => ({
          Component: (await import('../features/clientes/PaginaDetalheCliente')).PaginaDetalheCliente,
        }),
      },
      {
        path: 'planos',
        lazy: async () => ({ Component: (await import('../features/planos/PaginaPlanos')).PaginaPlanos }),
      },
      {
        path: 'planos/:planoId',
        lazy: async () => ({
          Component: (await import('../features/planos/PaginaPlanoDetalhe')).PaginaPlanoDetalhe,
        }),
      },
      {
        path: 'elegibilidade',
        lazy: async () => ({
          Component: (await import('../features/elegibilidade/PaginaElegibilidade')).PaginaElegibilidade,
        }),
      },
      {
        path: 'contratacoes/nova',
        lazy: async () => ({
          Component: (await import('../features/contratacoes/PaginaContratacoes')).PaginaContratacoes,
        }),
      },
      {
        path: 'contratacoes/:id',
        lazy: async () => ({
          Component: (await import('../features/contratacoes/PaginaDetalheContratacao'))
            .PaginaDetalheContratacao,
        }),
      },
      {
        path: 'solicitacoes/nova',
        lazy: async () => ({
          Component: (await import('../features/solicitacoes/PaginaSolicitacoes')).PaginaSolicitacoes,
        }),
      },
      {
        path: 'solicitacoes/:id',
        lazy: async () => ({
          Component: (await import('../features/solicitacoes/PaginaDetalheSolicitacao'))
            .PaginaDetalheSolicitacao,
        }),
      },
      { path: '*', element: <PaginaNaoEncontrada /> },
    ],
  },
]

export function criarRouter() {
  return createBrowserRouter(rotas)
}
