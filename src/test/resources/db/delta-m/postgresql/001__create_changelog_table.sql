CREATE TABLE IF NOT EXISTS ${changelog_table} (
    id VARCHAR(255) PRIMARY KEY,
    description TEXT NOT NULL,
    executed_at TIMESTAMP NOT NULL DEFAULT NOW()
);