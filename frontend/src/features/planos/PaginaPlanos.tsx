import { Alert, Box, Card, CardActions, CardContent, Chip, Stack, Typography } from '@mui/material'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

import { listarPlanos } from './api'

export function PaginaPlanos() {
  const planos = useQuery({
    queryKey: ['planos'],
    queryFn: listarPlanos,
    staleTime: 5 * 60 * 1000,
  })

  return (
    <Stack spacing={3}>
      <Box>
        <Typography component="h1" variant="h3" gutterBottom>
          Planos de assistência
        </Typography>
        <Typography color="text.secondary">
          Consulte as coberturas e os limites definidos pelo AssistLar.
        </Typography>
      </Box>

      {planos.isPending && <Typography role="status">Carregando planos...</Typography>}

      {planos.isError && (
        <Alert severity="error" role="alert">
          Não foi possível carregar os planos. Verifique se o backend está disponível e tente novamente.
        </Alert>
      )}

      {planos.data?.length === 0 && (
        <Alert severity="info">Nenhum plano ativo está disponível neste momento.</Alert>
      )}

      {planos.data && planos.data.length > 0 && (
        <Box
          component="section"
          aria-label="Planos disponíveis"
          sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' } }}
        >
          {planos.data.map((plano) => (
            <Card key={plano.id} variant="outlined">
              <CardContent>
                <Stack spacing={1.5}>
                  <Chip label={plano.codigo} size="small" sx={{ alignSelf: 'flex-start' }} />
                  <Typography component="h2" variant="h5">
                    {plano.nome}
                  </Typography>
                  <Typography color="text.secondary">{plano.descricao}</Typography>
                  <Typography>
                    {plano.coberturas.length}{' '}
                    {plano.coberturas.length === 1 ? 'cobertura disponível' : 'coberturas disponíveis'}
                  </Typography>
                </Stack>
              </CardContent>
              <CardActions>
                <Chip
                  component={Link}
                  to={`/planos/${plano.id}`}
                  clickable
                  color="primary"
                  label={`Ver detalhes de ${plano.nome}`}
                />
              </CardActions>
            </Card>
          ))}
        </Box>
      )}
    </Stack>
  )
}
