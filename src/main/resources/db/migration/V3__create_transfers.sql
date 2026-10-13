CREATE TABLE transfers (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    source_account_id CHAR(36) NOT NULL,
    destination_account_id CHAR(36) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfers_users
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transfers_source_account
        FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_transfers_destination_account
        FOREIGN KEY (destination_account_id) REFERENCES accounts(id),
    CONSTRAINT chk_transfers_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transfers_distinct_accounts
        CHECK (source_account_id <> destination_account_id)
);

CREATE INDEX idx_transfers_user_date ON transfers(user_id, date);
CREATE INDEX idx_transfers_source_account ON transfers(source_account_id);
CREATE INDEX idx_transfers_destination_account ON transfers(destination_account_id);
