import { describe, expect, it } from 'vitest'

import { ehUuid } from './uuid'

describe('ehUuid', () => {
  it('aceita o formato canônico entendido pelo UUID da API', () => {
    expect(ehUuid('10000000-0000-0000-0000-000000000001')).toBe(true)
    expect(ehUuid('8D8C18AF-760D-4B37-938B-B7C11B68BE34')).toBe(true)
  })

  it('rejeita texto e UUID incompleto', () => {
    expect(ehUuid('nao-e-uuid')).toBe(false)
    expect(ehUuid('10000000-0000-0000-0000')).toBe(false)
  })
})
