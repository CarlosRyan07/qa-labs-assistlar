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
      { path: '*', element: <PaginaNaoEncontrada /> },
    ],
  },
]

export function criarRouter() {
  return createBrowserRouter(rotas)
}
