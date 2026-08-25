import { expect, test } from '@playwright/test'

test.describe('regressao visual', () => {
  test('preserva a pagina inicial do AssistLar', async ({ page }, testInfo) => {
    test.skip(testInfo.project.name !== 'chromium', 'Baseline visual mantida no Chromium.')
    await page.goto('/')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    await expect(page).toHaveScreenshot('pagina-inicial.png', {
      fullPage: true,
      animations: 'disabled',
    })
  })
})
