import type {
  Account,
  Budget,
  Category,
  CreditCard,
  DashboardSummary,
  FinancialGoal,
  Invoice,
  RecurringFrequency,
  RecurringTransaction,
  Tag,
  Transaction,
  TransactionStatus,
  TransactionType,
  Transfer,
  UserProfile,
} from '../types'

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

// ==================== AUTH ====================
export async function login(email: string, password: string): Promise<string> {
  const data = await request<{ token: string }>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
  return data.token
}

export async function register(name: string, email: string, password: string, mainCurrency: string): Promise<void> {
  await request<void>('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ name, email, password, mainCurrency }),
  })
}

// ==================== USER PROFILE ====================
export async function getProfile(token: string): Promise<UserProfile> {
  return request<UserProfile>('/users/profile', { headers: auth(token) })
}

export async function updateProfile(token: string, data: { name: string; mainCurrency?: string }): Promise<UserProfile> {
  return request<UserProfile>('/users/profile', {
    method: 'PUT',
    headers: auth(token),
    body: JSON.stringify(data),
  })
}

export async function changePassword(token: string, data: { currentPassword: string; newPassword: string }): Promise<void> {
  await request<void>('/users/change-password', {
    method: 'PUT',
    headers: auth(token),
    body: JSON.stringify(data),
  })
}

// ==================== DASHBOARD ====================
export async function getDashboard(token: string, startDate: string, endDate: string): Promise<DashboardSummary> {
  return request<DashboardSummary>(`/dashboard/summary?startDate=${startDate}&endDate=${endDate}`, { headers: auth(token) })
}

// ==================== ACCOUNTS ====================
export async function getAccounts(token: string, activeOnly = false): Promise<Account[]> {
  return request<Account[]>(`/accounts${activeOnly ? '?activeOnly=true' : ''}`, { headers: auth(token) })
}

export async function getAccountById(token: string, id: string): Promise<Account> {
  return request<Account>(`/accounts/${id}`, { headers: auth(token) })
}

export async function createAccount(token: string, account: { name: string; type: string; initialBalance: number; color: string }): Promise<Account> {
  return request<Account>('/accounts', { method: 'POST', headers: auth(token), body: JSON.stringify(account) })
}

export async function updateAccount(token: string, id: string, account: { name: string; type: string; initialBalance?: number; color?: string; active?: boolean }): Promise<Account> {
  return request<Account>(`/accounts/${id}`, { method: 'PUT', headers: auth(token), body: JSON.stringify(account) })
}

export async function deleteAccount(token: string, id: string): Promise<void> {
  await request<void>(`/accounts/${id}`, { method: 'DELETE', headers: auth(token) })
}

// ==================== CATEGORIES ====================
export async function getCategories(token: string, type?: TransactionType, activeOnly = false): Promise<Category[]> {
  const params = new URLSearchParams()
  if (type) params.set('type', type)
  if (activeOnly) params.set('activeOnly', 'true')
  const query = params.toString()
  return request<Category[]>(`/categories${query ? `?${query}` : ''}`, { headers: auth(token) })
}

export async function getCategoryById(token: string, id: string): Promise<Category> {
  return request<Category>(`/categories/${id}`, { headers: auth(token) })
}

export async function createCategory(token: string, category: { name: string; type: TransactionType; icon: string; color: string; parentId: string | null }): Promise<Category> {
  return request<Category>('/categories', { method: 'POST', headers: auth(token), body: JSON.stringify(category) })
}

export async function updateCategory(token: string, id: string, category: { name: string; type: TransactionType; icon: string; color: string; parentId: string | null }): Promise<Category> {
  return request<Category>(`/categories/${id}`, { method: 'PUT', headers: auth(token), body: JSON.stringify(category) })
}

export async function deleteCategory(token: string, id: string): Promise<void> {
  await request<void>(`/categories/${id}`, { method: 'DELETE', headers: auth(token) })
}

// ==================== CREDIT CARDS ====================
export async function getCreditCards(token: string, activeOnly = false): Promise<CreditCard[]> {
  return request<CreditCard[]>(`/credit-cards${activeOnly ? '?activeOnly=true' : ''}`, { headers: auth(token) })
}

export async function getCreditCardById(token: string, id: string): Promise<CreditCard> {
  return request<CreditCard>(`/credit-cards/${id}`, { headers: auth(token) })
}

export async function createCreditCard(token: string, card: {
  name: string
  accountId?: string
  creditLimit: number
  closingDay: number
  dueDay: number
  color: string
}): Promise<CreditCard> {
  return request<CreditCard>('/credit-cards', { method: 'POST', headers: auth(token), body: JSON.stringify(card) })
}

export async function updateCreditCard(token: string, id: string, card: {
  name: string
  accountId?: string
  creditLimit: number
  closingDay: number
  dueDay: number
  color: string
}): Promise<CreditCard> {
  return request<CreditCard>(`/credit-cards/${id}`, { method: 'PUT', headers: auth(token), body: JSON.stringify(card) })
}

export async function deleteCreditCard(token: string, id: string): Promise<void> {
  await request<void>(`/credit-cards/${id}`, { method: 'DELETE', headers: auth(token) })
}

// ==================== INVOICES ====================
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

// ==================== TAGS ====================
export async function getTags(token: string): Promise<Tag[]> {
  return request<Tag[]>('/tags', { headers: auth(token) })
}

export async function createTag(token: string, tag: { name: string; color?: string }): Promise<Tag> {
  return request<Tag>('/tags', { method: 'POST', headers: auth(token), body: JSON.stringify(tag) })
}

export async function updateTag(token: string, id: string, tag: { name: string; color?: string }): Promise<Tag> {
  return request<Tag>(`/tags/${id}`, { method: 'PUT', headers: auth(token), body: JSON.stringify(tag) })
}

export async function deleteTag(token: string, id: string): Promise<void> {
  await request<void>(`/tags/${id}`, { method: 'DELETE', headers: auth(token) })
}

// ==================== TRANSACTIONS ====================
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

export async function createTransaction(token: string, transaction: {
  description: string
  amount: number
  type: TransactionType
  status: TransactionStatus
  date: string
  categoryId: string
  accountId?: string
  creditCardId?: string
  totalInstallments?: number
  tagIds?: string[]
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
  totalInstallments?: number
  tagIds?: string[]
}): Promise<Transaction> {
  return request<Transaction>(`/transactions/${transactionId}`, { method: 'PUT', headers: auth(token), body: JSON.stringify(transaction) })
}

export async function deleteTransaction(token: string, transactionId: string): Promise<void> {
  await request<void>(`/transactions/${transactionId}`, { method: 'DELETE', headers: auth(token) })
}

// ==================== TRANSFERS ====================
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

// ==================== RECURRING ====================
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

// ==================== BUDGETS ====================
export async function getBudgets(token: string, month?: number, year?: number): Promise<Budget[]> {
  const params = new URLSearchParams()
  if (month) params.set('month', String(month))
  if (year) params.set('year', String(year))
  const query = params.toString()
  return request<Budget[]>(`/budgets${query ? `?${query}` : ''}`, { headers: auth(token) })
}

export async function createBudget(token: string, budget: { categoryId: string; month: number; year: number; amount: number }): Promise<Budget> {
  return request<Budget>('/budgets', { method: 'POST', headers: auth(token), body: JSON.stringify(budget) })
}

export async function deleteBudget(token: string, budgetId: string): Promise<void> {
  await request<void>(`/budgets/${budgetId}`, { method: 'DELETE', headers: auth(token) })
}

// ==================== GOALS ====================
export async function getGoals(token: string): Promise<FinancialGoal[]> {
  return request<FinancialGoal[]>('/goals', { headers: auth(token) })
}

export async function createGoal(token: string, goal: { name: string; targetAmount: number; deadline?: string }): Promise<FinancialGoal> {
  return request<FinancialGoal>('/goals', { method: 'POST', headers: auth(token), body: JSON.stringify(goal) })
}

export async function contributeToGoal(token: string, goalId: string, amount: number): Promise<FinancialGoal> {
  return request<FinancialGoal>(`/goals/${goalId}/contributions`, {
    method: 'POST',
    headers: auth(token),
    body: JSON.stringify({ amount }),
  })
}

export async function deleteGoal(token: string, goalId: string): Promise<void> {
  await request<void>(`/goals/${goalId}`, { method: 'DELETE', headers: auth(token) })
}
