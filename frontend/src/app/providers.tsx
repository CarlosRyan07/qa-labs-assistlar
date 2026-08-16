import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import { QueryClientProvider, type QueryClient } from '@tanstack/react-query'
import { useState, type PropsWithChildren } from 'react'

import { criarClienteConsultas } from './query-client'

const tema = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#135d66', contrastText: '#ffffff' },
    secondary: { main: '#d97706', contrastText: '#1f1300' },
    background: { default: '#f5f7fa', paper: '#ffffff' },
  },
  typography: {
    fontFamily: 'Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    h2: {
      fontSize: 'clamp(2rem, 5vw, 3.5rem)',
      fontWeight: 750,
      lineHeight: 1.1,
    },
  },
  shape: { borderRadius: 12 },
})

interface AplicacaoProvidersProps extends PropsWithChildren {
  clienteConsultas?: QueryClient
}

export function AplicacaoProviders({ children, clienteConsultas }: AplicacaoProvidersProps) {
  const [clientePadrao] = useState(criarClienteConsultas)

  return (
    <ThemeProvider theme={tema}>
      <CssBaseline />
      <QueryClientProvider client={clienteConsultas ?? clientePadrao}>{children}</QueryClientProvider>
    </ThemeProvider>
  )
}
