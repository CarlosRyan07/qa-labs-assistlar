import { Alert, Box, Button, Card, CardContent, Chip, Stack, Typography } from '@mui/material'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

import { AlertaErro } from '../shared/components/AlertaErro'
import { listarPlanos } from '../features/planos'

const pilares = [
  {
    titulo: 'Jornada guiada',
    descricao: 'Os recursos são conectados pelos UUIDs retornados pela API.',
  },
  {
    titulo: 'Contrato preservado',
    descricao: 'A Web consome somente as operações existentes no backend.',
  },
  {
    titulo: 'Qualidade desde a fundação',
    descricao: 'Acessibilidade, testes e feedback rápido fazem parte da arquitetura.',
  },
]

export function PaginaInicial() {
  const planos = useQuery({ queryKey: ['planos'], queryFn: listarPlanos, staleTime: 5 * 60 * 1000 })

  return (
    <Stack spacing={4}>
      <Box>
        <Chip label="Frontend v0.2.0" color="secondary" sx={{ mb: 2 }} />
        <Typography component="h1" variant="h2" gutterBottom>
          Assistência residencial com uma jornada clara e testável
        </Typography>
        <Typography component="p" variant="h6" color="text.secondary" sx={{ maxWidth: 760 }}>
          Interface Web autoral para demonstrar práticas de Full Stack Quality
          Engineering sobre o domínio do AssistLar.
        </Typography>
      </Box>

      <Alert severity="info" role="status">
        A API lista apenas planos. Clientes, contratações e solicitações são retomados por UUID,
        sem simular dados que o backend não fornece.
      </Alert>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <Button component={Link} to="/clientes" variant="contained">
          Iniciar pelo cliente
        </Button>
        <Button component={Link} to="/planos" variant="outlined">
          Conhecer os planos
        </Button>
        <Button component={Link} to="/elegibilidade" variant="outlined">
          Avaliar elegibilidade
        </Button>
      </Stack>

      <Box component="section" aria-labelledby="resumo-planos-titulo">
        <Typography id="resumo-planos-titulo" component="h2" variant="h4" gutterBottom>
          Planos disponíveis
        </Typography>

        {planos.isPending && <Typography role="status">Carregando resumo dos planos...</Typography>}
        {planos.isError && <AlertaErro erro={planos.error} titulo="Não foi possível carregar o resumo" />}
        {planos.data?.length === 0 && (
          <Alert severity="info">Nenhum plano ativo está disponível neste momento.</Alert>
        )}

        {planos.data && planos.data.length > 0 && (
          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' } }}>
            {planos.data.map((plano) => (
              <Card key={plano.id} variant="outlined">
                <CardContent>
                  <Stack spacing={1.5}>
                    <Chip label={plano.codigo} size="small" sx={{ alignSelf: 'flex-start' }} />
                    <Typography component="h3" variant="h5">
                      {plano.nome}
                    </Typography>
                    <Typography color="text.secondary">
                      {plano.coberturas.length}{' '}
                      {plano.coberturas.length === 1 ? 'cobertura ativa' : 'coberturas ativas'}
                    </Typography>
                    <Button component={Link} to={`/planos/${plano.id}`} sx={{ alignSelf: 'flex-start' }}>
                      Consultar coberturas de {plano.nome}
                    </Button>
                  </Stack>
                </CardContent>
              </Card>
            ))}
          </Box>
        )}
      </Box>

      <Box
        component="section"
        aria-labelledby="pilares-titulo"
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
        }}
      >
        <Typography id="pilares-titulo" component="h2" variant="h4" sx={{ gridColumn: '1 / -1' }}>
          Princípios da experiência
        </Typography>

        {pilares.map((pilar) => (
          <Card key={pilar.titulo} variant="outlined">
            <CardContent>
              <Typography component="h3" variant="h6" gutterBottom>
                {pilar.titulo}
              </Typography>
              <Typography color="text.secondary">{pilar.descricao}</Typography>
            </CardContent>
          </Card>
        ))}
      </Box>
    </Stack>
  )
}
