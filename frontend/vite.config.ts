import react from '@vitejs/plugin-react'
import { loadEnv } from 'vite'
import { configDefaults, defineConfig } from 'vitest/config'

export default defineConfig(({ mode }) => {
  const ambiente = loadEnv(mode, process.cwd(), '')
  const destinoApi = ambiente.API_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': {
          target: destinoApi,
          changeOrigin: true,
        },
      },
    },
    test: {
      environment: 'jsdom',
      setupFiles: './src/test/setup.ts',
      css: true,
      pool: 'forks',
      maxWorkers: 1,
      fileParallelism: false,
      testTimeout: 10_000,
      exclude: [...configDefaults.exclude, 'e2e/**'],
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json-summary', 'html'],
        reportsDirectory: 'coverage',
        all: true,
        include: ['src/**/*.{ts,tsx}'],
        exclude: [
          'src/**/*.cy.{ts,tsx}',
          'src/main.tsx',
          'src/vite-env.d.ts',
          'e2e/**',
          'playwright.config.ts'
        ],
        thresholds: {
          statements: 80,
          branches: 70,
          functions: 80,
          lines: 80,
        },
      },
    },
  }
})
