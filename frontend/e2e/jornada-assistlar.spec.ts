import { expect, test, type APIRequestContext } from '@playwright/test'

const apiBase = 'http://127.0.0.1:18080/api'
const planoCompletoId = '10000000-0000-0000-0000-000000000002'

test('conclui a jornada principal pelo navegador', async ({ page }) => {
  const sufixo = gerarSufixo()

  await page.goto('/clientes')
  await page.getByRole('textbox', { name: /^Nome/ }).fill(`Teste E2E ${sufixo}`)
  await page.getByRole('textbox', { name: 'E-mail', exact: true }).fill(`teste-e2e-${sufixo}@example.test`)
  await page.getByRole('textbox', { name: 'Data de nascimento', exact: true }).fill('1990-05-10')
  await page.getByRole('button', { name: 'Cadastrar cliente' }).click()

  await expect(page).toHaveURL(/\/clientes\/[0-9a-f-]{36}$/)
  await page.getByRole('link', { name: 'Avaliar elegibilidade' }).click()
  await page.locator('select#plano').selectOption(planoCompletoId)
  await page.getByRole('button', { name: 'Avaliar elegibilidade' }).click()
  await expect(page.getByRole('status')).toContainText('Cliente elegível')

  await page.getByRole('link', { name: 'Criar contratação' }).click()
  await page.getByRole('button', { name: 'Criar contratação pendente' }).click()
  await expect(page).toHaveURL(/\/contratacoes\/[0-9a-f-]{36}$/)
  await page.getByRole('button', { name: 'Ativar contratação' }).click()
  await expect(page.getByLabel('Status: Ativa')).toBeVisible()

  await page.getByRole('link', { name: 'Abrir solicitação de assistência' }).click()
  await page.locator('select#tipo-assistencia').selectOption('ELETRICISTA')
  await page.getByLabel('Descrição do problema').fill('Tomada sem energia no quarto.')
  await page.getByRole('button', { name: 'Abrir solicitação' }).click()
  await expect(page).toHaveURL(/\/solicitacoes\/[0-9a-f-]{36}$/)

  await page.getByRole('button', { name: 'Iniciar atendimento' }).click()
  await expect(page.getByLabel('Status: Em atendimento')).toBeVisible()
  await page.getByRole('button', { name: 'Concluir atendimento' }).click()
  await expect(page.getByLabel('Status: Concluída')).toBeVisible()
  await expect(page.getByLabel('Histórico da solicitação')).toContainText('Em atendimento → Concluída')
})

test('exige motivo antes de cancelar uma solicitação em atendimento', async ({ page, request }) => {
  const solicitacaoId = await prepararSolicitacaoEmAtendimento(request)

  await page.goto(`/solicitacoes/${solicitacaoId}`)
  await expect(page.getByLabel('Status: Em atendimento')).toBeVisible()
  await page.getByRole('button', { name: 'Cancelar solicitação' }).click()
  await expect(page.getByText('Informe o motivo para cancelar uma solicitação em atendimento.')).toBeVisible()

  await page.getByLabel('Motivo do cancelamento').fill('Cliente solicitou o cancelamento.')
  await page.getByRole('button', { name: 'Cancelar solicitação' }).click()
  await expect(page.getByLabel('Status: Cancelada')).toBeVisible()
  await expect(page.getByLabel('Histórico da solicitação')).toContainText('Em atendimento → Cancelada')
})

async function prepararSolicitacaoEmAtendimento(request: APIRequestContext): Promise<string> {
  const sufixo = gerarSufixo()
  const cliente = await requisitar<{ id: string }>(request, 'POST', '/clientes', {
    nome: `Precondicao E2E ${sufixo}`,
    email: `precondicao-e2e-${sufixo}@example.test`,
    dataNascimento: '1990-05-10',
  })
  const contratacaoPendente = await requisitar<{ id: string }>(request, 'POST', '/contratacoes', {
    clienteId: cliente.id,
    planoId: planoCompletoId,
  })
  const contratacao = await requisitar<{ id: string }>(request, 'POST', `/contratacoes/${contratacaoPendente.id}/ativacao`)
  const solicitacaoAberta = await requisitar<{ id: string }>(request, 'POST', '/solicitacoes-assistencia', {
    contratacaoId: contratacao.id,
    tipoAssistencia: 'ELETRICISTA',
    descricaoProblema: 'Precondicao para validar o cancelamento.',
  })
  const solicitacao = await requisitar<{ id: string }>(request, 'POST', `/solicitacoes-assistencia/${solicitacaoAberta.id}/inicio`)

  return solicitacao.id
}

async function requisitar<T>(
  request: APIRequestContext,
  metodo: 'POST',
  caminho: string,
  data?: Record<string, unknown>,
): Promise<T> {
  const resposta = await request.fetch(`${apiBase}${caminho}`, { method: metodo, data })
  expect(resposta.ok(), `Falha ao preparar ${caminho}: ${await resposta.text()}`).toBeTruthy()
  return resposta.json() as Promise<T>
}

function gerarSufixo(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}
