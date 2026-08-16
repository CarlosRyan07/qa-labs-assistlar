import { Alert, Box, Card, CardContent, Chip, Stack, Typography } from '@mui/material'

const pilares = [
  {
    titulo: 'Jornada guiada',
    descricao: 'Os recursos serão conectados pelos UUIDs retornados pela API.',
  },
  {
    titulo: 'Contrato preservado',
    descricao: 'A Web consumirá somente as operações existentes no backend.',
  },
  {
    titulo: 'Qualidade desde a fundação',
    descricao: 'Acessibilidade, testes e feedback rápido fazem parte da arquitetura.',
  },
]

export function PaginaInicial() {
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
        A fundação da aplicação está pronta. As operações do domínio serão
        adicionadas progressivamente nos próximos marcos.
      </Alert>

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
