-- Adicionando controle de parcelas nas transações existentes
ALTER TABLE transactions 
ADD COLUMN installment_group_id VARCHAR(255) NULL,
ADD COLUMN installment_number INT NULL,
ADD COLUMN total_installments INT NULL;

-- Criando a tabela de transações recorrentes
CREATE TABLE recurring_transactions (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    category_id VARCHAR(255) NOT NULL,
    account_id VARCHAR(255) NULL,
    credit_card_id VARCHAR(255) NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    frequency ENUM('MONTHLY', 'WEEKLY', 'YEARLY') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    next_date DATE NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_recurring_categories FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_recurring_accounts FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_recurring_cards FOREIGN KEY (credit_card_id) REFERENCES credit_cards(id)
);