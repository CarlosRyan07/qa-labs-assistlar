import { Alert, AlertTitle, List, ListItem } from '@mui/material'
import { forwardRef } from 'react'

import { ehErroHttp } from '../api/problema-api'

interface AlertaErroProps {
  erro: unknown
  titulo?: string
}

export const AlertaErro = forwardRef<HTMLDivElement, AlertaErroProps>(function AlertaErro(
  { erro, titulo = 'Não foi possível concluir a operação' },
  ref,
) {
  const problema = ehErroHttp(erro) ? erro.problema : undefined
  const detalhe = problema?.detail ?? mensagemPorErro(erro)

  return (
    <Alert ref={ref} severity="error" role="alert" tabIndex={-1}>
      <AlertTitle>{problema?.title ?? titulo}</AlertTitle>
      {detalhe}
      {problema?.erros && problema.erros.length > 0 && (
        <List dense aria-label="Campos com erro">
          {problema.erros.map((item) => (
            <ListItem key={`${item.campo}-${item.mensagem}`} disableGutters>
              {item.campo}: {item.mensagem}
            </ListItem>
          ))}
        </List>
      )}
    </Alert>
  )
})

function mensagemPorErro(erro: unknown) {
  if (ehErroHttp(erro)) {
    if (erro.tipo === 'rede') {
      return 'Não foi possível comunicar com o servidor. Verifique se o backend está disponível.'
    }

    if (erro.status === 409) {
      return 'Os dados foram alterados ou existe um conflito. Consulte o recurso novamente.'
    }

    if (erro.status === 422) {
      return 'A operação foi recusada por uma regra de negócio.'
    }
  }

  return 'Tente novamente. Se o problema continuar, verifique a disponibilidade da aplicação.'
}
