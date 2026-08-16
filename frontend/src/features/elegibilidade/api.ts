import { requisitarJson } from '../../shared/api/http'
import type { ResultadoElegibilidade } from './types'

export function consultarElegibilidade(clienteId: string, planoId: string) {
  const parametros = new URLSearchParams({ clienteId, planoId })
  return requisitarJson<ResultadoElegibilidade>(`/elegibilidades?${parametros.toString()}`)
}
