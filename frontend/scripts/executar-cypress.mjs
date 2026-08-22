import { spawn } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const ambiente = { ...process.env }
delete ambiente.ELECTRON_RUN_AS_NODE

const cliCypress = fileURLToPath(new URL('../node_modules/cypress/bin/cypress', import.meta.url))
const processo = spawn(process.execPath, [cliCypress, ...process.argv.slice(2)], {
  env: ambiente,
  stdio: 'inherit',
})

processo.on('error', (erro) => {
  console.error(erro)
  process.exitCode = 1
})

processo.on('exit', (codigo, sinal) => {
  process.exitCode = codigo ?? (sinal ? 1 : 0)
})
