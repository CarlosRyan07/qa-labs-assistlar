import { Alert, Box, Button, Card, CardContent, Stack, TextField, Typography } from '@mui/material'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'

import { AlertaErro } from '../../shared/components/AlertaErro'
import { ehUuid } from '../../shared/utils/uuid'
import { criarContratacao, listarContratacoes } from './api'

interface DadosCriacao {
  clienteId: string
  planoId: string
}

export function PaginaContratacoes() {
  const navegar = useNavigate()
  const [parametros] = useSearchParams()
  const [dados, setDados] = useState<DadosCriacao>({
    clienteId: parametros.get('clienteId') ?? '',
    planoId: parametros.get('planoId') ?? '',
  })
  const [idConsulta, setIdConsulta] = useState('')
  const [erroCriacao, setErroCriacao] = useState<string>()
  const [erroConsulta, setErroConsulta] = useState<string>()
  const [clienteBusca, setClienteBusca] = useState(parametros.get('clienteId') ?? '')
  const lista = useQuery({
    queryKey: ['contratacoes', clienteBusca],
    queryFn: () => listarContratacoes(clienteBusca),
    enabled: ehUuid(clienteBusca),
  })
  const criacao = useMutation({
    mutationFn: criarContratacao,
    retry: false,
    onSuccess: (contratacao) => navegar(`/contratacoes/${contratacao.id}`),
  })

  function criar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const dadosNormalizados = {
      clienteId: dados.clienteId.trim(),
      planoId: dados.planoId.trim(),
    }

    if (!ehUuid(dadosNormalizados.clienteId) || !ehUuid(dadosNormalizados.planoId)) {
      setErroCriacao('Informe UUIDs válidos para o cliente e o plano.')
      return
    }

    setErroCriacao(undefined)
    criacao.mutate(dadosNormalizados)
  }

  function consultar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const id = idConsulta.trim()

    if (!ehUuid(id)) {
      setErroConsulta('Informe um UUID válido para consultar a contratação.')
      return
    }

    setErroConsulta(undefined)
    navegar(`/contratacoes/${id}`)
  }

  return (
    <Stack spacing={4}>
      <Box>
        <Typography component="h1" variant="h3" gutterBottom>
          Contratações
        </Typography>
        <Typography color="text.secondary">
          Crie uma contratação pendente ou consulte uma contratação existente pelo UUID.
        </Typography>
      </Box>

      <Card component="section" aria-labelledby="lista-contratacoes-titulo" aria-busy={lista.isLoading} variant="outlined">
        <CardContent>
          <Stack spacing={2}>
            <Box>
              <Typography id="lista-contratacoes-titulo" component="h2" variant="h5" gutterBottom>
                Contratacoes do cliente
              </Typography>
              <Typography color="text.secondary">Informe um UUID para consultar as contratacoes relacionadas.</Typography>
            </Box>
            <TextField label="UUID do cliente para listar" value={clienteBusca} onChange={(evento) => setClienteBusca(evento.target.value)} />
            {!ehUuid(clienteBusca) && <Typography color="text.secondary">A consulta sera habilitada quando o UUID for valido.</Typography>}
            {lista.isLoading && <Typography aria-live="polite" color="text.secondary">Carregando contratacoes...</Typography>}
            {lista.isError && <AlertaErro erro={lista.error} titulo="Nao foi possivel carregar as contratacoes" />}
            {!lista.isLoading && !lista.isError && lista.data?.itens.length === 0 && <Typography aria-live="polite" color="text.secondary">Nenhuma contratacao encontrada.</Typography>}
            {lista.data?.itens.map((contratacao) => (
              <Button key={contratacao.id} onClick={() => navegar(`/contratacoes/${contratacao.id}`)} sx={{ justifyContent: 'flex-start' }} variant="outlined">
                {contratacao.status} — {contratacao.id}
              </Button>
            ))}
          </Stack>
        </CardContent>
      </Card>

      <Box
        sx={{
          display: 'grid',
          gap: 3,
          gridTemplateColumns: { xs: '1fr', md: 'minmax(0, 2fr) minmax(280px, 1fr)' },
        }}
      >
        <Card component="section" aria-labelledby="nova-contratacao-titulo" variant="outlined">
          <CardContent>
            <Stack component="form" spacing={2.5} onSubmit={criar}>
              <Box>
                <Typography id="nova-contratacao-titulo" component="h2" variant="h5" gutterBottom>
                  Nova contratação
                </Typography>
                <Typography color="text.secondary">
                  A API reavaliará a elegibilidade antes de criar a contratação como pendente.
                </Typography>
              </Box>

              {erroCriacao && (
                <Alert severity="warning" role="alert">
                  {erroCriacao}
                </Alert>
              )}
              {criacao.isError && (
                <AlertaErro erro={criacao.error} titulo="Não foi possível criar a contratação" />
              )}

              <TextField
                autoComplete="off"
                label="UUID do cliente"
                name="clienteId"
                onChange={(evento) => {
                  setDados((atual) => ({ ...atual, clienteId: evento.target.value }))
                  setErroCriacao(undefined)
                }}
                placeholder="00000000-0000-0000-0000-000000000000"
                required
                value={dados.clienteId}
              />
              <TextField
                autoComplete="off"
                label="UUID do plano"
                name="planoId"
                onChange={(evento) => {
                  setDados((atual) => ({ ...atual, planoId: evento.target.value }))
                  setErroCriacao(undefined)
                }}
                placeholder="00000000-0000-0000-0000-000000000000"
                required
                value={dados.planoId}
              />
              <Box>
                <Button disabled={criacao.isPending} type="submit" variant="contained">
                  {criacao.isPending ? 'Criando contratação...' : 'Criar contratação pendente'}
                </Button>
              </Box>
            </Stack>
          </CardContent>
        </Card>

        <Card component="section" aria-labelledby="consulta-contratacao-titulo" variant="outlined">
          <CardContent>
            <Stack component="form" spacing={2.5} onSubmit={consultar}>
              <Box>
                <Typography id="consulta-contratacao-titulo" component="h2" variant="h5" gutterBottom>
                  Consultar contratação
                </Typography>
                <Typography color="text.secondary">A consulta requer o UUID da contratação.</Typography>
              </Box>

              {erroConsulta && (
                <Alert severity="warning" role="alert">
                  {erroConsulta}
                </Alert>
              )}

              <TextField
                autoComplete="off"
                label="UUID da contratação"
                name="contratacaoId"
                onChange={(evento) => {
                  setIdConsulta(evento.target.value)
                  setErroConsulta(undefined)
                }}
                placeholder="00000000-0000-0000-0000-000000000000"
                required
                value={idConsulta}
              />
              <Box>
                <Button type="submit" variant="outlined">
                  Consultar contratação
                </Button>
              </Box>
            </Stack>
          </CardContent>
        </Card>
      </Box>
    </Stack>
  )
}
