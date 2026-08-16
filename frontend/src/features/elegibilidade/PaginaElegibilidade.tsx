import {
  Alert,
  AlertTitle,
  Box,
  Button,
  FormControl,
  InputLabel,
  List,
  ListItem,
  NativeSelect,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'

import { AlertaErro } from '../../shared/components/AlertaErro'
import { listarPlanos } from '../planos'
import { consultarElegibilidade } from './api'
import type { MotivoInelegibilidade } from './types'

const descricoesMotivos: Record<MotivoInelegibilidade, string> = {
  CLIENTE_INATIVO: 'O cliente está inativo.',
  CLIENTE_MENOR_DE_IDADE: 'O cliente não possui a idade mínima exigida.',
  PLANO_INATIVO: 'O plano está inativo.',
  CLIENTE_POSSUI_CONTRATACAO_VIGENTE: 'O cliente já possui uma contratação pendente ou ativa.',
}

export function PaginaElegibilidade() {
  const [parametros] = useSearchParams()
  const [clienteId, setClienteId] = useState(parametros.get('clienteId') ?? '')
  const [planoId, setPlanoId] = useState(parametros.get('planoId') ?? '')
  const planos = useQuery({ queryKey: ['planos'], queryFn: listarPlanos, staleTime: 5 * 60 * 1000 })
  const resultado = useMutation({
    mutationFn: ({ clienteId: cliente, planoId: plano }: { clienteId: string; planoId: string }) =>
      consultarElegibilidade(cliente, plano),
  })

  function avaliar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    resultado.reset()
    resultado.mutate({ clienteId: clienteId.trim(), planoId })
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Typography component="h1" variant="h3" gutterBottom>
          Avaliar elegibilidade
        </Typography>
        <Typography color="text.secondary">
          Informe um cliente conhecido e selecione um plano ativo. A avaliação não é persistida.
        </Typography>
      </Box>

      <Box component="form" onSubmit={avaliar} sx={{ display: 'grid', gap: 2, maxWidth: 680 }}>
        <TextField
          label="UUID do cliente"
          value={clienteId}
          onChange={(evento) => {
            setClienteId(evento.target.value)
            resultado.reset()
          }}
          required
          slotProps={{ htmlInput: { minLength: 36, maxLength: 36 } }}
          helperText="Use o identificador retornado no cadastro ou na consulta do cliente."
        />
        <FormControl required>
          <InputLabel htmlFor="plano">Plano</InputLabel>
          <NativeSelect
            inputProps={{ id: 'plano' }}
            value={planoId}
            onChange={(evento) => {
              setPlanoId(evento.target.value)
              resultado.reset()
            }}
            disabled={planos.isPending || planos.isError}
          >
            <option aria-label="Selecione um plano" value="" />
            {planos.data?.map((plano) => (
              <option key={plano.id} value={plano.id}>
                {plano.nome} ({plano.codigo})
              </option>
            ))}
          </NativeSelect>
        </FormControl>

        {planos.isPending && <Typography role="status">Carregando planos...</Typography>}
        {planos.isError && <AlertaErro erro={planos.error} titulo="Não foi possível carregar os planos" />}

        <Button type="submit" variant="contained" disabled={resultado.isPending || !clienteId || !planoId}>
          {resultado.isPending ? 'Avaliando...' : 'Avaliar elegibilidade'}
        </Button>
      </Box>

      {resultado.isError && <AlertaErro erro={resultado.error} />}

      {resultado.data && (
        <Alert severity={resultado.data.elegivel ? 'success' : 'warning'} role="status">
          <AlertTitle>
            {resultado.data.elegivel ? 'Cliente elegível' : 'Cliente não elegível'}
          </AlertTitle>
          {resultado.data.elegivel ? (
            <>
              A contratação pode ser criada e nascerá com status PENDENTE.
              <Button
                component={Link}
                to={`/contratacoes/nova?clienteId=${resultado.data.clienteId}&planoId=${resultado.data.planoId}`}
                color="inherit"
                sx={{ ml: { sm: 2 } }}
              >
                Criar contratação
              </Button>
            </>
          ) : (
            <List dense aria-label="Motivos de inelegibilidade">
              {resultado.data.motivos.map((motivo) => (
                <ListItem key={motivo} disableGutters>
                  {descricoesMotivos[motivo] ?? motivo}
                </ListItem>
              ))}
            </List>
          )}
        </Alert>
      )}
    </Stack>
  )
}
