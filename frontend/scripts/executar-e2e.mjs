import { spawn } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import { resolve } from 'node:path'

const raizProjeto = fileURLToPath(new URL('../../', import.meta.url))
const diretorioFrontend = resolve(raizProjeto, 'frontend')
const composeE2e = resolve(raizProjeto, 'frontend/e2e/compose.e2e.yml')
const projetoDocker = 'qa-labs-assistlar-e2e'
const ambiente = { ...process.env }
const urlApiE2e = 'http://127.0.0.1:18080'

delete ambiente.ELECTRON_RUN_AS_NODE

function executar(comando, argumentos, diretorioAtual = raizProjeto) {
  return new Promise((resolveExecucao, reject) => {
    const processo = spawn(comando, argumentos, { cwd: diretorioAtual, env: ambiente, stdio: 'inherit' })

    processo.on('error', reject)
    processo.on('close', (codigo) => {
      if (codigo === 0) {
        resolveExecucao()
        return
      }

      reject(new Error(`${comando} terminou com codigo ${codigo ?? 'desconhecido'}.`))
    })
  })
}

const argumentosCompose = ['compose', '-p', projetoDocker, '-f', composeE2e]
const cliPlaywright = resolve(raizProjeto, 'frontend/node_modules/@playwright/test/cli.js')

try {
  await executar('docker', [...argumentosCompose, 'up', '--build', '--wait'])
  ambiente.PLAYWRIGHT_API_URL = urlApiE2e
  await executar(process.execPath, [cliPlaywright, 'test', ...process.argv.slice(2)], diretorioFrontend)
} finally {
  await executar('docker', [...argumentosCompose, 'down', '--volumes', '--remove-orphans'])
}
