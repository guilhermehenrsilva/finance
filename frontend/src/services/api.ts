import type { Account, Category, CreditCard, DashboardSummary, Invoice, RecurringFrequency, RecurringTransaction, Transaction, TransactionStatus, TransactionType, Transfer } from '../types'

const API_URL = import.meta.env.VITE_API_URL
  ?? `${window.location.protocol}//${window.location.hostname}:8080/api`

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  })
  const body = await response.text()

  if (!response.ok) {
    try {
      const error = JSON.parse(body) as { message?: string }
      throw new Error(error.message ?? `A API recusou a solicitação (${response.status}).`)
    } catch (error) {
      if (error instanceof Error && error.message !== 'Unexpected end of JSON input') throw error
      throw new Error(`A API recusou a solicitação (${response.status}).`)
    }
  }

  if (!body.trim()) return undefined as T
  try {
    return JSON.parse(body) as T
  } catch {
    throw new Error('A API retornou uma resposta inválida.')
  }
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

export async function getCreditCards(token: string): Promise<CreditCard[]> {
  return request<CreditCard[]>('/credit-cards', { headers: auth(token) })
}

export async function createCreditCard(token: string, card: {
  name: string
  accountId: string
  creditLimit: number
  closingDay: number
  dueDay: number
  color: string
}): Promise<CreditCard> {
  return request<CreditCard>('/credit-cards', { method: 'POST', headers: auth(token), body: JSON.stringify(card) })
}

export async function getInvoices(token: string): Promise<Invoice[]> {
  return request<Invoice[]>('/invoices', { headers: auth(token) })
}

export async function payInvoice(token: string, invoiceId: string, payment: {
  accountId: string
  amount?: number
}): Promise<Invoice> {
  return request<Invoice>(`/invoices/${invoiceId}/pay`, {
    method: 'POST',
    headers: auth(token),
    body: JSON.stringify(payment),
  })
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
  creditCardId?: string
  totalInstallments: number
}): Promise<Transaction> {
  return request<Transaction>('/transactions', { method: 'POST', headers: auth(token), body: JSON.stringify(transaction) })
}

export async function updateTransaction(token: string, transactionId: string, transaction: {
  description: string
  amount: number
  type: TransactionType
  status: TransactionStatus
  date: string
  categoryId: string
  accountId: string
  totalInstallments: number
}): Promise<Transaction> {
  return request<Transaction>(`/transactions/${transactionId}`, { method: 'PUT', headers: auth(token), body: JSON.stringify(transaction) })
}

export async function getTransactions(token: string, filters: {
  startDate?: string
  endDate?: string
  type?: TransactionType
  status?: TransactionStatus
  categoryId?: string
  accountId?: string
  creditCardId?: string
  description?: string
} = {}): Promise<Transaction[]> {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  const query = params.toString()
  return request<Transaction[]>(`/transactions${query ? `?${query}` : ''}`, { headers: auth(token) })
}

export async function deleteTransaction(token: string, transactionId: string): Promise<void> {
  await request<void>(`/transactions/${transactionId}`, { method: 'DELETE', headers: auth(token) })
}

export async function getTransfers(token: string): Promise<Transfer[]> {
  return request<Transfer[]>('/transfers', { headers: auth(token) })
}

export async function createTransfer(token: string, transfer: {
  sourceAccountId: string
  destinationAccountId: string
  amount: number
  date: string
  description?: string
}): Promise<Transfer> {
  return request<Transfer>('/transfers', {
    method: 'POST',
    headers: auth(token),
    body: JSON.stringify(transfer),
  })
}

export async function deleteTransfer(token: string, transferId: string): Promise<void> {
  await request<void>(`/transfers/${transferId}`, { method: 'DELETE', headers: auth(token) })
}

export async function getRecurringTransactions(token: string): Promise<RecurringTransaction[]> {
  return request<RecurringTransaction[]>('/recurring-transactions', { headers: auth(token) })
}

export async function createRecurringTransaction(token: string, recurring: {
  description: string
  amount: number
  type: TransactionType
  frequency: RecurringFrequency
  startDate: string
  endDate?: string
  categoryId: string
  accountId?: string
  creditCardId?: string
}): Promise<RecurringTransaction> {
  return request<RecurringTransaction>('/recurring-transactions', {
    method: 'POST',
    headers: auth(token),
    body: JSON.stringify(recurring),
  })
}

export async function toggleRecurringTransaction(token: string, recurringId: string): Promise<RecurringTransaction> {
  return request<RecurringTransaction>(`/recurring-transactions/${recurringId}/toggle`, {
    method: 'PATCH',
    headers: auth(token),
  })
}

export async function deleteRecurringTransaction(token: string, recurringId: string): Promise<void> {
  await request<void>(`/recurring-transactions/${recurringId}`, { method: 'DELETE', headers: auth(token) })
}
