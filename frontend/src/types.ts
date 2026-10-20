export type TransactionType = 'INCOME' | 'EXPENSE'
export type TransactionStatus = 'PENDING' | 'PAID'
export type RecurringFrequency = 'WEEKLY' | 'MONTHLY' | 'YEARLY'

export interface Account {
  id: string
  name: string
  type: string
  initialBalance: number
  currentBalance: number
  color: string | null
  active: boolean
}

export interface CreditCard {
  id: string
  name: string
  accountName: string | null
  creditLimit: number
  availableLimit: number
  closingDay: number
  dueDay: number
  color: string | null
}

export type InvoiceStatus = 'OPEN' | 'CLOSED' | 'PAID'

export interface Invoice {
  id: string
  creditCardId: string
  creditCardName: string
  month: number
  year: number
  totalAmount: number
  paidAmount: number
  remainingAmount: number
  status: InvoiceStatus
  dueDate: string
}

export interface Budget {
  id: string
  categoryId: string
  categoryName: string
  month: number
  year: number
  amount: number
  spentAmount: number
  remainingAmount: number
  percentage: number
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
  accountId: string | null
  accountName: string | null
  categoryId: string | null
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

export interface Transfer {
  id: string
  sourceAccountId: string
  sourceAccountName: string
  destinationAccountId: string
  destinationAccountName: string
  amount: number
  date: string
  description: string | null
}

export interface RecurringTransaction {
  id: string
  description: string
  amount: number
  type: TransactionType
  frequency: RecurringFrequency
  nextDate: string
  active: boolean
}
