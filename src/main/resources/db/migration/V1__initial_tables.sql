CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE tb_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON tb_users(email);
CREATE INDEX idx_users_cpf ON tb_users(cpf);
CREATE INDEX idx_users_active ON tb_users(active);
CREATE INDEX idx_users_name ON tb_users(name);

COMMENT ON TABLE tb_users IS 'Users table for the digital wallet system';
COMMENT ON COLUMN tb_users.id IS 'Unique identifier for the user (BIGINT with IDENTITY generation)';
COMMENT ON COLUMN tb_users.name IS 'Full name of the user';
COMMENT ON COLUMN tb_users.email IS 'Email address of the user (unique)';
COMMENT ON COLUMN tb_users.cpf IS 'CPF (Brazilian tax ID) of the user (unique, 11 digits)';
COMMENT ON COLUMN tb_users.phone IS 'Phone number of the user (optional)';
COMMENT ON COLUMN tb_users.active IS 'Whether the user account is active';
COMMENT ON COLUMN tb_users.created_at IS 'Timestamp when the user was created';
COMMENT ON COLUMN tb_users.updated_at IS 'Timestamp when the user was last updated';

CREATE TABLE tb_wallets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_number BIGINT NOT NULL UNIQUE,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    user_id BIGINT NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_wallets_user_id FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);

CREATE INDEX idx_wallets_account_number ON tb_wallets(account_number);
CREATE INDEX idx_wallets_user_id ON tb_wallets(user_id);
CREATE INDEX idx_wallets_active ON tb_wallets(active);
CREATE INDEX idx_wallets_created_at ON tb_wallets(created_at);

COMMENT ON TABLE tb_wallets IS 'Digital wallets for users in the payment system';
COMMENT ON COLUMN tb_wallets.id IS 'Unique identifier for the wallet (UUID)';
COMMENT ON COLUMN tb_wallets.account_number IS 'Unique account number for transactions (BIGINT format)';
COMMENT ON COLUMN tb_wallets.balance IS 'Current balance in the wallet (cannot be negative)';
COMMENT ON COLUMN tb_wallets.user_id IS 'Foreign key reference to the wallet owner';
COMMENT ON COLUMN tb_wallets.active IS 'Whether the wallet is active for transactions';
COMMENT ON COLUMN tb_wallets.created_at IS 'Timestamp when the wallet was created';
COMMENT ON COLUMN tb_wallets.updated_at IS 'Timestamp when the wallet was last updated';

CREATE TABLE tb_transaction_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(20) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN', 'TRANSFER')),
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    source_wallet_id UUID,
    target_wallet_id UUID,
    description VARCHAR(500),
    balance_before_transaction DECIMAL(15,2) NOT NULL,
    balance_after_transaction DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transaction_source_wallet FOREIGN KEY (source_wallet_id) REFERENCES tb_wallets(id) ON DELETE SET NULL,
    CONSTRAINT fk_transaction_target_wallet FOREIGN KEY (target_wallet_id) REFERENCES tb_wallets(id) ON DELETE SET NULL,
    CONSTRAINT chk_transaction_wallets CHECK (
        (type = 'DEPOSIT' AND source_wallet_id IS NULL AND target_wallet_id IS NOT NULL) OR
        (type = 'WITHDRAWAL' AND source_wallet_id IS NOT NULL AND target_wallet_id IS NULL) OR
        (type IN ('TRANSFER_OUT', 'TRANSFER_IN', 'TRANSFER') AND source_wallet_id IS NOT NULL AND target_wallet_id IS NOT NULL)
    )
);

CREATE INDEX idx_transaction_type ON tb_transaction_history(type);
CREATE INDEX idx_transaction_source_wallet ON tb_transaction_history(source_wallet_id);
CREATE INDEX idx_transaction_target_wallet ON tb_transaction_history(target_wallet_id);
CREATE INDEX idx_transaction_status ON tb_transaction_history(status);
CREATE INDEX idx_transaction_created_at ON tb_transaction_history(created_at);
CREATE INDEX idx_transaction_amount ON tb_transaction_history(amount);

CREATE INDEX idx_transaction_wallet_date ON tb_transaction_history(source_wallet_id, created_at);
CREATE INDEX idx_transaction_wallet_type ON tb_transaction_history(source_wallet_id, type);

COMMENT ON TABLE tb_transaction_history IS 'Transaction history for all wallet operations';
COMMENT ON COLUMN tb_transaction_history.id IS 'Unique identifier for the transaction (UUID)';
COMMENT ON COLUMN tb_transaction_history.type IS 'Type of transaction: DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN, TRANSFER';
COMMENT ON COLUMN tb_transaction_history.amount IS 'Transaction amount (must be positive)';
COMMENT ON COLUMN tb_transaction_history.source_wallet_id IS 'Source wallet for the transaction (null for deposits)';
COMMENT ON COLUMN tb_transaction_history.target_wallet_id IS 'Target wallet for the transaction (null for withdrawals)';
COMMENT ON COLUMN tb_transaction_history.description IS 'Optional description of the transaction';
COMMENT ON COLUMN tb_transaction_history.balance_before_transaction IS 'Wallet balance before the transaction';
COMMENT ON COLUMN tb_transaction_history.balance_after_transaction IS 'Wallet balance after the transaction';
COMMENT ON COLUMN tb_transaction_history.status IS 'Transaction status: PENDING, COMPLETED, FAILED, CANCELLED';
COMMENT ON COLUMN tb_transaction_history.created_at IS 'Timestamp when the transaction was created';