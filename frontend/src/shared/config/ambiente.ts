const apiUrlPadrao = '/api'

export const ambiente = {
  apiUrl: (import.meta.env.VITE_API_URL || apiUrlPadrao).replace(/\/$/, ''),
} as const
