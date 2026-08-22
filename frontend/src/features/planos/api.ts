import { requisitarJson } from '../../shared/api/http'
import type { PlanoAssistencia } from './types'

export function listarPlanos() {
  return requisitarJson<PlanoAssistencia[]>('/planos')
}

export function consultarPlano(id: string) {
  return requisitarJson<PlanoAssistencia>(`/planos/${id}`)
}
