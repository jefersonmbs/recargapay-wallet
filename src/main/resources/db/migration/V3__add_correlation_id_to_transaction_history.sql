ALTER TABLE tb_transaction_history
ADD COLUMN correlation_id VARCHAR(100);

CREATE INDEX idx_transaction_correlation_id ON tb_transaction_history(correlation_id);

COMMENT ON COLUMN tb_transaction_history.correlation_id IS 'Correlation identifier for distributed tracing and grouping related transactions';