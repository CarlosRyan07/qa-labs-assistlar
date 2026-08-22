import { Button, Stack, Typography } from '@mui/material'
import { Link } from 'react-router-dom'

export function PaginaNaoEncontrada() {
  return (
    <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
      <Typography component="h1" variant="h3">
        Página não encontrada
      </Typography>
      <Typography color="text.secondary">
        O endereço informado não corresponde a uma página do AssistLar.
      </Typography>
      <Button component={Link} to="/" variant="contained">
        Voltar ao início
      </Button>
    </Stack>
  )
}
