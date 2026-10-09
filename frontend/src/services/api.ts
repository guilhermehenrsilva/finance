import type { Account, Category, DashboardSummary, Transaction, TransactionStatus, TransactionType } from '../types'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  })
  if (!response.ok) throw new Error('Não foi possível concluir a solicitação.')
  const body = await response.text()
  return (body ? JSON.parse(body) : undefined) as T
}

const auth = (token: string): HeadersInit => ({ Authorization: `Bearer ${token}` })

export async function login(email: string, password: string): Promise<string> {
  const data = await request<{ token: string }>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
  return data.token
}

export async function register(name: string, email: string, password: string, mainCurrency: string): Promise<void> {
  await request<void>('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, password, mainCurrency }) })
}

export async function getDashboard(token: string, startDate: string, endDate: string): Promise<DashboardSummary> {
  return request<DashboardSummary>(`/dashboard/summary?startDate=${startDate}&endDate=${endDate}`, { headers: auth(token) })
}

export async function getAccounts(token: string): Promise<Account[]> {
  return request<Account[]>('/accounts', { headers: auth(token) })
}

export async function createAccount(token: string, account: { name: string; type: string; initialBalance: number; color: string }): Promise<Account> {
  return request<Account>('/accounts', { method: 'POST', headers: auth(token), body: JSON.stringify(account) })
}

export async function getCategories(token: string): Promise<Category[]> {
  return request<Category[]>('/categories', { headers: auth(token) })
}

export async function createCategory(token: string, category: { name: string; type: TransactionType; icon: string; color: string; parentId: string | null }): Promise<Category> {
  return request<Category>('/categories', { method: 'POST', headers: auth(token), body: JSON.stringify(category) })
}

export async function createTransaction(token: string, transaction: {
  description: string
  amount: number
  type: TransactionType
  status: TransactionStatus
  date: string
  categoryId: string
  accountId: string
  totalInstallments: number
}): Promise<Transaction> {
  return request<Transaction>('/transactions', { method: 'POST', headers: auth(token), body: JSON.stringify(transaction) })
}
