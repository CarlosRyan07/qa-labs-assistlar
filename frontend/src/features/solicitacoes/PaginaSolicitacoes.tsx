import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  NativeSelect,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useMutation } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'

import { AlertaErro } from '../../shared/components/AlertaErro'
import { ehUuid } from '../../shared/utils/uuid'
import { abrirSolicitacao } from './api/solicitacoes-api'
import type { TipoAssistencia } from './tipos'

interface FormularioSolicitacao {
  contratacaoId: string
  tipoAssistencia: TipoAssistencia | ''
  descricaoProblema: string
}

export function PaginaSolicitacoes() {
  const [parametros] = useSearchParams()
  const navegar = useNavigate()
  const [formulario, setFormulario] = useState<FormularioSolicitacao>({
    contratacaoId: parametros.get('contratacaoId') ?? '',
    tipoAssistencia: '',
    descricaoProblema: '',
  })
  const [idConsulta, setIdConsulta] = useState('')
  const [erroConsulta, setErroConsulta] = useState<string>()
  const abertura = useMutation({
    mutationFn: abrirSolicitacao,
    retry: false,
    onSuccess: (solicitacao) => {
      navegar(`/solicitacoes/${solicitacao.id}`)
    },
  })

  function abrir(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()

    if (!formulario.tipoAssistencia) {
      return
    }

    abertura.mutate({
      contratacaoId: formulario.contratacaoId.trim(),
      tipoAssistencia: formulario.tipoAssistencia,
      descricaoProblema: formulario.descricaoProblema.trim(),
    })
  }

  function consultar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const id = idConsulta.trim()

    if (!ehUuid(id)) {
      setErroConsulta('Informe um UUID válido para consultar a solicitação.')
      return
    }

    setErroConsulta(undefined)
    navegar(`/solicitacoes/${id}`)
  }

  return (
    <Stack spacing={4}>
      <Box>
        <Typography component="h1" variant="h3" gutterBottom>
          Solicitações de assistência
        </Typography>
        <Typography color="text.secondary">
          Abra uma solicitação para uma contratação ativa ou consulte uma solicitação pelo UUID.
        </Typography>
      </Box>

      <Box
        sx={{
          display: 'grid',
          gap: 3,
          gridTemplateColumns: { xs: '1fr', md: 'minmax(0, 2fr) minmax(280px, 1fr)' },
        }}
      >
        <Card component="section" aria-labelledby="abertura-solicitacao-titulo" variant="outlined">
          <CardContent>
            <Stack component="form" spacing={2.5} onSubmit={abrir}>
              <Box>
                <Typography id="abertura-solicitacao-titulo" component="h2" variant="h5" gutterBottom>
                  Abrir solicitação
                </Typography>
                <Typography color="text.secondary">
                  Cobertura e limite disponível serão confirmados pela API.
                </Typography>
              </Box>

              {abertura.isError && (
                <AlertaErro erro={abertura.error} titulo="Não foi possível abrir a solicitação" />
              )}

              <TextField
                autoComplete="off"
                label="UUID da contratação"
                name="contratacaoId"
                onChange={(evento) =>
                  setFormulario((atual) => ({ ...atual, contratacaoId: evento.target.value }))
                }
                placeholder="00000000-0000-0000-0000-000000000000"
                required
                value={formulario.contratacaoId}
                slotProps={{ htmlInput: { minLength: 36, maxLength: 36 } }}
              />

              <FormControl required>
                <InputLabel htmlFor="tipo-assistencia">Tipo de assistência</InputLabel>
                <NativeSelect
                  inputProps={{ id: 'tipo-assistencia', name: 'tipoAssistencia' }}
                  onChange={(evento) =>
                    setFormulario((atual) => ({
                      ...atual,
                      tipoAssistencia: evento.target.value as TipoAssistencia | '',
                    }))
                  }
                  value={formulario.tipoAssistencia}
                >
                  <option aria-label="Selecione um tipo de assistência" value="" />
                  <option value="ELETRICISTA">Eletricista</option>
                  <option value="ENCANADOR">Encanador</option>
                  <option value="CHAVEIRO">Chaveiro</option>
                </NativeSelect>
              </FormControl>

              <TextField
                label="Descrição do problema"
                name="descricaoProblema"
                multiline
                minRows={4}
                onChange={(evento) =>
                  setFormulario((atual) => ({ ...atual, descricaoProblema: evento.target.value }))
                }
                required
                value={formulario.descricaoProblema}
                slotProps={{ htmlInput: { maxLength: 500 } }}
                helperText={`${formulario.descricaoProblema.length}/500 caracteres`}
              />

              <Box>
                <Button disabled={abertura.isPending} type="submit" variant="contained">
                  {abertura.isPending ? 'Abrindo solicitação...' : 'Abrir solicitação'}
                </Button>
              </Box>
            </Stack>
          </CardContent>
        </Card>

        <Card component="section" aria-labelledby="consulta-solicitacao-titulo" variant="outlined">
          <CardContent>
            <Stack component="form" spacing={2.5} onSubmit={consultar}>
              <Box>
                <Typography id="consulta-solicitacao-titulo" component="h2" variant="h5" gutterBottom>
                  Consultar solicitação
                </Typography>
                <Typography color="text.secondary">A consulta requer um UUID já conhecido.</Typography>
              </Box>

              {erroConsulta && (
                <Alert severity="warning" role="alert">
                  {erroConsulta}
                </Alert>
              )}

              <TextField
                autoComplete="off"
                label="UUID da solicitação"
                name="solicitacaoId"
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
                  Consultar solicitação
                </Button>
              </Box>
            </Stack>
          </CardContent>
        </Card>
      </Box>
    </Stack>
  )
}
