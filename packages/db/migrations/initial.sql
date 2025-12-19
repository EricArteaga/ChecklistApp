-- Migración inicial para packages/db
CREATE TABLE IF NOT EXISTS migrations (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  applied_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);
