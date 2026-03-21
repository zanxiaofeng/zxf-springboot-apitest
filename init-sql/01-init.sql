-- Demo Application MySQL Initialization Script
-- Create database if not exists (handled by docker-compose)

-- Create project table
CREATE TABLE IF NOT EXISTS project (
    id VARCHAR(40) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert initial data
INSERT INTO project (id, name) VALUES ('proj-001', 'Demo Project One');
INSERT INTO project (id, name) VALUES ('proj-002', 'Demo Project Two');
INSERT INTO project (id, name) VALUES ('proj-003', 'Demo Project Three');