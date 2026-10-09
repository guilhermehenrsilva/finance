import { FormEvent, useEffect, useMemo, useState } from 'react'
import { getDashboard, login, register } from './services/api'
import type { DashboardSummary, Transaction } from './types'

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
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

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
  }, [token, startDate, endDate])

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
        {summary && <Dashboard summary={summary} />}
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

function Dashboard({ summary }: { summary: DashboardSummary }) {
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
    </div>
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
