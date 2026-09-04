import http from 'k6/http'
import { check, sleep } from 'k6'

const baseUrl = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export const options = {
  scenarios: {
    monitoramento_health: {
      executor: 'constant-vus',
      exec: 'consultarHealth',
      vus: 1,
      duration: '45s',
    },
    consulta_planos: {
      executor: 'ramping-vus',
      exec: 'consultarPlanos',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 2 },
        { duration: '25s', target: 5 },
        { duration: '10s', target: 0 },
      ],
      gracefulRampDown: '5s',
    },
    listagem_clientes: {
      executor: 'constant-arrival-rate',
      exec: 'listarClientes',
      startTime: '5s',
      rate: 2,
      timeUnit: '1s',
      duration: '35s',
      preAllocatedVUs: 2,
      maxVUs: 10,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],
    'http_req_duration{recurso:health}': ['p(95)<250'],
    'http_req_duration{recurso:planos}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{recurso:clientes}': ['p(95)<750', 'p(99)<1200'],
    dropped_iterations: ['count==0'],
  },
}

export function consultarHealth() {
  const resposta = http.get(`${baseUrl}/actuator/health`, {
    tags: { recurso: 'health' },
  })

  check(resposta, {
    'health retorna 200': (res) => res.status === 200,
    'health informa UP': (res) => res.json('status') === 'UP',
  })
  sleep(2)
}

export function consultarPlanos() {
  const resposta = http.get(`${baseUrl}/api/planos`, {
    tags: { recurso: 'planos' },
  })

  check(resposta, {
    'planos retorna 200': (res) => res.status === 200,
    'planos retorna uma lista nao vazia': (res) => Array.isArray(res.json()) && res.json().length > 0,
  })
  sleep(1)
}

export function listarClientes() {
  const resposta = http.get(`${baseUrl}/api/clientes?pagina=0&tamanho=20`, {
    tags: { recurso: 'clientes' },
  })

  check(resposta, {
    'clientes retorna 200': (res) => res.status === 200,
    'clientes retorna pagina valida': (res) => Array.isArray(res.json('itens')) && res.json('pagina') === 0,
  })
}
