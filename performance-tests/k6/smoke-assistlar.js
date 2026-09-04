import http from 'k6/http'
import { check, group } from 'k6'

const baseUrl = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export const options = {
  vus: 1,
  iterations: 3,
  thresholds: {
    http_req_failed: ['rate==0'],
    http_req_duration: ['p(95)<1000'],
    checks: ['rate==1'],
  },
}

export default function () {
  group('disponibilidade', function () {
    const resposta = http.get(`${baseUrl}/actuator/health`)
    check(resposta, {
      'health retorna 200': (res) => res.status === 200,
      'health informa UP': (res) => res.json('status') === 'UP',
    })
  })

  group('consultas publicas', function () {
    const planos = http.get(`${baseUrl}/api/planos`)
    check(planos, {
      'planos retorna 200': (res) => res.status === 200,
      'planos retorna uma lista nao vazia': (res) => Array.isArray(res.json()) && res.json().length > 0,
    })

    const clientes = http.get(`${baseUrl}/api/clientes?pagina=0&tamanho=1`)
    check(clientes, {
      'clientes retorna 200': (res) => res.status === 200,
      'clientes retorna pagina valida': (res) => Array.isArray(res.json('itens')) && res.json('pagina') === 0,
    })
  })
}
