import type { DashboardSummary } from '../types'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  if (!response.ok) {
    throw new Error('Não foi possível concluir a solicitação.')
  }

  return response.json() as Promise<T>
}

export async function login(email: string, password: string): Promise<string> {
  const data = await request<{ token: string }>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
  return data.token
}

export async function getDashboard(
  token: string,
  startDate: string,
  endDate: string,
): Promise<DashboardSummary> {
  return request<DashboardSummary>(
    `/dashboard/summary?startDate=${startDate}&endDate=${endDate}`,
    { headers: { Authorization: `Bearer ${token}` } },
  )
}
