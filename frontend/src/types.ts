export type TransactionType = 'INCOME' | 'EXPENSE'
export type TransactionStatus = 'PENDING' | 'PAID'

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
