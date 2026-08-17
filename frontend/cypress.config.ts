import { defineConfig } from 'cypress'

export default defineConfig({
  allowCypressEnv: false,
  component: {
    devServer: {
      framework: 'react',
      bundler: 'vite',
    },
    specPattern: 'src/**/*.cy.{ts,tsx}',
    supportFile: 'cypress/support/component.ts',
    video: false,
    screenshotOnRunFailure: true,
  },
})
