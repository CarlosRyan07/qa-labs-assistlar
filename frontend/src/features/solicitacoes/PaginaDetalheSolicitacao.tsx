import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  List,
  ListItem,
  ListItemText,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link as LinkRouter, useParams } from 'react-router-dom'

import { AlertaErro } from '../../shared/components/AlertaErro'
import { ehUuid } from '../../shared/utils/uuid'
import {
  cancelarSolicitacao,
  concluirSolicitacao,
  consultarHistoricoSolicitacao,
  consultarSolicitacao,
  iniciarSolicitacao,
} from './api/solicitacoes-api'
import type { HistoricoSolicitacao, Solicitacao, StatusSolicitacao } from './tipos'

type AcaoSolicitacao =
  | { tipo: 'iniciar' }
  | { tipo: 'concluir' }
  | { tipo: 'cancelar'; motivo?: string }

function chaveSolicitacao(id: string) {
  return ['solicitacoes', id] as const
}

function chaveHistorico(id: string) {
  return ['solicitacoes', id, 'historico'] as const
}

export function PaginaDetalheSolicitacao() {
  const { id = '' } = useParams()
  const consultas = useQueryClient()
  const idValido = ehUuid(id)
  const [motivoCancelamento, setMotivoCancelamento] = useState('')
  const [erroMotivo, setErroMotivo] = useState<string>()
  const [mensagemSucesso, setMensagemSucesso] = useState<string>()
  const solicitacao = useQuery({
    queryKey: chaveSolicitacao(id),
    queryFn: () => consultarSolicitacao(id),
    enabled: idValido,
    retry: false,
  })
  const historico = useQuery({
    queryKey: chaveHistorico(id),
    queryFn: () => consultarHistoricoSolicitacao(id),
    enabled: idValido,
    retry: false,
  })
  const transicao = useMutation({
    mutationFn: (acao: AcaoSolicitacao) => executarAcao(id, acao),
    retry: false,
    onMutate: () => {
      setMensagemSucesso(undefined)
    },
    onSuccess: (atualizada) => {
      consultas.setQueryData(chaveSolicitacao(id), atualizada)
      void consultas.invalidateQueries({ queryKey: chaveHistorico(id) })
      setMotivoCancelamento('')
      setErroMotivo(undefined)
      setMensagemSucesso(`Solicitação atualizada para ${rotuloStatus(atualizada.status)}.`)
    },
  })

  if (!idValido) {
    return <EstadoIdInvalido />
  }

  if (solicitacao.isPending) {
    return (
      <Stack role="status" spacing={2} sx={{ alignItems: 'center', py: 6 }}>
        <CircularProgress aria-label="Carregando solicitação" />
        <Typography>Carregando solicitação...</Typography>
      </Stack>
    )
  }

  if (solicitacao.isError || !solicitacao.data) {
    return <EstadoErroConsulta erro={solicitacao.error} onRetry={() => void solicitacao.refetch()} />
  }

  const dados = solicitacao.data
  const podeCancelar = dados.status === 'ABERTA' || dados.status === 'EM_ATENDIMENTO'
  const motivoObrigatorio = dados.status === 'EM_ATENDIMENTO'

  function cancelar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const motivo = motivoCancelamento.trim()

    if (motivoObrigatorio && !motivo) {
      setErroMotivo('Informe o motivo para cancelar uma solicitação em atendimento.')
      return
    }

    setErroMotivo(undefined)
    transicao.mutate({ tipo: 'cancelar', motivo: motivo || undefined })
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Button component={LinkRouter} to="/solicitacoes/nova" variant="text">
          Voltar para solicitações
        </Button>
        <Typography component="h1" variant="h3" gutterBottom>
          Detalhe da solicitação
        </Typography>
      </Box>

      {mensagemSucesso && (
        <Alert severity="success" role="status">
          {mensagemSucesso}
        </Alert>
      )}
      {transicao.isError && (
        <AlertaErro erro={transicao.error} titulo="Não foi possível atualizar a solicitação" />
      )}

      <Card component="article" variant="outlined">
        <CardContent>
          <Stack spacing={3}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={1}
              sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
            >
              <Box>
                <Typography component="h2" variant="h4">
                  {rotuloTipo(dados.tipoAssistencia)}
                </Typography>
                <Typography color="text.secondary" sx={{ overflowWrap: 'anywhere' }}>
                  {dados.id}
                </Typography>
              </Box>
              <Chip
                aria-label={`Status: ${rotuloStatus(dados.status)}`}
                color={corStatus(dados.status)}
                label={rotuloStatus(dados.status)}
              />
            </Stack>

            <Divider />
            <DadosSolicitacao solicitacao={dados} />

            {(dados.status === 'ABERTA' || dados.status === 'EM_ATENDIMENTO') && (
              <Box component="section" aria-labelledby="acoes-solicitacao-titulo">
                <Typography id="acoes-solicitacao-titulo" component="h3" variant="h6" gutterBottom>
                  Próxima ação
                </Typography>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                  {dados.status === 'ABERTA' && (
                    <Button
                      disabled={transicao.isPending}
                      onClick={() => transicao.mutate({ tipo: 'iniciar' })}
                      variant="contained"
                    >
                      {transicao.isPending ? 'Atualizando...' : 'Iniciar atendimento'}
                    </Button>
                  )}
                  {dados.status === 'EM_ATENDIMENTO' && (
                    <Button
                      disabled={transicao.isPending}
                      onClick={() => transicao.mutate({ tipo: 'concluir' })}
                      variant="contained"
                    >
                      {transicao.isPending ? 'Atualizando...' : 'Concluir atendimento'}
                    </Button>
                  )}
                </Stack>
              </Box>
            )}

            {podeCancelar && (
              <Stack
                component="form"
                spacing={2}
                onSubmit={cancelar}
                aria-labelledby="cancelamento-solicitacao-titulo"
              >
                <Box>
                  <Typography id="cancelamento-solicitacao-titulo" component="h3" variant="h6">
                    Cancelar solicitação
                  </Typography>
                  <Typography color="text.secondary">
                    {motivoObrigatorio
                      ? 'O motivo é obrigatório porque o atendimento já foi iniciado.'
                      : 'O motivo é opcional enquanto a solicitação está aberta.'}
                  </Typography>
                </Box>
                <TextField
                  error={Boolean(erroMotivo)}
                  helperText={erroMotivo ?? `${motivoCancelamento.length}/500 caracteres`}
                  label={motivoObrigatorio ? 'Motivo do cancelamento' : 'Motivo do cancelamento (opcional)'}
                  multiline
                  minRows={3}
                  onChange={(evento) => {
                    setMotivoCancelamento(evento.target.value)
                    setErroMotivo(undefined)
                  }}
                  required={motivoObrigatorio}
                  value={motivoCancelamento}
                  slotProps={{ htmlInput: { maxLength: 500 } }}
                />
                <Box>
                  <Button color="error" disabled={transicao.isPending} type="submit" variant="outlined">
                    {transicao.isPending ? 'Cancelando...' : 'Cancelar solicitação'}
                  </Button>
                </Box>
              </Stack>
            )}
          </Stack>
        </CardContent>
      </Card>

      <HistoricoSolicitacaoSecao
        dados={historico.data}
        carregando={historico.isPending}
        erro={historico.isError ? historico.error : undefined}
        onRetry={() => void historico.refetch()}
      />
    </Stack>
  )
}

async function executarAcao(id: string, acao: AcaoSolicitacao): Promise<Solicitacao> {
  if (acao.tipo === 'iniciar') {
    return iniciarSolicitacao(id)
  }

  if (acao.tipo === 'concluir') {
    return concluirSolicitacao(id)
  }

  return cancelarSolicitacao(id, acao.motivo)
}

function DadosSolicitacao({ solicitacao }: { solicitacao: Solicitacao }) {
  const dados = [
    ['UUID da contratação', solicitacao.contratacaoId],
    ['Descrição do problema', solicitacao.descricaoProblema],
    ['Aberta em', formatarDataHora(solicitacao.abertaEm)],
    ...(solicitacao.iniciadaEm ? [['Iniciada em', formatarDataHora(solicitacao.iniciadaEm)]] : []),
    ...(solicitacao.concluidaEm ? [['Concluída em', formatarDataHora(solicitacao.concluidaEm)]] : []),
    ...(solicitacao.canceladaEm ? [['Cancelada em', formatarDataHora(solicitacao.canceladaEm)]] : []),
    ...(solicitacao.motivoCancelamento ? [['Motivo do cancelamento', solicitacao.motivoCancelamento]] : []),
    ['Versão', String(solicitacao.versao)],
  ]

  return (
    <Box
      component="dl"
      sx={{
        display: 'grid',
        gap: 2,
        gridTemplateColumns: { xs: '1fr', sm: 'minmax(190px, 0.4fr) 1fr' },
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

function HistoricoSolicitacaoSecao({
  dados,
  carregando,
  erro,
  onRetry,
}: {
  dados: HistoricoSolicitacao[] | undefined
  carregando: boolean
  erro: unknown
  onRetry: () => void
}) {
  return (
    <Card component="section" aria-labelledby="historico-solicitacao-titulo" variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <Typography id="historico-solicitacao-titulo" component="h2" variant="h5">
            Histórico de status
          </Typography>
          {carregando && <Typography role="status">Carregando histórico...</Typography>}
          {erro !== undefined && (
            <>
              <AlertaErro erro={erro} titulo="Não foi possível carregar o histórico" />
              <Box>
                <Button onClick={onRetry} variant="outlined">
                  Tentar novamente
                </Button>
              </Box>
            </>
          )}
          {dados?.length === 0 && <Alert severity="info">Nenhuma mudança de status foi registrada.</Alert>}
          {dados && dados.length > 0 && (
            <List aria-label="Histórico da solicitação" disablePadding>
              {dados.map((item) => (
                <ListItem key={item.id} divider disableGutters>
                  <ListItemText
                    primary={`${item.statusAnterior ? rotuloStatus(item.statusAnterior as StatusSolicitacao) : 'Criação'} → ${rotuloStatus(item.statusNovo as StatusSolicitacao)}`}
                    secondary={descricaoHistorico(item)}
                  />
                </ListItem>
              ))}
            </List>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}

function descricaoHistorico(item: HistoricoSolicitacao): string {
  const responsavel = item.tipoResponsavel.toLocaleLowerCase('pt-BR')
  const motivo = item.motivo ? ` Motivo: ${item.motivo}` : ''

  return `${formatarDataHora(item.registradoEm)} — responsável: ${responsavel}.${motivo}`
}

function EstadoIdInvalido() {
  return (
    <Stack spacing={3}>
      <Typography component="h1" variant="h3">
        Detalhe da solicitação
      </Typography>
      <Alert severity="warning">O UUID informado para a solicitação não é válido.</Alert>
      <Box>
        <Button component={LinkRouter} to="/solicitacoes/nova" variant="outlined">
          Consultar outra solicitação
        </Button>
      </Box>
    </Stack>
  )
}

function EstadoErroConsulta({ erro, onRetry }: { erro: unknown; onRetry: () => void }) {
  return (
    <Stack spacing={3}>
      <Typography component="h1" variant="h3">
        Detalhe da solicitação
      </Typography>
      <AlertaErro erro={erro} titulo="Não foi possível carregar a solicitação" />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <Button onClick={onRetry} variant="contained">
          Tentar novamente
        </Button>
        <Button component={LinkRouter} to="/solicitacoes/nova" variant="outlined">
          Voltar para solicitações
        </Button>
      </Stack>
    </Stack>
  )
}

function rotuloTipo(tipo: Solicitacao['tipoAssistencia']): string {
  const rotulos = {
    ELETRICISTA: 'Eletricista',
    ENCANADOR: 'Encanador',
    CHAVEIRO: 'Chaveiro',
  }

  return rotulos[tipo]
}

function rotuloStatus(status: StatusSolicitacao): string {
  const rotulos: Record<StatusSolicitacao, string> = {
    ABERTA: 'Aberta',
    EM_ATENDIMENTO: 'Em atendimento',
    CONCLUIDA: 'Concluída',
    CANCELADA: 'Cancelada',
  }

  return rotulos[status] ?? status
}

function corStatus(status: StatusSolicitacao): 'default' | 'info' | 'success' | 'warning' {
  if (status === 'CONCLUIDA') return 'success'
  if (status === 'CANCELADA') return 'default'
  if (status === 'EM_ATENDIMENTO') return 'warning'

  return 'info'
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
