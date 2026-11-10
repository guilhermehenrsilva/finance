# Finance

Aplicação de gestão financeira pessoal com backend em Spring Boot e frontend em React + TypeScript.

## Funcionalidades

- Autenticação com JWT
- Contas e categorias
- Receitas, despesas e transferências
- Cartões de crédito, parcelas e faturas
- Transações recorrentes
- Orçamentos por categoria
- Metas financeiras
- Tags para movimentações
- Relatórios com exportação CSV e impressão em PDF
- Alertas financeiros
- Gerenciamento de perfil e senha

## Requisitos

Instale os seguintes programas antes de iniciar:

- Java 17
- MySQL 8
- Node.js 20 ou superior
- Git

O Maven não precisa ser instalado: o projeto inclui o Maven Wrapper.

## Baixar o projeto

```bash
git clone https://github.com/guilhermehenrsilva/finance.git
cd finance
```

## Configurar o banco de dados

Inicie o MySQL e crie o banco:

```sql
CREATE DATABASE finance_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Na raiz do projeto, crie um arquivo `.env`:

```properties
DB_URL=jdbc:mysql://localhost:3306/finance_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=sua_senha_do_mysql
JWT_SECRET=troque-esta-chave-por-uma-chave-segura-e-longa
```

O arquivo `.env` não deve ser commitado. As migrations do Flyway serão executadas automaticamente na inicialização do backend.

## Configurar o frontend

O frontend já possui a configuração padrão para acessar o backend local em `http://localhost:8080/api`.

Para usar outra URL, copie o arquivo de exemplo:

```bash
cd frontend
copy .env.example .env
```

No Linux/macOS, use:

```bash
cp .env.example .env
```

Edite `frontend/.env` quando necessário:

```properties
VITE_API_URL=http://localhost:8080/api
```

## Instalar dependências

Na raiz do projeto:

```bash
npm install
```

Depois instale as dependências do frontend:

```bash
cd frontend
npm install
cd ..
```

## Executar o sistema

### Opção 1: iniciar backend e frontend juntos

Na raiz do projeto:

```bash
npm run dev
```

Depois acesse:

- Aplicação: http://localhost:5173
- API: http://localhost:8080

### Opção 2: iniciar separadamente

Terminal 1 — backend:

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Terminal 2 — frontend:

```bash
cd frontend
npm run dev
```

## Criar uma conta

Abra http://localhost:5173 e selecione **Ainda não tenho uma conta**. Informe nome, e-mail, senha e moeda principal para criar o primeiro usuário.

## Comandos úteis

### Frontend

```bash
cd frontend
npm run build
npm run preview
```

### Backend

No Windows:

```powershell
.\mvnw.cmd clean test
```

No Linux/macOS:

```bash
./mvnw clean test
```

## Estrutura do projeto

```text
finance/
├── frontend/                 # Aplicação React + TypeScript
├── src/main/java/             # API Spring Boot
├── src/main/resources/       # Configurações e migrations Flyway
├── src/test/                 # Testes do backend
├── api-tests/                # Exemplos de requisições HTTP
├── pom.xml                   # Dependências e build do backend
└── package.json              # Comando para iniciar os dois projetos
```

## Solução de problemas

### Erro de conexão com o MySQL

Confirme se:

1. O serviço do MySQL está em execução.
2. O banco `finance_db` existe.
3. `DB_USERNAME` e `DB_PASSWORD` estão corretos no `.env`.
4. A porta configurada no `DB_URL` está correta.

### Erro de versão do Java

Confira a versão instalada:

```bash
java -version
```

O projeto requer Java 17. Uma versão diferente pode causar erro de incompatibilidade nas classes compiladas.

### Frontend não acessa a API

Verifique se o backend está em execução na porta 8080 e se `frontend/.env` contém:

```properties
VITE_API_URL=http://localhost:8080/api
```

Depois reinicie o servidor do Vite.

## Segurança

- Nunca publique o arquivo `.env`.
- Use uma chave JWT longa e exclusiva em cada ambiente.
- Não use a senha padrão do banco em produção.
- Para disponibilizar o sistema na internet, configure HTTPS e credenciais específicas para produção.

## Licença

Este projeto ainda não possui uma licença definida.
