CREATE TABLE tb_transaction_audit (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    operation_type VARCHAR(20) NOT NULL CHECK (operation_type IN ('CREDIT', 'DEBIT', 'TRANSFER_OUT', 'TRANSFER_IN', 'REFUND', 'ADJUSTMENT')),
    amount DECIMAL(19,2) NOT NULL CHECK (amount >= 0),
    balance_before DECIMAL(19,2) NOT NULL CHECK (balance_before >= 0),
    balance_after DECIMAL(19,2) NOT NULL CHECK (balance_after >= 0),
    transaction_status VARCHAR(20) NOT NULL CHECK (transaction_status IN ('INITIATED', 'COMPLETED', 'FAILED', 'ROLLED_BACK')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    origin_ip VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    correlation_id VARCHAR(100),
    description VARCHAR(500),
    metadata TEXT,
    
    CONSTRAINT fk_audit_wallet_id FOREIGN KEY (wallet_id) REFERENCES tb_wallets(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_user_id FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);

CREATE INDEX idx_transaction_audit_transaction_id ON tb_transaction_audit(transaction_id);
CREATE INDEX idx_transaction_audit_wallet_id ON tb_transaction_audit(wallet_id);
CREATE INDEX idx_transaction_audit_user_id ON tb_transaction_audit(user_id);
CREATE INDEX idx_transaction_audit_created_at ON tb_transaction_audit(created_at);
CREATE INDEX idx_transaction_audit_correlation_id ON tb_transaction_audit(correlation_id);
CREATE INDEX idx_transaction_audit_operation_type ON tb_transaction_audit(operation_type);
CREATE INDEX idx_transaction_audit_status ON tb_transaction_audit(transaction_status);
CREATE INDEX idx_transaction_audit_origin_ip ON tb_transaction_audit(origin_ip);
CREATE INDEX idx_transaction_audit_session_id ON tb_transaction_audit(session_id);

CREATE INDEX idx_audit_wallet_created_at ON tb_transaction_audit(wallet_id, created_at DESC);
CREATE INDEX idx_audit_user_created_at ON tb_transaction_audit(user_id, created_at DESC);
CREATE INDEX idx_audit_status_created_at ON tb_transaction_audit(transaction_status, created_at DESC);
CREATE INDEX idx_audit_operation_wallet ON tb_transaction_audit(operation_type, wallet_id, created_at DESC);
CREATE INDEX idx_audit_ip_created_at ON tb_transaction_audit(origin_ip, created_at DESC);

CREATE INDEX idx_audit_failed_transactions ON tb_transaction_audit(created_at DESC)
    WHERE transaction_status IN ('FAILED', 'ROLLED_BACK');

COMMENT ON TABLE tb_transaction_audit IS 'Comprehensive audit trail for all wallet transactions with technical metadata';
COMMENT ON COLUMN tb_transaction_audit.id IS 'Unique identifier for the audit record (UUID)';
COMMENT ON COLUMN tb_transaction_audit.transaction_id IS 'Reference to the transaction being audited';
COMMENT ON COLUMN tb_transaction_audit.wallet_id IS 'ID of the wallet involved in the transaction';
COMMENT ON COLUMN tb_transaction_audit.user_id IS 'ID of the user who owns the wallet';
COMMENT ON COLUMN tb_transaction_audit.operation_type IS 'Type of operation: CREDIT, DEBIT, TRANSFER_OUT, TRANSFER_IN, REFUND, ADJUSTMENT';
COMMENT ON COLUMN tb_transaction_audit.amount IS 'Amount of the transaction (always positive)';
COMMENT ON COLUMN tb_transaction_audit.balance_before IS 'Wallet balance before the transaction';
COMMENT ON COLUMN tb_transaction_audit.balance_after IS 'Wallet balance after the transaction';
COMMENT ON COLUMN tb_transaction_audit.transaction_status IS 'Status: INITIATED, COMPLETED, FAILED, ROLLED_BACK';
COMMENT ON COLUMN tb_transaction_audit.created_at IS 'Timestamp when the audit record was created';
COMMENT ON COLUMN tb_transaction_audit.created_by IS 'User or system that initiated the transaction';
COMMENT ON COLUMN tb_transaction_audit.origin_ip IS 'IP address of the client that initiated the transaction';
COMMENT ON COLUMN tb_transaction_audit.user_agent IS 'User agent string from the client request';
COMMENT ON COLUMN tb_transaction_audit.session_id IS 'Session identifier for grouping related operations';
COMMENT ON COLUMN tb_transaction_audit.correlation_id IS 'Correlation identifier for distributed tracing';
COMMENT ON COLUMN tb_transaction_audit.description IS 'Human-readable description of the transaction';
COMMENT ON COLUMN tb_transaction_audit.metadata IS 'Additional metadata stored as JSON or text';

