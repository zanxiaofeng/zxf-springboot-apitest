-- Case-level SQL: Create a specific test project for isolated test scenario
-- This demonstrates case-level SQL that runs before a specific test method
INSERT INTO project (id, name, created_at, updated_at)
VALUES ('p-test', 'Case Test Project', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);