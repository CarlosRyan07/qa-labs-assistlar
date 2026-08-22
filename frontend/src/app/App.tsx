import { AppBar, Box, Button, Container, Toolbar, Typography } from '@mui/material'
import { Link, Outlet } from 'react-router-dom'

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

      <AppBar
        position="static"
        component="header"
        elevation={0}
        sx={{
          background: 'linear-gradient(115deg, #0a2840 0%, #123b5d 58%, #1d5879 100%)',
          borderBottom: '1px solid rgba(255,255,255,0.13)',
        }}
      >
        <Toolbar sx={{ gap: 2, minHeight: { xs: 72, md: 84 }, flexWrap: 'wrap', py: 1.25 }}>
          <Typography component="span" variant="h6" sx={{ fontWeight: 700 }}>
            Ryan QA Labs — AssistLar
          </Typography>
          <Box
            component="nav"
            aria-label="Navegação principal"
            sx={{
              ml: { md: 'auto' },
              display: 'flex',
              gap: 0.5,
              width: { xs: '100%', md: 'auto' },
              overflowX: 'auto',
              '& a': { whiteSpace: 'nowrap' },
            }}
          >
            <Button component={Link} to="/" color="inherit">
              Início
            </Button>
            <Button component={Link} to="/clientes" color="inherit">
              Clientes
            </Button>
            <Button component={Link} to="/planos" color="inherit">
              Planos
            </Button>
            <Button component={Link} to="/elegibilidade" color="inherit">
              Elegibilidade
            </Button>
            <Button component={Link} to="/contratacoes/nova" color="inherit">
              Contratações
            </Button>
            <Button component={Link} to="/solicitacoes/nova" color="inherit">
              Solicitações
            </Button>
          </Box>
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

      <Box component="footer" sx={{ borderTop: '1px solid', borderColor: 'divider', py: 3 }}>
        <Container maxWidth="lg">
          <Typography variant="body2" color="text.secondary">
            Ryan QA Labs — AssistLar · Plataforma autoral para demonstrar Quality Engineering.
          </Typography>
        </Container>
      </Box>
    </Box>
  )
}
