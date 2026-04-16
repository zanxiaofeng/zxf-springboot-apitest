-- Project test data
INSERT INTO project (id, name, details) VALUES ('proj-001', 'Demo Project One', 'Details for project one');
INSERT INTO project (id, name, details) VALUES ('proj-002', 'Demo Project Two', 'Details for project two');
INSERT INTO project (id, name, details) VALUES ('proj-003', 'Demo Project Three', NULL);
INSERT INTO project (id, name, details) VALUES ('proj-delete', 'Project To Delete', NULL);

-- Task test data
INSERT INTO task (id, name, status, project_id, priority) VALUES ('task-001', 'Test Task One', 'PENDING', 'proj-001', 1);
INSERT INTO task (id, name, status, project_id, priority) VALUES ('task-002', 'Test Task Two', 'COMPLETED', 'proj-001', 2);
INSERT INTO task (id, name, status, project_id, priority) VALUES ('task-list', 'List Test Task', 'PENDING', NULL, 1);