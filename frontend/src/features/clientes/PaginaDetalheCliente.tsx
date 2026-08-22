import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  Stack,
  Typography,
} from '@mui/material'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link as LinkRouter, useParams } from 'react-router-dom'

import { AlertaErro } from '../../shared/components/AlertaErro'
import { ehUuid } from '../../shared/utils/uuid'
import { consultarCliente, inativarCliente, reativarCliente } from './api/clientes-api'
import type { Cliente } from './tipos'

function chaveCliente(id: string) {
  return ['clientes', id] as const
}

export function PaginaDetalheCliente() {
  const { id = '' } = useParams()
  const clienteConsultas = useQueryClient()
  const idValido = ehUuid(id)
  const consulta = useQuery({
    queryKey: chaveCliente(id),
    queryFn: () => consultarCliente(id),
    enabled: idValido,
    retry: false,
  })
  const atualizarCliente = useMutation({
    mutationFn: async (acao: 'inativar' | 'reativar') =>
      acao === 'inativar' ? inativarCliente(id) : reativarCliente(id),
    retry: false,
    onSuccess: (cliente) => {
      clienteConsultas.setQueryData(chaveCliente(id), cliente)
    },
  })

  if (!idValido) {
    return <EstadoClienteInvalido />
  }

  if (consulta.isPending) {
    return (
      <Stack role="status" spacing={2} sx={{ alignItems: 'center', py: 6 }}>
        <CircularProgress aria-label="Carregando cliente" />
        <Typography>Carregando cliente...</Typography>
      </Stack>
    )
  }

  if (consulta.isError || !consulta.data) {
    return <EstadoErroConsulta erro={consulta.error} onRetry={() => void consulta.refetch()} />
  }

  const cliente = consulta.data
  const inativo = cliente.status === 'INATIVO'

  return (
    <Stack spacing={3}>
      <Box>
        <Button component={LinkRouter} to="/clientes" variant="text">
          Voltar para clientes
        </Button>
        <Typography component="h1" variant="h3" gutterBottom>
          Detalhe do cliente
        </Typography>
      </Box>

      {atualizarCliente.isError && (
        <AlertaErro erro={atualizarCliente.error} titulo="Não foi possível alterar o status do cliente" />
      )}

      <Card component="article" variant="outlined">
        <CardContent>
          <Stack spacing={3}>
            <Box>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={1}
                sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
              >
                <Typography component="h2" variant="h4">
                  {cliente.nome}
                </Typography>
                <Chip
                  aria-label={`Status: ${rotuloStatus(cliente.status)}`}
                  color={inativo ? 'default' : 'success'}
                  label={rotuloStatus(cliente.status)}
                />
              </Stack>
              <Typography color="text.secondary">{cliente.email}</Typography>
            </Box>

            <Divider />
            <DadosCliente cliente={cliente} />

            <Box>
              <Typography component="h3" variant="h6" gutterBottom>
                Status do cadastro
              </Typography>
              <Button
                color={inativo ? 'primary' : 'warning'}
                disabled={atualizarCliente.isPending}
                onClick={() => atualizarCliente.mutate(inativo ? 'reativar' : 'inativar')}
                variant="contained"
              >
                {atualizarCliente.isPending
                  ? 'Atualizando status...'
                  : inativo
                    ? 'Reativar cliente'
                    : 'Inativar cliente'}
              </Button>
            </Box>

            {cliente.status === 'ATIVO' && (
              <Button
                component={LinkRouter}
                to={`/elegibilidade?clienteId=${cliente.id}`}
                variant="outlined"
                sx={{ alignSelf: 'flex-start' }}
              >
                Avaliar elegibilidade
              </Button>
            )}
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  )
}

function DadosCliente({ cliente }: { cliente: Cliente }) {
  return (
    <Box
      component="dl"
      sx={{
        display: 'grid',
        gap: 2,
        gridTemplateColumns: { xs: '1fr', sm: 'minmax(160px, 0.4fr) 1fr' },
        m: 0,
      }}
    >
      <Typography component="dt" sx={{ fontWeight: 700 }}>
        UUID
      </Typography>
      <Typography component="dd" sx={{ m: 0, overflowWrap: 'anywhere' }}>
        {cliente.id}
      </Typography>
      <Typography component="dt" sx={{ fontWeight: 700 }}>
        Data de nascimento
      </Typography>
      <Typography component="dd" sx={{ m: 0 }}>
        {formatarData(cliente.dataNascimento)}
      </Typography>
      <Typography component="dt" sx={{ fontWeight: 700 }}>
        Cadastrado em
      </Typography>
      <Typography component="dd" sx={{ m: 0 }}>
        {formatarDataHora(cliente.criadoEm)}
      </Typography>
      <Typography component="dt" sx={{ fontWeight: 700 }}>
        Atualizado em
      </Typography>
      <Typography component="dd" sx={{ m: 0 }}>
        {formatarDataHora(cliente.atualizadoEm)}
      </Typography>
    </Box>
  )
}

function EstadoClienteInvalido() {
  return (
    <Stack spacing={3}>
      <Typography component="h1" variant="h3">
        Detalhe do cliente
      </Typography>
      <Alert severity="warning">O UUID informado para o cliente não é válido.</Alert>
      <Box>
        <Button component={LinkRouter} to="/clientes" variant="outlined">
          Consultar outro cliente
        </Button>
      </Box>
    </Stack>
  )
}

function EstadoErroConsulta({ erro, onRetry }: { erro: unknown; onRetry: () => void }) {
  return (
    <Stack spacing={3}>
      <Typography component="h1" variant="h3">
        Detalhe do cliente
      </Typography>
      <AlertaErro erro={erro} titulo="Não foi possível carregar o cliente solicitado" />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <Button onClick={onRetry} variant="contained">
          Tentar novamente
        </Button>
        <Button component={LinkRouter} to="/clientes" variant="outlined">
          Voltar para clientes
        </Button>
      </Stack>
    </Stack>
  )
}

function rotuloStatus(status: Cliente['status']): string {
  return status === 'ATIVO' ? 'Ativo' : 'Inativo'
}

function formatarData(data: string): string {
  const [ano, mes, dia] = data.split('-')

  return ano && mes && dia ? `${dia}/${mes}/${ano}` : data
}

function formatarDataHora(dataHora: string): string {
  const data = new Date(dataHora)

  if (Number.isNaN(data.getTime())) {
    return dataHora
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(data)
}
