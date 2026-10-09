export type TransactionType = 'INCOME' | 'EXPENSE'
export type TransactionStatus = 'PENDING' | 'PAID'

export interface Account {
  id: string
  name: string
  type: string
  initialBalance: number
  currentBalance: number
  color: string | null
  active: boolean
}

export interface Category {
  id: string
  name: string
  type: TransactionType
  icon: string | null
  color: string | null
  parentId: string | null
  parentName: string | null
}

export interface CategorySummary {
  categoryName: string
  amount: number
}

export interface Transaction {
  id: string
  accountName: string | null
  creditCardName: string | null
  description: string
  amount: number
  type: TransactionType
  status: TransactionStatus
  date: string
  categoryName: string | null
}

export interface DashboardSummary {
  startDate: string
  endDate: string
  totalBalance: number
  periodIncome: number
  periodExpense: number
  periodResult: number
  pendingIncome: number
  pendingExpense: number
  expensesByCategory: CategorySummary[]
  recentTransactions: Transaction[]
}
