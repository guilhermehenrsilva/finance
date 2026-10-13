-- O primeiro schema criou apenas usuários, contas e categorias. As tabelas
-- abaixo são criadas nesta migration antes das tabelas que possuem FKs para
-- elas, permitindo que o Flyway inicialize um banco MySQL vazio.
CREATE TABLE credit_cards (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    account_id CHAR(36) NULL,
    name VARCHAR(100) NOT NULL,
    credit_limit DECIMAL(19, 2) NOT NULL,
    available_limit DECIMAL(19, 2) NOT NULL,
    closing_day INT NOT NULL,
    due_day INT NOT NULL,
    color VARCHAR(7),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_cards_users
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_cards_accounts
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

CREATE TABLE invoices (
    id CHAR(36) PRIMARY KEY,
    credit_card_id CHAR(36) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_invoices_card_period UNIQUE (credit_card_id, month, year),
    CONSTRAINT fk_invoices_credit_cards
        FOREIGN KEY (credit_card_id) REFERENCES credit_cards(id) ON DELETE CASCADE
);

CREATE TABLE transactions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    account_id CHAR(36) NULL,
    category_id CHAR(36) NULL,
    credit_card_id CHAR(36) NULL,
    invoice_id CHAR(36) NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    installment_group_id CHAR(36) NULL,
    installment_number INT NULL,
    total_installments INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_users
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_accounts
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_categories
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_credit_cards
        FOREIGN KEY (credit_card_id) REFERENCES credit_cards(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_invoices
        FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL
);

-- Criando a tabela de transações recorrentes
CREATE TABLE recurring_transactions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    category_id CHAR(36) NOT NULL,
    account_id CHAR(36) NULL,
    credit_card_id CHAR(36) NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    next_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_users
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recurring_categories
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_recurring_accounts
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_recurring_cards
        FOREIGN KEY (credit_card_id) REFERENCES credit_cards(id) ON DELETE SET NULL
);

CREATE INDEX idx_credit_cards_user ON credit_cards(user_id);
CREATE INDEX idx_invoices_card_period ON invoices(credit_card_id, year, month);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_invoice ON transactions(invoice_id);
CREATE INDEX idx_recurring_user_next_date
    ON recurring_transactions(user_id, active, next_date);