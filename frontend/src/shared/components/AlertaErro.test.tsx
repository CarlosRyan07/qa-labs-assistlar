import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { ErroHttp } from '../api/problema-api'
import { AlertaErro } from './AlertaErro'

describe('AlertaErro', () => {
  it('apresenta ProblemDetail e os erros de campo sem perder a semântica de alerta', () => {
    const erro = new ErroHttp({
      tipo: 'problema',
      status: 400,
      problema: {
        title: 'Requisição inválida',
        status: 400,
        detail: 'Revise os dados informados.',
        erros: [{ campo: 'email', mensagem: 'O e-mail deve ter um formato válido.' }],
      },
    })

    render(<AlertaErro erro={erro} />)

    expect(screen.getByRole('alert')).toHaveTextContent('Requisição inválida')
    expect(screen.getByRole('list', { name: 'Campos com erro' })).toHaveTextContent(
      'email: O e-mail deve ter um formato válido.',
    )
  })

  it('usa mensagem segura quando a resposta não possui ProblemDetail', () => {
    render(<AlertaErro erro={new Error('SQL exception em tabela cliente')} />)

    expect(screen.getByRole('alert')).not.toHaveTextContent('SQL exception')
    expect(screen.getByRole('alert')).toHaveTextContent('Tente novamente')
  })
})
