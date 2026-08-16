import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'

import { AplicacaoProviders } from './app/providers'
import { criarRouter } from './app/router'
import './index.css'

const raiz = document.getElementById('root')

if (!raiz) {
  throw new Error('Elemento raiz da aplicacao nao encontrado.')
}

const router = criarRouter()

createRoot(raiz).render(
  <StrictMode>
    <AplicacaoProviders>
      <RouterProvider router={router} />
    </AplicacaoProviders>
  </StrictMode>,
)
