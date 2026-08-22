import http from 'k6/http'
import { check, group, sleep } from 'k6'

const baseUrl = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export const options = {
  scenarios: {
    carga_leve: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 1 },
        { duration: '20s', target: 5 },
        { duration: '10s', target: 0 },
      ],
      gracefulRampDown: '5s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    checks: ['rate>0.99'],
  },
}

export default function () {
  group('health da aplicacao', function () {
    const resposta = http.get(`${baseUrl}/actuator/health`, {
      tags: { recurso: 'health' },
    })

    check(resposta, {
      'health retorna 200': (res) => res.status === 200,
      'health informa UP': (res) => res.json('status') === 'UP',
    })
  })

  group('consulta de planos', function () {
    const resposta = http.get(`${baseUrl}/api/planos`, {
      tags: { recurso: 'planos' },
    })

    check(resposta, {
      'planos retorna 200': (res) => res.status === 200,
      'planos retorna uma lista': (res) => Array.isArray(res.json()),
    })
  })

  sleep(1)
}
