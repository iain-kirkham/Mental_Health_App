import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import tsconfigPaths from 'vite-tsconfig-paths'

export default defineConfig({
    plugins: [tsconfigPaths(), react()],
    test: {
        environment: 'jsdom',
        // Only run unit/component tests colocated under src by default.
        // Exclude the Playwright E2E tests in the top-level `tests/` directory
        // which import `@playwright/test` and must be run with Playwright.
        include: ['src/**/*.test.{js,ts,jsx,tsx}', 'src/**/*.spec.{js,ts,jsx,tsx}'],
        exclude: ['tests/**', '**/tests/**', 'playwright.config.*'],
    },
})