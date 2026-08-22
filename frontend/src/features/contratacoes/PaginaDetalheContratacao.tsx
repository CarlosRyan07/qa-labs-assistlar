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
  TextField,
  Typography,
} from '@mui/material'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link as LinkRouter, useParams } from 'react-router-dom'

import { AlertaErro } from '../../shared/components/AlertaErro'
import { ehUuid } from '../../shared/utils/uuid'
import {
  ativarContratacao,
  cancelarContratacao,
  consultarContratacao,
  consultarHistoricoContratacao,
} from './api'
import type { Contratacao, HistoricoContratacao, StatusContratacao } from './tipos'

function chaveContratacao(id: string) {
  return ['contratacoes', id] as const
}

function chaveHistorico(id: string) {
  return ['contratacoes', id, 'historico'] as const
}

type AcaoContratacao = { tipo: 'ativar' } | { tipo: 'cancelar'; motivo?: string }

export function PaginaDetalheContratacao() {
  const { id = '' } = useParams()
  const clienteConsultas = useQueryClient()
  const [motivo, setMotivo] = useState('')
  const alertaAcaoRef = useRef<HTMLDivElement>(null)
  const idValido = ehUuid(id)
  const consulta = useQuery({
    queryKey: chaveContratacao(id),
    queryFn: () => consultarContratacao(id),
    enabled: idValido,
    retry: false,
  })
  const historico = useQuery({
    queryKey: chaveHistorico(id),
    queryFn: () => consultarHistoricoContratacao(id),
    enabled: idValido,
    retry: false,
  })
  const alterarStatus = useMutation({
    mutationFn: (acao: AcaoContratacao) =>
      acao.tipo === 'ativar'
        ? ativarContratacao(id)
        : cancelarContratacao(id, acao.motivo),
    retry: false,
    onSuccess: async (contratacao, acao) => {
      clienteConsultas.setQueryData(chaveContratacao(id), contratacao)
      if (acao.tipo === 'cancelar') {
        setMotivo('')
      }
      await clienteConsultas.invalidateQueries({ queryKey: chaveHistorico(id) })
    },
  })

  useEffect(() => {
    if (alterarStatus.isError) {
      alertaAcaoRef.current?.focus()
    }
  }, [alterarStatus.isError])

  if (!idValido) {
    return <EstadoIdInvalido />
  }

  if (consulta.isPending) {
    return (
      <Stack role="status" spacing={2} sx={{ alignItems: 'center', py: 6 }}>
        <CircularProgress aria-label="Carregando contratação" />
        <Typography>Carregando contratação...</Typography>
      </Stack>
    )
  }

  if (consulta.isError || !consulta.data) {
    return <EstadoErroConsulta erro={consulta.error} onRetry={() => void consulta.refetch()} />
  }

  const contratacao = consulta.data
  const podeAtivar = contratacao.status === 'PENDENTE'
  const podeCancelar = contratacao.status !== 'CANCELADA'

  function enviarCancelamento(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    alterarStatus.mutate({ tipo: 'cancelar', motivo })
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Button component={LinkRouter} to="/contratacoes/nova" variant="text">
          Voltar para contratações
        </Button>
        <Typography component="h1" variant="h3" gutterBottom>
          Detalhe da contratação
        </Typography>
      </Box>

      {alterarStatus.isError && (
        <AlertaErro
          ref={alertaAcaoRef}
          erro={alterarStatus.error}
          titulo="Não foi possível alterar a contratação"
        />
      )}

      <Card component="article" variant="outlined">
        <CardContent>
          <Stack spacing={3}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={1}
              sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
            >
              <Typography component="h2" variant="h4">
                Contratação
              </Typography>
              <Chip
                aria-label={`Status: ${rotuloStatus(contratacao.status)}`}
                color={corStatus(contratacao.status)}
                label={rotuloStatus(contratacao.status)}
              />
            </Stack>

            <Divider />
            <DadosContratacao contratacao={contratacao} />

            {(podeAtivar || podeCancelar) && (
              <Box component="section" aria-labelledby="acoes-contratacao-titulo">
                <Typography id="acoes-contratacao-titulo" component="h3" variant="h6" gutterBottom>
                  Ações disponíveis
                </Typography>
                <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
                  {podeAtivar && (
                    <Button
                      disabled={alterarStatus.isPending}
                      onClick={() => alterarStatus.mutate({ tipo: 'ativar' })}
                      variant="contained"
                    >
                      {alterarStatus.isPending ? 'Atualizando...' : 'Ativar contratação'}
                    </Button>
                  )}

                  {podeCancelar && (
                    <Stack component="form" spacing={1.5} onSubmit={enviarCancelamento} sx={{ width: '100%' }}>
                      <TextField
                        fullWidth
                        helperText="O motivo é opcional para o cancelamento da contratação."
                        label="Motivo do cancelamento"
                        multiline
                        name="motivo"
                        onChange={(evento) => setMotivo(evento.target.value)}
                        rows={3}
                        slotProps={{ htmlInput: { maxLength: 500 } }}
                        value={motivo}
                      />
                      <Box>
                        <Button
                          color="warning"
                          disabled={alterarStatus.isPending}
                          type="submit"
                          variant="outlined"
                        >
                          {alterarStatus.isPending ? 'Atualizando...' : 'Cancelar contratação'}
                        </Button>
                      </Box>
                    </Stack>
                  )}
                </Stack>
              </Box>
            )}

            {contratacao.status === 'ATIVA' && (
              <Button
                component={LinkRouter}
                to={`/solicitacoes/nova?contratacaoId=${contratacao.id}`}
                variant="contained"
                sx={{ alignSelf: 'flex-start' }}
              >
                Abrir solicitação de assistência
              </Button>
            )}
          </Stack>
        </CardContent>
      </Card>

      <HistoricoContratacaoSection
        carregando={historico.isPending}
        erro={historico.isError ? historico.error : undefined}
        historico={historico.data}
        onRetry={() => void historico.refetch()}
      />
    </Stack>
  )
}

function DadosContratacao({ contratacao }: { contratacao: Contratacao }) {
  const dados = [
    ['UUID', contratacao.id],
    ['UUID do cliente', contratacao.clienteId],
    ['UUID do plano', contratacao.planoId],
    ['Criada em', formatarDataHora(contratacao.criadaEm)],
    ...(contratacao.ativadaEm ? [['Ativada em', formatarDataHora(contratacao.ativadaEm)]] : []),
    ...(contratacao.canceladaEm ? [['Cancelada em', formatarDataHora(contratacao.canceladaEm)]] : []),
    ['Versão', String(contratacao.versao)],
  ]

  return (
    <Box
      component="dl"
      sx={{
        display: 'grid',
        gap: 2,
        gridTemplateColumns: { xs: '1fr', sm: 'minmax(170px, 0.4fr) 1fr' },
        m: 0,
      }}
    >
      {dados.map(([rotulo, valor]) => (
        <Box key={rotulo} sx={{ display: 'contents' }}>
          <Typography component="dt" sx={{ fontWeight: 700 }}>
            {rotulo}
          </Typography>
          <Typography component="dd" sx={{ m: 0, overflowWrap: 'anywhere' }}>
            {valor}
          </Typography>
        </Box>
      ))}
    </Box>
  )
}

interface HistoricoContratacaoSectionProps {
  carregando: boolean
  erro?: unknown
  historico?: HistoricoContratacao[]
  onRetry: () => void
}

function HistoricoContratacaoSection({
  carregando,
  erro,
  historico,
  onRetry,
}: HistoricoContratacaoSectionProps) {
  const possuiErro = erro !== undefined

  return (
    <Card component="section" aria-labelledby="historico-contratacao-titulo" variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <Typography id="historico-contratacao-titulo" component="h2" variant="h5">
            Histórico de status
          </Typography>

          {carregando && <Typography role="status">Carregando histórico...</Typography>}
          {possuiErro && (
            <>
              <AlertaErro erro={erro} titulo="Não foi possível carregar o histórico" />
              <Box>
                <Button onClick={onRetry} variant="outlined">
                  Tentar carregar o histórico novamente
                </Button>
              </Box>
            </>
          )}
          {!carregando && !possuiErro && historico?.length === 0 && (
            <Alert severity="info">Nenhuma mudança de status foi registrada.</Alert>
          )}
          {!carregando && !possuiErro && historico && historico.length > 0 && (
            <Stack component="ol" spacing={2} sx={{ listStylePosition: 'inside', m: 0, p: 0 }}>
              {historico.map((item) => (
                <Box component="li" key={item.id}>
                  <Typography component="span" sx={{ fontWeight: 700 }}>
                    {item.statusAnterior
                      ? `${rotuloStatusTexto(item.statusAnterior)} → ${rotuloStatusTexto(item.statusNovo)}`
                      : `Estado inicial: ${rotuloStatusTexto(item.statusNovo)}`}
                  </Typography>
                  <Typography color="text.secondary">
                    {formatarDataHora(item.registradoEm)} · Responsável: {rotuloResponsavel(item.tipoResponsavel)}
                  </Typography>
                  {item.motivo && <Typography>Motivo: {item.motivo}</Typography>}
                </Box>
              ))}
            </Stack>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}

function EstadoIdInvalido() {
  return (
    <Stack spacing={3}>
      <Typography component="h1" variant="h3">
        Detalhe da contratação
      </Typography>
      <Alert severity="warning">O UUID informado para a contratação não é válido.</Alert>
      <Box>
        <Button component={LinkRouter} to="/contratacoes/nova" variant="outlined">
          Consultar outra contratação
        </Button>
      </Box>
    </Stack>
  )
}

function EstadoErroConsulta({ erro, onRetry }: { erro: unknown; onRetry: () => void }) {
  return (
    <Stack spacing={3}>
      <Typography component="h1" variant="h3">
        Detalhe da contratação
      </Typography>
      <AlertaErro erro={erro} titulo="Não foi possível carregar a contratação solicitada" />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <Button onClick={onRetry} variant="contained">
          Tentar novamente
        </Button>
        <Button component={LinkRouter} to="/contratacoes/nova" variant="outlined">
          Voltar para contratações
        </Button>
      </Stack>
    </Stack>
  )
}

function rotuloStatus(status: StatusContratacao): string {
  return rotuloStatusTexto(status)
}

function rotuloStatusTexto(status: string): string {
  const rotulos: Record<string, string> = {
    PENDENTE: 'Pendente',
    ATIVA: 'Ativa',
    CANCELADA: 'Cancelada',
  }

  return rotulos[status] ?? status
}

function corStatus(status: StatusContratacao): 'default' | 'success' | 'warning' {
  if (status === 'ATIVA') return 'success'
  if (status === 'PENDENTE') return 'warning'
  return 'default'
}

function rotuloResponsavel(responsavel: HistoricoContratacao['tipoResponsavel']): string {
  const rotulos = { CLIENTE: 'Cliente', OPERADOR: 'Operador', SISTEMA: 'Sistema' }
  return rotulos[responsavel]
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
