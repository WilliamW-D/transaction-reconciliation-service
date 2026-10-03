-- V1__init_schema.sql
-- Initial Schema for Transaction Reconciliation Service

CREATE TABLE internal_transactions (
    id BIGSERIAL PRIMARY KEY,
    reference_id VARCHAR(255) NOT NULL,
    account_number VARCHAR(255),
    amount NUMERIC(19, 4) NOT NULL,
    fee_amount NUMERIC(19, 4),
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    transaction_date TIMESTAMP NOT NULL,
    status VARCHAR(30),
    payment_method VARCHAR(50),
    is_duplicate BOOLEAN NOT NULL DEFAULT FALSE,
    batch_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_internal_ref_id ON internal_transactions(reference_id);
CREATE INDEX idx_internal_batch_id ON internal_transactions(batch_id);

CREATE TABLE settlement_records (
    id BIGSERIAL PRIMARY KEY,
    settlement_ref_id VARCHAR(255) NOT NULL,
    internal_ref_id VARCHAR(255) NOT NULL,
    settlement_amount NUMERIC(19, 4) NOT NULL,
    net_amount NUMERIC(19, 4),
    fee_amount NUMERIC(19, 4),
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    settlement_date TIMESTAMP NOT NULL,
    processor_name VARCHAR(50),
    is_duplicate BOOLEAN NOT NULL DEFAULT FALSE,
    batch_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_settlement_internal_ref_id ON settlement_records(internal_ref_id);
CREATE INDEX idx_settlement_ref_id ON settlement_records(settlement_ref_id);
CREATE INDEX idx_settlement_batch_id ON settlement_records(batch_id);

CREATE TABLE tolerance_configs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    amount_tolerance NUMERIC(19, 4) NOT NULL,
    fee_tolerance NUMERIC(19, 4) NOT NULL,
    date_window_minutes INT NOT NULL,
    currency_strict BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reconciliation_batches (
    id VARCHAR(100) PRIMARY KEY,
    batch_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_internal_count INT DEFAULT 0,
    total_settlement_count INT DEFAULT 0,
    matched_count INT DEFAULT 0,
    mismatch_count INT DEFAULT 0,
    missing_internal_count INT DEFAULT 0,
    missing_settlement_count INT DEFAULT 0,
    duplicate_count INT DEFAULT 0,
    total_discrepancy_amount NUMERIC(19, 4) DEFAULT 0.0000,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    tolerance_config_summary VARCHAR(500)
);

CREATE TABLE match_results (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    internal_transaction_id BIGINT REFERENCES internal_transactions(id) ON DELETE SET NULL,
    settlement_record_id BIGINT REFERENCES settlement_records(id) ON DELETE SET NULL,
    match_status VARCHAR(30) NOT NULL,
    discrepancy_category VARCHAR(30) NOT NULL,
    amount_difference NUMERIC(19, 4) DEFAULT 0.0000,
    fee_difference NUMERIC(19, 4) DEFAULT 0.0000,
    date_difference_seconds BIGINT DEFAULT 0,
    notes VARCHAR(1000),
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolution_notes VARCHAR(1000),
    resolved_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_match_batch_id ON match_results(batch_id);
CREATE INDEX idx_match_status ON match_results(match_status);
CREATE INDEX idx_discrepancy_category ON match_results(discrepancy_category);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(100),
    entity_id VARCHAR(100),
    action VARCHAR(50) NOT NULL,
    details VARCHAR(2000),
    performed_by VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_action ON audit_logs(action);

-- Insert Default Matching Tolerance Rules
INSERT INTO tolerance_configs (name, amount_tolerance, fee_tolerance, date_window_minutes, currency_strict, is_default, updated_at)
VALUES ('Standard Production Rules', 0.05, 0.01, 1440, TRUE, TRUE, CURRENT_TIMESTAMP);
