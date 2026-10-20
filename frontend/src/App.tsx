import { FormEvent, useEffect, useMemo, useState } from 'react'
import { createAccount, createCategory, createCreditCard, createRecurringTransaction, createTransaction, createTransfer, deleteRecurringTransaction, deleteTransaction, deleteTransfer, getAccounts, getCategories, getCreditCards, getDashboard, getInvoices, getRecurringTransactions, getTransactions, getTransfers, login, payInvoice, register, toggleRecurringTransaction, updateTransaction } from './services/api'
import type { Account, Category, CreditCard, DashboardSummary, Invoice, RecurringFrequency, RecurringTransaction, Transaction, TransactionType, Transfer } from './types'

const TOKEN_KEY = 'finance.auth.token'
type PageKey = 'dashboard' | 'transactions' | 'accounts' | 'categories' | 'cards' | 'invoices' | 'transfers' | 'recurring'

function pageFromHash(): PageKey {
  const value = window.location.hash.replace('#', '') as PageKey
  return ['dashboard', 'transactions', 'accounts', 'categories', 'cards', 'invoices', 'transfers', 'recurring'].includes(value)
    ? value
    : 'dashboard'
}

function currentMonthRange() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const lastDay = new Date(year, now.getMonth() + 1, 0).getDate()
  return {
    start: `${year}-${month}-01`,
    end: `${year}-${month}-${String(lastDay).padStart(2, '0')}`,
  }
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR').format(new Date(`${value}T00:00:00`))
}

function App() {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [page, setPage] = useState<PageKey>(pageFromHash)
  const range = useMemo(currentMonthRange, [])
  const [startDate, setStartDate] = useState(range.start)
  const [endDate, setEndDate] = useState(range.end)
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [refresh, setRefresh] = useState(0)
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [creditCards, setCreditCards] = useState<CreditCard[]>([])
  const [invoices, setInvoices] = useState<Invoice[]>([])
  const [transfers, setTransfers] = useState<Transfer[]>([])
  const [recurringTransactions, setRecurringTransactions] = useState<RecurringTransaction[]>([])

  useEffect(() => {
    const onHashChange = () => setPage(pageFromHash())
    window.addEventListener('hashchange', onHashChange)
    return () => window.removeEventListener('hashchange', onHashChange)
  }, [])

  useEffect(() => {
    if (!token) return
    setLoading(true)
    getDashboard(token, startDate, endDate)
      .then(setSummary)
      .catch(() => {
        setError('Sua sessão pode ter expirado. Entre novamente.')
        localStorage.removeItem(TOKEN_KEY)
        setToken(null)
      })
      .finally(() => setLoading(false))
    getAccounts(token)
      .then(setAccounts)
      .catch(() => setError('Não foi possível carregar suas contas.'))
    getCategories(token)
      .then(setCategories)
      .catch(() => setError('Não foi possível carregar suas categorias.'))
    getTransactions(token, { startDate, endDate })
      .then(setTransactions)
      .catch(() => setError('Não foi possível carregar suas movimentações.'))
    getCreditCards(token)
      .then(setCreditCards)
      .catch(() => setError('Não foi possível carregar seus cartões.'))
    getInvoices(token)
      .then(setInvoices)
      .catch(() => setError('Não foi possível carregar suas faturas.'))
    getTransfers(token)
      .then(setTransfers)
      .catch(() => setError('Não foi possível carregar suas transferências.'))
    getRecurringTransactions(token)
      .then(setRecurringTransactions)
      .catch(() => setError('Não foi possível carregar suas transações recorrentes.'))
  }, [token, startDate, endDate, refresh])

  function handleLogin(newToken: string) {
    localStorage.setItem(TOKEN_KEY, newToken)
    setToken(newToken)
    setError('')
  }

  function navigate(nextPage: PageKey) {
    window.location.hash = nextPage
    setPage(nextPage)
  }

  if (!token) {
    return <LoginScreen onLogin={handleLogin} />
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark">$</span><span>Finance</span></div>
        <nav>
          <a className={`nav-item ${page === 'dashboard' ? 'active' : ''}`} href="#dashboard">Visão geral</a>
          <a className={`nav-item ${page === 'transactions' ? 'active' : ''}`} href="#transactions">Movimentações</a>
          <a className={`nav-item ${page === 'accounts' ? 'active' : ''}`} href="#accounts">Contas</a>
          <a className={`nav-item ${page === 'categories' ? 'active' : ''}`} href="#categories">Categorias</a>
          <a className={`nav-item ${page === 'cards' ? 'active' : ''}`} href="#cards">Cartões</a>
          <a className={`nav-item ${page === 'invoices' ? 'active' : ''}`} href="#invoices">Faturas</a>
          <a className={`nav-item ${page === 'transfers' ? 'active' : ''}`} href="#transfers">Transferências</a>
          <a className={`nav-item ${page === 'recurring' ? 'active' : ''}`} href="#recurring">Recorrências</a>
        </nav>
        <button className="logout-button" onClick={() => {
          localStorage.removeItem(TOKEN_KEY)
          setToken(null)
        }}>Sair da conta</button>
      </aside>

      <main className="content">
        <header className="page-header">
          <div>
            <p className="eyebrow">{page === 'dashboard' ? 'Visão geral' : page === 'transactions' ? 'Movimentações' : page === 'accounts' ? 'Contas' : page === 'categories' ? 'Categorias' : page === 'cards' ? 'Cartões' : page === 'invoices' ? 'Faturas' : page === 'transfers' ? 'Transferências' : 'Recorrências'}</p>
            <h1>{page === 'dashboard' ? 'Seu dinheiro, sob controle.' : 'Organize sua vida financeira.'}</h1>
          </div>
          <div className="period-selector">
            <label>De <input type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} /></label>
            <label>Até <input type="date" value={endDate} onChange={(event) => setEndDate(event.target.value)} /></label>
          </div>
        </header>

        {error && <div className="alert error">{error}</div>}
        {loading && <div className="loading">Atualizando seus dados...</div>}
        {summary && <Dashboard page={page} summary={summary} accounts={accounts} categories={categories} creditCards={creditCards} invoices={invoices} transactions={transactions} transfers={transfers} recurringTransactions={recurringTransactions} token={token} onNavigate={navigate} onAccountCreated={(account) => setAccounts((current) => [...current, account])} onCategoryCreated={(category) => setCategories((current) => [...current, category])} onCreditCardCreated={(card) => setCreditCards((current) => [...current, card])} onTransactionCreated={() => setRefresh((current) => current + 1)} onTransactionsChanged={() => setRefresh((current) => current + 1)} onTransfersChanged={() => setRefresh((current) => current + 1)} onInvoicesChanged={() => setRefresh((current) => current + 1)} onRecurringChanged={() => setRefresh((current) => current + 1)} />}
      </main>
    </div>
  )
}

function LoginScreen({ onLogin }: { onLogin: (token: string) => void }) {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      if (mode === 'register') {
        if (password !== confirmation) {
          setError('As senhas não conferem.')
          return
        }
        await register(name, email, password, 'BRL')
        setMode('login')
        setPassword('')
        setConfirmation('')
        setError('Cadastro realizado. Agora entre com seus dados.')
        return
      }
      onLogin(await login(email, password))
    } catch {
      setError(mode === 'register'
        ? 'Não foi possível realizar o cadastro. Verifique se o e-mail já está em uso.'
        : 'E-mail ou senha inválidos.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-panel">
        <div className="brand"><span className="brand-mark">$</span><span>Finance</span></div>
        <p className="eyebrow">{mode === 'login' ? 'Bem-vindo de volta' : 'Comece agora'}</p>
        <h1>{mode === 'login' ? 'Cuide melhor das suas finanças.' : 'Organize sua vida financeira.'}</h1>
        <p className="muted">Acompanhe seu dinheiro de forma simples, clara e segura.</p>
        <form onSubmit={submit} className="login-form">
          {mode === 'register' && <label>Nome<input required type="text" value={name} onChange={(event) => setName(event.target.value)} /></label>}
          <label>E-mail<input required type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
          <label>Senha<input required type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          {mode === 'register' && <label>Confirmar senha<input required type="password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} /></label>}
          {error && <div className="alert error">{error}</div>}
          <button className="primary-button" disabled={loading}>{loading ? 'Aguarde...' : mode === 'login' ? 'Entrar' : 'Criar conta'}</button>
        </form>
        <button className="auth-switch" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError('') }}>
          {mode === 'login' ? 'Ainda não tenho uma conta' : 'Já tenho uma conta'}
        </button>
      </section>
      <section className="login-illustration">
        <div className="illustration-card"><span>Saldo total</span><strong>R$ 12.840,50</strong><div className="illustration-chart" /></div>
      </section>
    </main>
  )
}

function Dashboard({ page, summary, accounts, categories, creditCards, invoices, transactions, transfers, recurringTransactions, token, onNavigate, onAccountCreated, onCategoryCreated, onCreditCardCreated, onTransactionCreated, onTransactionsChanged, onTransfersChanged, onInvoicesChanged, onRecurringChanged }: {
  page: PageKey
  summary: DashboardSummary
  accounts: Account[]
  categories: Category[]
  creditCards: CreditCard[]
  invoices: Invoice[]
  transactions: Transaction[]
  transfers: Transfer[]
  recurringTransactions: RecurringTransaction[]
  token: string
  onNavigate: (page: PageKey) => void
  onAccountCreated: (account: Account) => void
  onCategoryCreated: (category: Category) => void
  onCreditCardCreated: (card: CreditCard) => void
  onTransactionCreated: () => void
  onTransactionsChanged: () => void
  onTransfersChanged: () => void
  onInvoicesChanged: () => void
  onRecurringChanged: () => void
}) {
  const refreshTransactions = () => onTransactionsChanged()

  return (
    <div className="dashboard">
      {page === 'dashboard' && <section className="metrics-grid">
        <MetricCard label="Saldo total" value={formatCurrency(summary.totalBalance)} tone="blue" />
        <MetricCard label="Receitas no período" value={formatCurrency(summary.periodIncome)} tone="green" />
        <MetricCard label="Despesas no período" value={formatCurrency(summary.periodExpense)} tone="red" />
        <MetricCard label="Resultado" value={formatCurrency(summary.periodResult)} tone={summary.periodResult >= 0 ? 'green' : 'red'} />
      </section>}
      {page === 'dashboard' && <section className="dashboard-grid">
        <article className="card category-card">
          <div className="card-heading"><div><p className="eyebrow">Distribuição</p><h2>Despesas por categoria</h2></div></div>
          {summary.expensesByCategory.length === 0 ? <EmptyState text="Nenhuma despesa paga no período." /> : <CategoryBars categories={summary.expensesByCategory} />}
        </article>
        <article className="card pending-card">
          <div className="card-heading"><div><p className="eyebrow">Previsões</p><h2>Em aberto</h2></div></div>
          <div className="pending-row"><span>Contas a receber</span><strong className="positive">{formatCurrency(summary.pendingIncome)}</strong></div>
          <div className="pending-row"><span>Contas a pagar</span><strong className="negative">{formatCurrency(summary.pendingExpense)}</strong></div>
          <div className="pending-tip">Mantenha suas contas em dia para ter uma visão fiel do seu saldo.</div>
        </article>
      </section>}
      {page === 'dashboard' && <section className="card transactions-card" id="transactions">
        <div className="card-heading"><div><p className="eyebrow">Atividade</p><h2>Últimas movimentações</h2></div><button className="text-button">Ver todas</button></div>
        {summary.recentTransactions.length === 0 ? <EmptyState text="Nenhuma movimentação encontrada." /> : <TransactionList transactions={summary.recentTransactions} />}
        <button className="primary-small-button page-action" onClick={() => onNavigate('transactions')}>Ver todas as movimentações</button>
      </section>}
      {page === 'accounts' && <AccountsSection accounts={accounts} token={token} onAccountCreated={onAccountCreated} />}
      {page === 'transfers' && <TransfersSection accounts={accounts} transfers={transfers} token={token} onChanged={onTransfersChanged} />}
      {page === 'categories' && <CategoriesSection categories={categories} token={token} onCategoryCreated={onCategoryCreated} />}
      {page === 'cards' && <CreditCardsSection cards={creditCards} accounts={accounts} token={token} onCreated={onCreditCardCreated} />}
      {page === 'invoices' && <InvoicesSection invoices={invoices} accounts={accounts} token={token} onChanged={onInvoicesChanged} />}
      {page === 'recurring' && <RecurringTransactionsSection recurringTransactions={recurringTransactions} accounts={accounts} categories={categories} creditCards={creditCards} token={token} onChanged={onRecurringChanged} />}
      {page === 'transactions' && <TransactionsSection accounts={accounts} categories={categories} creditCards={creditCards} transactions={transactions} token={token} onCreated={onTransactionCreated} onChanged={refreshTransactions} />}
    </div>
  )
}

function TransfersSection({ accounts, transfers, token, onChanged }: {
  accounts: Account[]
  transfers: Transfer[]
  token: string
  onChanged: () => void
}) {
  const [open, setOpen] = useState(false)
  const [sourceAccountId, setSourceAccountId] = useState('')
  const [destinationAccountId, setDestinationAccountId] = useState('')
  const [amount, setAmount] = useState('')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  function reset() {
    setSourceAccountId('')
    setDestinationAccountId('')
    setAmount('')
    setDate(new Date().toISOString().slice(0, 10))
    setDescription('')
    setError('')
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (sourceAccountId === destinationAccountId) {
      setError('Selecione contas de origem e destino diferentes.')
      return
    }
    setSaving(true)
    setError('')
    try {
      await createTransfer(token, {
        sourceAccountId,
        destinationAccountId,
        amount: Number(amount),
        date,
        description: description || undefined,
      })
      reset()
      setOpen(false)
      onChanged()
    } catch {
      setError('Não foi possível realizar a transferência. Confira o saldo e as contas.')
    } finally {
      setSaving(false)
    }
  }

  async function remove(transfer: Transfer) {
    if (!window.confirm(`Estornar a transferência de ${formatCurrency(transfer.amount)}?`)) return
    try {
      await deleteTransfer(token, transfer.id)
      onChanged()
    } catch {
      setError('Não foi possível estornar a transferência.')
    }
  }

  return (
    <section className="card transfers-card" id="transfers">
      <div className="card-heading">
        <div><p className="eyebrow">Movimentação interna</p><h2>Transferências</h2></div>
        <button className="primary-small-button" onClick={() => { if (open) reset(); setOpen((current) => !current) }}>
          {open ? 'Cancelar' : 'Nova transferência'}
        </button>
      </div>
      {open && <form className="transfer-form" onSubmit={submit}>
        <label>Conta de origem<select required value={sourceAccountId} onChange={(event) => setSourceAccountId(event.target.value)}><option value="">Selecione</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}</select></label>
        <label>Conta de destino<select required value={destinationAccountId} onChange={(event) => setDestinationAccountId(event.target.value)}><option value="">Selecione</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}</select></label>
        <label>Valor<input required min="0.01" step="0.01" type="number" value={amount} onChange={(event) => setAmount(event.target.value)} /></label>
        <label>Data<input required type="date" value={date} onChange={(event) => setDate(event.target.value)} /></label>
        <label>Descrição<input value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Opcional" /></label>
        {error && <div className="alert error">{error}</div>}
        <button className="primary-button" disabled={saving}>{saving ? 'Transferindo...' : 'Confirmar transferência'}</button>
      </form>}
      {transfers.length === 0 ? <EmptyState text="Nenhuma transferência registrada." /> : <div className="transfer-list">{transfers.map((transfer) => <div className="transfer-row" key={transfer.id}><div className="transfer-description"><strong>{transfer.sourceAccountName} → {transfer.destinationAccountName}</strong><span>{transfer.description || 'Transferência interna'} · {formatDate(transfer.date)}</span></div><strong>{formatCurrency(transfer.amount)}</strong><button className="delete-button" onClick={() => remove(transfer)}>Estornar</button></div>)}</div>}
    </section>
  )
}

function TransactionsSection({ accounts, categories, creditCards, transactions, token, onCreated, onChanged }: {
  accounts: Account[]
  categories: Category[]
  creditCards: CreditCard[]
  transactions: Transaction[]
  token: string
  onCreated: () => void
  onChanged: () => void
}) {
  const [open, setOpen] = useState(false)
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [type, setType] = useState<TransactionType>('EXPENSE')
  const [status, setStatus] = useState<'PAID' | 'PENDING'>('PAID')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [categoryId, setCategoryId] = useState('')
  const [accountId, setAccountId] = useState('')
  const [creditCardId, setCreditCardId] = useState('')
  const [installments, setInstallments] = useState('1')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [filter, setFilter] = useState<'ALL' | TransactionType>('ALL')
  const [filterStatus, setFilterStatus] = useState<'ALL' | 'PAID' | 'PENDING'>('ALL')
  const [filterCategoryId, setFilterCategoryId] = useState('ALL')
  const [filterAccountId, setFilterAccountId] = useState('ALL')
  const [filterCreditCardId, setFilterCreditCardId] = useState('ALL')
  const [filterDescription, setFilterDescription] = useState('')
  const availableCategories = categories.filter((category) => category.type === type)
  const filteredTransactions = transactions.filter((transaction) => {
    const typeMatches = filter === 'ALL' || transaction.type === filter
    const statusMatches = filterStatus === 'ALL' || transaction.status === filterStatus
    const categoryMatches = filterCategoryId === 'ALL' || transaction.categoryId === filterCategoryId
    const accountMatches = filterAccountId === 'ALL' || transaction.accountId === filterAccountId
    const selectedCard = creditCards.find((card) => card.id === filterCreditCardId)
    const cardMatches = filterCreditCardId === 'ALL' || transaction.creditCardName === selectedCard?.name
    const descriptionMatches = !filterDescription.trim() || transaction.description.toLowerCase().includes(filterDescription.trim().toLowerCase())
    return typeMatches && statusMatches && categoryMatches && accountMatches && cardMatches && descriptionMatches
  })
  function resetForm() {
    setDescription('')
    setAmount('')
    setType('EXPENSE')
    setStatus('PAID')
    setDate(new Date().toISOString().slice(0, 10))
    setCategoryId('')
    setAccountId('')
    setCreditCardId('')
    setInstallments('1')
    setEditingId(null)
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      if ((accountId && creditCardId) || (!accountId && !creditCardId)) {
        setError('Selecione uma conta ou um cartão de crédito.')
        setSaving(false)
        return
      }
      const payload = {
        description,
        amount: Number(amount),
        type,
        status,
        date,
        categoryId,
        accountId,
        ...(creditCardId ? { creditCardId } : {}),
        totalInstallments: Number(installments),
      }
      if (editingId) {
        await updateTransaction(token, editingId, payload)
      } else {
        await createTransaction(token, payload)
      }
      resetForm()
      setOpen(false)
      if (editingId) onChanged()
      else onCreated()
    } catch (transactionError) {
      setError(transactionError instanceof Error
        ? transactionError.message
        : editingId
          ? 'Não foi possível editar a movimentação. Confira conta e categoria.'
          : 'Não foi possível cadastrar a movimentação. Confira conta e categoria.')
    } finally {
      setSaving(false)
    }

  }

  async function remove(transaction: Transaction) {
    if (!window.confirm(`Excluir "${transaction.description}"?`)) return
    try {
      await deleteTransaction(token, transaction.id)
      onChanged()
    } catch {
      setError('Não foi possível excluir a movimentação.')
    }
  }

  function edit(transaction: Transaction) {
    if (!transaction.accountId || !transaction.categoryId) {
      setError('Transações de cartão não podem ser editadas por esta tela.')
      return
    }
    setEditingId(transaction.id)
    setDescription(transaction.description)
    setAmount(String(transaction.amount))
    setType(transaction.type)
    setStatus(transaction.status)
    setDate(transaction.date)
    setCategoryId(transaction.categoryId)
    setAccountId(transaction.accountId)
    setCreditCardId('')
    setInstallments('1')
    setError('')
    setOpen(true)
  }

  return (
    <section className="card transaction-form-card">
      <div className="card-heading">
        <div><p className="eyebrow">Movimentações</p><h2>Receitas e despesas</h2></div>
        <button className="primary-small-button" onClick={() => { if (open) resetForm(); setOpen((current) => !current) }}>{open ? 'Cancelar' : 'Nova movimentação'}</button>
      </div>
      {open && <form className="transaction-form" onSubmit={submit}>
        <label>Descrição<input required value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Ex.: Supermercado" /></label>
        <label>Valor<input required min="0.01" step="0.01" type="number" value={amount} onChange={(event) => setAmount(event.target.value)} /></label>
        <label>Tipo<select value={type} onChange={(event) => { const nextType = event.target.value as TransactionType; setType(nextType); setCategoryId(''); if (nextType === 'INCOME') setCreditCardId('') }}><option value="EXPENSE">Despesa</option><option value="INCOME">Receita</option></select></label>
        <label>Status<select value={status} onChange={(event) => setStatus(event.target.value as 'PAID' | 'PENDING')}><option value="PAID">Pago</option><option value="PENDING">Pendente</option></select></label>
        <label>Data<input required type="date" value={date} onChange={(event) => setDate(event.target.value)} /></label>
        <label>Categoria<select required value={categoryId} onChange={(event) => setCategoryId(event.target.value)}><option value="">Selecione</option>{availableCategories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>
        <label>Conta<select value={accountId} onChange={(event) => { setAccountId(event.target.value); setCreditCardId('') }}><option value="">Nenhuma</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}</select></label>
        {type === 'EXPENSE' && <label>Cartão<select value={creditCardId} onChange={(event) => { setCreditCardId(event.target.value); setAccountId('') }}><option value="">Nenhum</option>{creditCards.map((card) => <option key={card.id} value={card.id}>{card.name}</option>)}</select></label>}
        <label>Parcelas<input disabled={Boolean(editingId)} min="1" max="60" type="number" value={installments} onChange={(event) => setInstallments(event.target.value)} /></label>
        {error && <div className="alert error">{error}</div>}
        <button className="primary-button" disabled={saving || accounts.length === 0 || availableCategories.length === 0}>{saving ? 'Salvando...' : editingId ? 'Salvar alterações' : 'Salvar movimentação'}</button>
      </form>}
      <p className="transaction-help">{accounts.length === 0 ? 'Cadastre uma conta antes de adicionar movimentações.' : availableCategories.length === 0 ? 'Cadastre uma categoria do tipo selecionado.' : 'Registre suas movimentações para acompanhar o saldo no dashboard.'}</p>
      <div className="transaction-filter">
        <strong>Histórico</strong>
        <div className="transaction-filter-controls">
          <input value={filterDescription} onChange={(event) => setFilterDescription(event.target.value)} placeholder="Buscar descrição" />
          <select value={filter} onChange={(event) => setFilter(event.target.value as 'ALL' | TransactionType)}>
            <option value="ALL">Todos os tipos</option><option value="INCOME">Receitas</option><option value="EXPENSE">Despesas</option>
          </select>
          <select value={filterStatus} onChange={(event) => setFilterStatus(event.target.value as 'ALL' | 'PAID' | 'PENDING')}>
            <option value="ALL">Todos os status</option><option value="PAID">Pagas</option><option value="PENDING">Pendentes</option>
          </select>
          <select value={filterCategoryId} onChange={(event) => setFilterCategoryId(event.target.value)}>
            <option value="ALL">Todas as categorias</option>
            {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
          </select>
          <select value={filterAccountId} onChange={(event) => setFilterAccountId(event.target.value)}>
            <option value="ALL">Todas as contas</option>
            {accounts.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}
          </select>
          <select value={filterCreditCardId} onChange={(event) => setFilterCreditCardId(event.target.value)}>
            <option value="ALL">Todos os cartões</option>
            {creditCards.map((card) => <option key={card.id} value={card.id}>{card.name}</option>)}
          </select>
        </div>
      </div>
      <div className="transaction-history">
        {filteredTransactions.slice(0, 20).map((transaction) => (
          <div className="transaction-row" key={transaction.id}>
            <div className={`transaction-icon ${transaction.type === 'INCOME' ? 'income' : 'expense'}`}>{transaction.type === 'INCOME' ? '+' : '−'}</div>
            <div className="transaction-description"><strong>{transaction.description}</strong><span>{transaction.categoryName ?? 'Sem categoria'} · {formatDate(transaction.date)}</span></div>
            <strong className={transaction.type === 'INCOME' ? 'positive' : 'negative'}>{formatCurrency(transaction.amount)}</strong>
            <button className="edit-button" title={transaction.creditCardName ? 'Compras no cartão não podem ser editadas' : 'Editar movimentação'} onClick={() => edit(transaction)}>Editar</button>
            <button className="delete-button" title="Excluir movimentação" onClick={() => remove(transaction)}>Excluir</button>
          </div>
        ))}
        {transactions.length === 0
          ? <EmptyState text="Nenhuma movimentação cadastrada no período." />
          : filteredTransactions.length === 0 && <EmptyState text="Nenhuma movimentação corresponde aos filtros." />}
      </div>
    </section>
  )
}

function CreditCardsSection({ cards, accounts, token, onCreated }: {
  cards: CreditCard[]
  accounts: Account[]
  token: string
  onCreated: (card: CreditCard) => void
}) {
  const [open, setOpen] = useState(false)
  const [name, setName] = useState('')
  const [creditLimit, setCreditLimit] = useState('')
  const [closingDay, setClosingDay] = useState('10')
  const [dueDay, setDueDay] = useState('15')
  const [accountId, setAccountId] = useState('')
  const [color, setColor] = useState('#4fd1c5')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      const card = await createCreditCard(token, {
        name,
        creditLimit: Number(creditLimit),
        closingDay: Number(closingDay),
        dueDay: Number(dueDay),
        accountId,
        color,
      })
      onCreated(card)
      setName('')
      setCreditLimit('')
      setOpen(false)
    } catch {
      setError('Não foi possível cadastrar o cartão. Confira os dados informados.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="card cards-card" id="cards">
      <div className="card-heading">
        <div><p className="eyebrow">Crédito</p><h2>Cartões</h2></div>
        <button className="primary-small-button" onClick={() => setOpen((current) => !current)}>{open ? 'Cancelar' : 'Novo cartão'}</button>
      </div>
      {open && <form className="card-form" onSubmit={submit}>
        <label>Nome<input required value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex.: Nubank" /></label>
        <label>Limite<input required min="0.01" step="0.01" type="number" value={creditLimit} onChange={(event) => setCreditLimit(event.target.value)} /></label>
        <label>Fechamento<input required min="1" max="31" type="number" value={closingDay} onChange={(event) => setClosingDay(event.target.value)} /></label>
        <label>Vencimento<input required min="1" max="31" type="number" value={dueDay} onChange={(event) => setDueDay(event.target.value)} /></label>
        <label>Conta para pagamento<select value={accountId} onChange={(event) => setAccountId(event.target.value)}><option value="">Nenhuma</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}</select></label>
        <label>Cor<input type="color" value={color} onChange={(event) => setColor(event.target.value)} /></label>
        {error && <div className="alert error">{error}</div>}
        <button className="primary-button" disabled={saving}>{saving ? 'Salvando...' : 'Salvar cartão'}</button>
      </form>}
      {cards.length === 0 ? <EmptyState text="Nenhum cartão cadastrado." /> : <div className="card-list">
        {cards.map((card) => {
          const usage = card.creditLimit > 0 ? ((card.creditLimit - card.availableLimit) / card.creditLimit) * 100 : 0
          return <article className="credit-card-item" key={card.id} style={{ borderTopColor: card.color ?? '#4fd1c5' }}>
            <div className="credit-card-heading"><strong>{card.name}</strong><span>Fecha dia {card.closingDay} · vence dia {card.dueDay}</span></div>
            <div className="credit-card-values"><span>Disponível <strong>{formatCurrency(card.availableLimit)}</strong></span><span>Limite <strong>{formatCurrency(card.creditLimit)}</strong></span></div>
            <div className="limit-bar"><span style={{ width: `${Math.min(usage, 100)}%`, background: card.color ?? '#4fd1c5' }} /></div>
            <small>{card.accountName ? `Pagamento pela conta ${card.accountName}` : 'Sem conta de pagamento definida'}</small>
          </article>
        })}
      </div>}
    </section>
  )
}

function InvoicesSection({ invoices, accounts, token, onChanged }: {
  invoices: Invoice[]
  accounts: Account[]
  token: string
  onChanged: () => void
}) {
  const [cardFilter, setCardFilter] = useState('ALL')
  const [paymentInvoiceId, setPaymentInvoiceId] = useState<string | null>(null)
  const [accountId, setAccountId] = useState('')
  const [amount, setAmount] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const visibleInvoices = invoices.filter((invoice) => cardFilter === 'ALL' || invoice.creditCardId === cardFilter)
  const monthName = (month: number) => new Intl.DateTimeFormat('pt-BR', { month: 'long' }).format(new Date(2024, month - 1, 1))
  const statusLabel: Record<Invoice['status'], string> = { OPEN: 'Aberta', CLOSED: 'Fechada', PAID: 'Paga' }

  function startPayment(invoice: Invoice) {
    setPaymentInvoiceId(invoice.id)
    setAccountId(accounts[0]?.id ?? '')
    setAmount('')
    setError('')
  }

  async function submitPayment(event: FormEvent) {
    event.preventDefault()
    if (!paymentInvoiceId || !accountId) return
    setSaving(true)
    setError('')
    try {
      await payInvoice(token, paymentInvoiceId, {
        accountId,
        ...(amount ? { amount: Number(amount) } : {}),
      })
      setPaymentInvoiceId(null)
      onChanged()
    } catch (paymentError) {
      setError(paymentError instanceof Error ? paymentError.message : 'Não foi possível pagar a fatura.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="card invoices-card" id="invoices">
      <div className="card-heading">
        <div><p className="eyebrow">Cartões</p><h2>Faturas</h2></div>
        {invoices.length > 0 && <select className="invoice-filter" value={cardFilter} onChange={(event) => setCardFilter(event.target.value)}>
          <option value="ALL">Todos os cartões</option>
          {Array.from(new Map(invoices.map((invoice) => [invoice.creditCardId, invoice.creditCardName]))).map(([id, name]) => <option key={id} value={id}>{name}</option>)}
        </select>}
      </div>
      {visibleInvoices.length === 0 ? <EmptyState text="Nenhuma fatura gerada ainda." /> : <div className="invoice-list">
        {visibleInvoices.map((invoice) => <article className="invoice-row" key={invoice.id}>
          <div className="invoice-main"><strong>{invoice.creditCardName}</strong><span>{monthName(invoice.month)} de {invoice.year} · vence em {formatDate(invoice.dueDate)}</span></div>
          <div className="invoice-amount"><strong>{formatCurrency(invoice.remainingAmount)}</strong><span>de {formatCurrency(invoice.totalAmount)}</span></div>
          <span className={`invoice-status ${invoice.status.toLowerCase()}`}>{statusLabel[invoice.status]}</span>
          {invoice.status !== 'PAID' && invoice.remainingAmount > 0 && <button className="primary-small-button" onClick={() => startPayment(invoice)}>Pagar</button>}
          {paymentInvoiceId === invoice.id && <form className="invoice-payment-form" onSubmit={submitPayment}>
            <label>Conta<select required value={accountId} onChange={(event) => setAccountId(event.target.value)}><option value="">Selecione</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}</select></label>
            <label>Valor parcial <input type="number" min="0.01" max={invoice.remainingAmount} step="0.01" value={amount} onChange={(event) => setAmount(event.target.value)} placeholder={`Total: ${invoice.remainingAmount.toFixed(2)}`} /></label>
            <button className="primary-small-button" disabled={saving}>{saving ? 'Pagando...' : 'Confirmar'}</button>
            <button type="button" className="text-button" onClick={() => setPaymentInvoiceId(null)}>Cancelar</button>
            {error && <div className="alert error">{error}</div>}
          </form>}
        </article>)}
      </div>}
    </section>
  )
}

function RecurringTransactionsSection({ recurringTransactions, accounts, categories, creditCards, token, onChanged }: {
  recurringTransactions: RecurringTransaction[]
  accounts: Account[]
  categories: Category[]
  creditCards: CreditCard[]
  token: string
  onChanged: () => void
}) {
  const [open, setOpen] = useState(false)
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [type, setType] = useState<TransactionType>('EXPENSE')
  const [frequency, setFrequency] = useState<RecurringFrequency>('MONTHLY')
  const [startDate, setStartDate] = useState(new Date().toISOString().slice(0, 10))
  const [endDate, setEndDate] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [accountId, setAccountId] = useState('')
  const [creditCardId, setCreditCardId] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const availableCategories = categories.filter((category) => category.type === type)

  function reset() {
    setDescription('')
    setAmount('')
    setType('EXPENSE')
    setFrequency('MONTHLY')
    setStartDate(new Date().toISOString().slice(0, 10))
    setEndDate('')
    setCategoryId('')
    setAccountId('')
    setCreditCardId('')
    setError('')
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    if ((accountId && creditCardId) || (!accountId && !creditCardId)) {
      setError('Selecione uma conta ou um cartão de crédito.')
      return
    }
    setSaving(true)
    setError('')
    try {
      await createRecurringTransaction(token, {
        description,
        amount: Number(amount),
        type,
        frequency,
        startDate,
        ...(endDate ? { endDate } : {}),
        categoryId,
        ...(accountId ? { accountId } : {}),
        ...(creditCardId ? { creditCardId } : {}),
      })
      reset()
      setOpen(false)
      onChanged()
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Não foi possível criar a recorrência.')
    } finally {
      setSaving(false)
    }
  }

  async function toggle(item: RecurringTransaction) {
    try {
      await toggleRecurringTransaction(token, item.id)
      onChanged()
    } catch {
      setError('Não foi possível alterar o status da recorrência.')
    }
  }

  async function remove(item: RecurringTransaction) {
    if (!window.confirm(`Excluir a recorrência "${item.description}"?`)) return
    try {
      await deleteRecurringTransaction(token, item.id)
      onChanged()
    } catch {
      setError('Não foi possível excluir a recorrência.')
    }
  }

  const frequencyLabel: Record<RecurringFrequency, string> = {
    WEEKLY: 'Semanal',
    MONTHLY: 'Mensal',
    YEARLY: 'Anual',
  }

  return (
    <section className="card recurring-card" id="recurring">
      <div className="card-heading">
        <div><p className="eyebrow">Automação</p><h2>Transações recorrentes</h2></div>
        <button className="primary-small-button" onClick={() => { if (open) reset(); setOpen((current) => !current) }}>
          {open ? 'Cancelar' : 'Nova recorrência'}
        </button>
      </div>
      {open && <form className="recurring-form" onSubmit={submit}>
        <label>Descrição<input required value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Ex.: Aluguel" /></label>
        <label>Valor<input required min="0.01" step="0.01" type="number" value={amount} onChange={(event) => setAmount(event.target.value)} /></label>
        <label>Tipo<select value={type} onChange={(event) => { setType(event.target.value as TransactionType); setCategoryId(''); setCreditCardId('') }}><option value="EXPENSE">Despesa</option><option value="INCOME">Receita</option></select></label>
        <label>Frequência<select value={frequency} onChange={(event) => setFrequency(event.target.value as RecurringFrequency)}><option value="WEEKLY">Semanal</option><option value="MONTHLY">Mensal</option><option value="YEARLY">Anual</option></select></label>
        <label>Início<input required type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} /></label>
        <label>Fim (opcional)<input type="date" min={startDate} value={endDate} onChange={(event) => setEndDate(event.target.value)} /></label>
        <label>Categoria<select required value={categoryId} onChange={(event) => setCategoryId(event.target.value)}><option value="">Selecione</option>{availableCategories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>
        <label>Conta<select value={accountId} onChange={(event) => { setAccountId(event.target.value); setCreditCardId('') }}><option value="">Nenhuma</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}</select></label>
        {type === 'EXPENSE' && <label>Cartão<select value={creditCardId} onChange={(event) => { setCreditCardId(event.target.value); setAccountId('') }}><option value="">Nenhum</option>{creditCards.map((card) => <option key={card.id} value={card.id}>{card.name}</option>)}</select></label>}
        {error && <div className="alert error">{error}</div>}
        <button className="primary-button" disabled={saving}>{saving ? 'Salvando...' : 'Salvar recorrência'}</button>
      </form>}
      {recurringTransactions.length === 0 ? <EmptyState text="Nenhuma transação recorrente cadastrada." /> : <div className="recurring-list">
        {recurringTransactions.map((item) => <article className="recurring-row" key={item.id}>
          <div className="recurring-description"><strong>{item.description}</strong><span>{frequencyLabel[item.frequency]} · próxima em {formatDate(item.nextDate)}</span></div>
          <strong>{formatCurrency(item.amount)}</strong>
          <span className={`recurring-status ${item.active ? 'active' : 'paused'}`}>{item.active ? 'Ativa' : 'Pausada'}</span>
          <button className="text-button" onClick={() => toggle(item)}>{item.active ? 'Pausar' : 'Reativar'}</button>
          <button className="delete-button" onClick={() => remove(item)}>Excluir</button>
        </article>)}
      </div>}
    </section>
  )
}

function CategoriesSection({ categories, token, onCategoryCreated }: {
  categories: Category[]
  token: string
  onCategoryCreated: (category: Category) => void
}) {
  const [open, setOpen] = useState(false)
  const [name, setName] = useState('')
  const [type, setType] = useState<TransactionType>('EXPENSE')
  const [icon, setIcon] = useState('●')
  const [color, setColor] = useState('#4fd1c5')
  const [parentId, setParentId] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const parents = categories.filter((category) => !category.parentId && category.type === type)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      const category = await createCategory(token, {
        name,
        type,
        icon,
        color,
        parentId: parentId || null,
      })
      onCategoryCreated(category)
      setName('')
      setParentId('')
      setOpen(false)
    } catch {
      setError('Não foi possível cadastrar a categoria.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="card categories-card" id="categories">
      <div className="card-heading">
        <div><p className="eyebrow">Organização</p><h2>Minhas categorias</h2></div>
        <button className="primary-small-button" onClick={() => setOpen((current) => !current)}>
          {open ? 'Cancelar' : 'Nova categoria'}
        </button>
      </div>
      {open && <form className="category-form" onSubmit={submit}>
        <label>Nome<input required value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex.: Alimentação" /></label>
        <label>Tipo<select value={type} onChange={(event) => { setType(event.target.value as TransactionType); setParentId('') }}><option value="EXPENSE">Despesa</option><option value="INCOME">Receita</option></select></label>
        <label>Ícone<input value={icon} maxLength={3} onChange={(event) => setIcon(event.target.value)} /></label>
        <label>Cor<input type="color" value={color} onChange={(event) => setColor(event.target.value)} /></label>
        <label>Categoria pai<select value={parentId} onChange={(event) => setParentId(event.target.value)}><option value="">Categoria principal</option>{parents.map((parent) => <option key={parent.id} value={parent.id}>{parent.name}</option>)}</select></label>
        {error && <div className="alert error">{error}</div>}
        <button className="primary-button" disabled={saving}>{saving ? 'Salvando...' : 'Salvar categoria'}</button>
      </form>}
      {categories.length === 0 ? <EmptyState text="Você ainda não cadastrou nenhuma categoria." /> : <div className="category-items">{categories.map((category) => <div className="category-item" key={category.id}><span className="category-icon" style={{ background: category.color ?? '#4fd1c5' }}>{category.icon ?? '●'}</span><div><strong>{category.name}</strong><small>{category.parentName ? `${category.parentName} · ` : ''}{category.type === 'EXPENSE' ? 'Despesa' : 'Receita'}</small></div></div>)}</div>}
    </section>
  )
}

function AccountsSection({ accounts, token, onAccountCreated }: {
  accounts: Account[]
  token: string
  onAccountCreated: (account: Account) => void
}) {
  const [open, setOpen] = useState(false)
  const [name, setName] = useState('')
  const [type, setType] = useState('CORRENTE')
  const [initialBalance, setInitialBalance] = useState('0')
  const [color, setColor] = useState('#4fd1c5')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      const account = await createAccount(token, {
        name,
        type,
        initialBalance: Number(initialBalance),
        color,
      })
      onAccountCreated(account)
      setName('')
      setInitialBalance('0')
      setOpen(false)
    } catch {
      setError('Não foi possível cadastrar a conta.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="card accounts-card" id="accounts">
      <div className="card-heading">
        <div><p className="eyebrow">Patrimônio</p><h2>Minhas contas</h2></div>
        <button className="primary-small-button" onClick={() => setOpen((current) => !current)}>
          {open ? 'Cancelar' : 'Nova conta'}
        </button>
      </div>
      {open && <form className="account-form" onSubmit={submit}>
        <label>Nome<input required value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex.: Conta principal" /></label>
        <label>Tipo<select value={type} onChange={(event) => setType(event.target.value)}><option value="CORRENTE">Conta corrente</option><option value="POUPANCA">Poupança</option><option value="DIGITAL">Conta digital</option><option value="CARTEIRA">Carteira</option></select></label>
        <label>Saldo inicial<input required type="number" step="0.01" value={initialBalance} onChange={(event) => setInitialBalance(event.target.value)} /></label>
        <label>Cor<input type="color" value={color} onChange={(event) => setColor(event.target.value)} /></label>
        {error && <div className="alert error">{error}</div>}
        <button className="primary-button" disabled={saving}>{saving ? 'Salvando...' : 'Salvar conta'}</button>
      </form>}
      {accounts.length === 0 ? <EmptyState text="Você ainda não cadastrou nenhuma conta." /> : <div className="account-list">{accounts.map((account) => <div className="account-row" key={account.id}><span className="account-color" style={{ background: account.color ?? '#4fd1c5' }} /><div className="account-description"><strong>{account.name}</strong><span>{account.type}</span></div><strong>{formatCurrency(account.currentBalance)}</strong></div>)}</div>}
    </section>
  )
}

function MetricCard({ label, value, tone }: { label: string; value: string; tone: string }) {
  return <article className={`metric-card ${tone}`}><span>{label}</span><strong>{value}</strong><small>no período selecionado</small></article>
}

function CategoryBars({ categories }: { categories: DashboardSummary['expensesByCategory'] }) {
  const maximum = Math.max(...categories.map((category) => category.amount))
  return <div className="category-list">{categories.slice(0, 6).map((category) => <div className="category-row" key={category.categoryName}><div className="category-label"><span>{category.categoryName}</span><strong>{formatCurrency(category.amount)}</strong></div><div className="bar-track"><div className="bar-fill" style={{ width: `${(category.amount / maximum) * 100}%` }} /></div></div>)}</div>
}

function TransactionList({ transactions }: { transactions: Transaction[] }) {
  return <div className="transaction-list">{transactions.map((transaction) => <div className="transaction-row" key={transaction.id}><div className={`transaction-icon ${transaction.type === 'INCOME' ? 'income' : 'expense'}`}>{transaction.type === 'INCOME' ? '+' : '−'}</div><div className="transaction-description"><strong>{transaction.description}</strong><span>{transaction.categoryName ?? 'Sem categoria'} · {formatDate(transaction.date)}</span></div><strong className={transaction.type === 'INCOME' ? 'positive' : 'negative'}>{transaction.type === 'INCOME' ? '+' : '-'} {formatCurrency(transaction.amount)}</strong></div>)}</div>
}

function EmptyState({ text }: { text: string }) {
  return <div className="empty-state">{text}</div>
}

export default App
