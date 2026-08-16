import { AppBar, Box, Button, Container, Toolbar, Typography } from '@mui/material'
import { Outlet } from 'react-router-dom'

export function App() {
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Button
        component="a"
        href="#conteudo-principal"
        sx={{
          position: 'fixed',
          top: 8,
          left: 8,
          zIndex: 2000,
          transform: 'translateY(-150%)',
          bgcolor: 'background.paper',
          '&:focus': { transform: 'translateY(0)' },
        }}
      >
        Pular para o conteúdo
      </Button>

      <AppBar position="static" component="header" elevation={0}>
        <Toolbar>
          <Typography component="span" variant="h6" sx={{ fontWeight: 700 }}>
            Ryan QA Labs — AssistLar
          </Typography>
        </Toolbar>
      </AppBar>

      <Container
        component="main"
        id="conteudo-principal"
        tabIndex={-1}
        maxWidth="lg"
        sx={{ py: { xs: 4, md: 7 } }}
      >
        <Outlet />
      </Container>
    </Box>
  )
}
