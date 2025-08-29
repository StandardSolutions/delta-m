CREATE TABLE IF NOT EXISTS ${outbox_table} (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP,
    
    FOREIGN KEY (recipient_id) REFERENCES ${recipient_table}(id)
);