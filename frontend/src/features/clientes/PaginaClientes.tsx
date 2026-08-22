import { Alert, Box, Button, Card, CardContent, Stack, TextField, Typography } from '@mui/material'
import { useMutation } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'

import { AlertaErro } from '../../shared/components/AlertaErro'
import { ehUuid } from '../../shared/utils/uuid'
import { cadastrarCliente } from './api/clientes-api'

interface CadastroForm {
  nome: string
  email: string
  dataNascimento: string
}

const cadastroInicial: CadastroForm = {
  nome: '',
  email: '',
  dataNascimento: '',
}

export function PaginaClientes() {
  const navegar = useNavigate()
  const [cadastro, setCadastro] = useState<CadastroForm>(cadastroInicial)
  const [idConsulta, setIdConsulta] = useState('')
  const [erroConsulta, setErroConsulta] = useState<string>()
  const cadastroCliente = useMutation({
    mutationFn: cadastrarCliente,
    retry: false,
    onSuccess: (cliente) => {
      navegar(`/clientes/${cliente.id}`)
    },
  })

  function enviarCadastro(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    cadastroCliente.mutate(cadastro)
  }

  function consultarPorId(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const id = idConsulta.trim()

    if (!ehUuid(id)) {
      setErroConsulta('Informe um UUID válido para consultar o cliente.')
      return
    }

    setErroConsulta(undefined)
    navegar(`/clientes/${id}`)
  }

  return (
    <Stack spacing={4}>
      <Box>
        <Typography component="h1" variant="h3" gutterBottom>
          Clientes
        </Typography>
        <Typography color="text.secondary">
          Cadastre um cliente ou consulte os dados a partir de um UUID conhecido.
        </Typography>
      </Box>

      <Box
        sx={{
          display: 'grid',
          gap: 3,
          gridTemplateColumns: { xs: '1fr', md: 'minmax(0, 2fr) minmax(280px, 1fr)' },
        }}
      >
        <Card component="section" aria-labelledby="cadastro-cliente-titulo" variant="outlined">
          <CardContent>
            <Stack component="form" spacing={2.5} onSubmit={enviarCadastro}>
              <Box>
                <Typography id="cadastro-cliente-titulo" component="h2" variant="h5" gutterBottom>
                  Cadastrar cliente
                </Typography>
                <Typography color="text.secondary">
                  Os dados serão validados novamente pela API antes do cadastro.
                </Typography>
              </Box>

              {cadastroCliente.isError && (
                <AlertaErro erro={cadastroCliente.error} titulo="Não foi possível cadastrar o cliente" />
              )}

              <TextField
                autoComplete="name"
                label="Nome"
                name="nome"
                onChange={(evento) => setCadastro((atual) => ({ ...atual, nome: evento.target.value }))}
                required
                value={cadastro.nome}
                slotProps={{ htmlInput: { minLength: 3, maxLength: 120 } }}
              />
              <TextField
                autoComplete="email"
                label="E-mail"
                name="email"
                onChange={(evento) => setCadastro((atual) => ({ ...atual, email: evento.target.value }))}
                required
                type="email"
                value={cadastro.email}
                slotProps={{ htmlInput: { maxLength: 254 } }}
              />
              <TextField
                label="Data de nascimento"
                name="dataNascimento"
                onChange={(evento) =>
                  setCadastro((atual) => ({ ...atual, dataNascimento: evento.target.value }))
                }
                required
                slotProps={{ inputLabel: { shrink: true } }}
                type="date"
                value={cadastro.dataNascimento}
              />
              <Box>
                <Button disabled={cadastroCliente.isPending} type="submit" variant="contained">
                  {cadastroCliente.isPending ? 'Cadastrando...' : 'Cadastrar cliente'}
                </Button>
              </Box>
            </Stack>
          </CardContent>
        </Card>

        <Card component="section" aria-labelledby="consulta-cliente-titulo" variant="outlined">
          <CardContent>
            <Stack component="form" spacing={2.5} onSubmit={consultarPorId}>
              <Box>
                <Typography id="consulta-cliente-titulo" component="h2" variant="h5" gutterBottom>
                  Consultar cliente
                </Typography>
                <Typography color="text.secondary">A consulta requer o UUID do cliente.</Typography>
              </Box>

              {erroConsulta && (
                <Alert severity="warning" role="alert">
                  {erroConsulta}
                </Alert>
              )}

              <TextField
                autoComplete="off"
                label="UUID do cliente"
                name="idCliente"
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
                  Consultar cliente
                </Button>
              </Box>
            </Stack>
          </CardContent>
        </Card>
      </Box>
    </Stack>
  )
}
