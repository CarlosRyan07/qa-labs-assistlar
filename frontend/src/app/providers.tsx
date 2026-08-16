import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { PropsWithChildren } from 'react'

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

const clienteConsultas = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: false,
    },
  },
})

export function AplicacaoProviders({ children }: PropsWithChildren) {
  return (
    <ThemeProvider theme={tema}>
      <CssBaseline />
      <QueryClientProvider client={clienteConsultas}>{children}</QueryClientProvider>
    </ThemeProvider>
  )
}
