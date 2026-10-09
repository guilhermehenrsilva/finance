import { FormEvent, useEffect, useMemo, useState } from 'react'
import { createAccount, createCategory, createTransaction, getAccounts, getCategories, getDashboard, login, register } from './services/api'
import type { Account, Category, DashboardSummary, Transaction, TransactionType } from './types'

const TOKEN_KEY = 'finance.auth.token'

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
  const range = useMemo(currentMonthRange, [])
  const [startDate, setStartDate] = useState(range.start)
  const [endDate, setEndDate] = useState(range.end)
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [accounts, setAccounts] = useState<Account[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [refresh, setRefresh] = useState(0)

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
  }, [token, startDate, endDate, refresh])

  function handleLogin(newToken: string) {
    localStorage.setItem(TOKEN_KEY, newToken)
    setToken(newToken)
    setError('')
  }

  if (!token) {
    return <LoginScreen onLogin={handleLogin} />
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark">$</span><span>Finance</span></div>
        <nav>
          <a className="nav-item active" href="#dashboard">Visão geral</a>
          <a className="nav-item" href="#transactions">Movimentações</a>
          <a className="nav-item" href="#accounts">Contas</a>
          <a className="nav-item" href="#cards">Cartões</a>
        </nav>
        <button className="logout-button" onClick={() => {
          localStorage.removeItem(TOKEN_KEY)
          setToken(null)
        }}>Sair da conta</button>
      </aside>

      <main className="content">
        <header className="page-header">
          <div>
            <p className="eyebrow">Visão geral</p>
            <h1>Seu dinheiro, sob controle.</h1>
          </div>
          <div className="period-selector">
            <label>De <input type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} /></label>
            <label>Até <input type="date" value={endDate} onChange={(event) => setEndDate(event.target.value)} /></label>
          </div>
        </header>

        {error && <div className="alert error">{error}</div>}
        {loading && <div className="loading">Atualizando seus dados...</div>}
        {summary && <Dashboard summary={summary} accounts={accounts} categories={categories} token={token} onAccountCreated={(account) => setAccounts((current) => [...current, account])} onCategoryCreated={(category) => setCategories((current) => [...current, category])} onTransactionCreated={() => setRefresh((current) => current + 1)} />}
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

function Dashboard({ summary, accounts, categories, token, onAccountCreated, onCategoryCreated, onTransactionCreated }: {
  summary: DashboardSummary
  accounts: Account[]
  categories: Category[]
  token: string
  onAccountCreated: (account: Account) => void
  onCategoryCreated: (category: Category) => void
  onTransactionCreated: () => void
}) {
  return (
    <div className="dashboard">
      <section className="metrics-grid">
        <MetricCard label="Saldo total" value={formatCurrency(summary.totalBalance)} tone="blue" />
        <MetricCard label="Receitas no período" value={formatCurrency(summary.periodIncome)} tone="green" />
        <MetricCard label="Despesas no período" value={formatCurrency(summary.periodExpense)} tone="red" />
        <MetricCard label="Resultado" value={formatCurrency(summary.periodResult)} tone={summary.periodResult >= 0 ? 'green' : 'red'} />
      </section>
      <section className="dashboard-grid">
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
      </section>
      <section className="card transactions-card" id="transactions">
        <div className="card-heading"><div><p className="eyebrow">Atividade</p><h2>Últimas movimentações</h2></div><button className="text-button">Ver todas</button></div>
        {summary.recentTransactions.length === 0 ? <EmptyState text="Nenhuma movimentação encontrada." /> : <TransactionList transactions={summary.recentTransactions} />}
      </section>
      <AccountsSection accounts={accounts} token={token} onAccountCreated={onAccountCreated} />
      <CategoriesSection categories={categories} token={token} onCategoryCreated={onCategoryCreated} />
      <TransactionsSection accounts={accounts} categories={categories} token={token} onCreated={onTransactionCreated} />
    </div>
  )
}

function TransactionsSection({ accounts, categories, token, onCreated }: {
  accounts: Account[]
  categories: Category[]
  token: string
  onCreated: () => void
}) {
  const [open, setOpen] = useState(false)
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [type, setType] = useState<TransactionType>('EXPENSE')
  const [status, setStatus] = useState<'PAID' | 'PENDING'>('PAID')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [categoryId, setCategoryId] = useState('')
  const [accountId, setAccountId] = useState('')
  const [installments, setInstallments] = useState('1')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const availableCategories = categories.filter((category) => category.type === type)

  async function submit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      await createTransaction(token, {
        description,
        amount: Number(amount),
        type,
        status,
        date,
        categoryId,
        accountId,
        totalInstallments: Number(installments),
      })
      setDescription('')
      setAmount('')
      setOpen(false)
      onCreated()
    } catch {
      setError('Não foi possível cadastrar a movimentação. Confira conta e categoria.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="card transaction-form-card">
      <div className="card-heading">
        <div><p className="eyebrow">Movimentações</p><h2>Receitas e despesas</h2></div>
        <button className="primary-small-button" onClick={() => setOpen((current) => !current)}>{open ? 'Cancelar' : 'Nova movimentação'}</button>
      </div>
      {open && <form className="transaction-form" onSubmit={submit}>
        <label>Descrição<input required value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Ex.: Supermercado" /></label>
        <label>Valor<input required min="0.01" step="0.01" type="number" value={amount} onChange={(event) => setAmount(event.target.value)} /></label>
        <label>Tipo<select value={type} onChange={(event) => { setType(event.target.value as TransactionType); setCategoryId('') }}><option value="EXPENSE">Despesa</option><option value="INCOME">Receita</option></select></label>
        <label>Status<select value={status} onChange={(event) => setStatus(event.target.value as 'PAID' | 'PENDING')}><option value="PAID">Pago</option><option value="PENDING">Pendente</option></select></label>
        <label>Data<input required type="date" value={date} onChange={(event) => setDate(event.target.value)} /></label>
        <label>Categoria<select required value={categoryId} onChange={(event) => setCategoryId(event.target.value)}><option value="">Selecione</option>{availableCategories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>
        <label>Conta<select required value={accountId} onChange={(event) => setAccountId(event.target.value)}><option value="">Selecione</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.name}</option>)}</select></label>
        <label>Parcelas<input min="1" max="60" type="number" value={installments} onChange={(event) => setInstallments(event.target.value)} /></label>
        {error && <div className="alert error">{error}</div>}
        <button className="primary-button" disabled={saving || accounts.length === 0 || availableCategories.length === 0}>{saving ? 'Salvando...' : 'Salvar movimentação'}</button>
      </form>}
      <p className="transaction-help">{accounts.length === 0 ? 'Cadastre uma conta antes de adicionar movimentações.' : availableCategories.length === 0 ? 'Cadastre uma categoria do tipo selecionado.' : 'Registre suas movimentações para acompanhar o saldo no dashboard.'}</p>
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
