import { Alert, Box, Button, Card, CardContent, Chip, List, ListItem, ListItemText, Stack, Typography } from '@mui/material'
import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

import { consultarPlano } from './api'

export function PaginaPlanoDetalhe() {
  const { planoId = '' } = useParams()
  const plano = useQuery({
    queryKey: ['plano', planoId],
    queryFn: () => consultarPlano(planoId),
    enabled: Boolean(planoId),
    staleTime: 5 * 60 * 1000,
  })

  return (
    <Stack spacing={3}>
      <Button component={Link} to="/planos" sx={{ alignSelf: 'flex-start' }}>
        Voltar aos planos
      </Button>

      {plano.isPending && <Typography role="status">Carregando plano...</Typography>}

      {plano.isError && (
        <Alert severity="error" role="alert">
          Não foi possível consultar este plano. Confira o identificador e tente novamente.
        </Alert>
      )}

      {plano.data && (
        <>
          <Box>
            <Chip label={plano.data.codigo} color="secondary" sx={{ mb: 1 }} />
            <Typography component="h1" variant="h3" gutterBottom>
              {plano.data.nome}
            </Typography>
            <Typography color="text.secondary">{plano.data.descricao}</Typography>
          </Box>

          <Card variant="outlined" component="section" aria-labelledby="coberturas-titulo">
            <CardContent>
              <Typography id="coberturas-titulo" component="h2" variant="h5">
                Coberturas
              </Typography>
              <List>
                {plano.data.coberturas.map((cobertura) => (
                  <ListItem key={cobertura.tipoAssistencia} disableGutters>
                    <ListItemText
                      primary={cobertura.tipoAssistencia}
                      secondary={`Limite de ${cobertura.limiteUtilizacoes} utilização(ões)`}
                    />
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>

          <Button
            component={Link}
            to={`/elegibilidade?planoId=${plano.data.id}`}
            variant="contained"
            sx={{ alignSelf: 'flex-start' }}
          >
            Avaliar elegibilidade para este plano
          </Button>
        </>
      )}
    </Stack>
  )
}
