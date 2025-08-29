CREATE TABLE IF NOT EXISTS ${outbox_table} (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    recipient_id VARCHAR(255) NOT NULL,
    payload JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    
    CONSTRAINT fk_recipient FOREIGN KEY (recipient_id) REFERENCES ${recipient_table}(id)
);