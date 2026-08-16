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

  it.each([
    ['rede', undefined, 'Verifique se o backend está disponível.'],
    ['http', 409, 'Os dados foram alterados ou existe um conflito.'],
    ['http', 422, 'A operação foi recusada por uma regra de negócio.'],
  ] as const)('traduz o erro %s com status %s para uma mensagem segura', (tipo, status, mensagem) => {
    render(<AlertaErro erro={new ErroHttp({ tipo, status })} />)

    expect(screen.getByRole('alert')).toHaveTextContent(mensagem)
  })
})
