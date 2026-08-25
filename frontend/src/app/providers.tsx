import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import { QueryClientProvider, type QueryClient } from '@tanstack/react-query'
import { useState, type PropsWithChildren } from 'react'

import { criarClienteConsultas } from './query-client'

const tema = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#123b5d', dark: '#0a2840', light: '#2c648d', contrastText: '#ffffff' },
    secondary: { main: '#d9963d', dark: '#a8681f', light: '#f3c477', contrastText: '#20170b' },
    background: { default: '#f4f7fb', paper: '#ffffff' },
    text: { primary: '#16283b', secondary: '#526579' },
    success: { main: '#187c6b' },
    info: { main: '#2a6f97' },
  },
  typography: {
    fontFamily: 'Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    h1: { fontWeight: 800, letterSpacing: '-0.04em' },
    h2: {
      fontSize: 'clamp(2rem, 5vw, 3.5rem)',
      fontWeight: 800,
      lineHeight: 1.1,
      letterSpacing: '-0.04em',
    },
    h3: { fontWeight: 750, letterSpacing: '-0.02em' },
    button: { fontWeight: 700, textTransform: 'none' },
  },
  shape: { borderRadius: 16 },
  components: {
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { borderRadius: 10, paddingInline: 18 } },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderColor: 'rgba(18, 59, 93, 0.12)',
          boxShadow: '0 4px 18px rgba(18, 59, 93, 0.06)',
        },
      },
    },
    MuiChip: { styleOverrides: { root: { fontWeight: 700 } } },
    MuiTextField: { defaultProps: { fullWidth: true } },
  },
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
