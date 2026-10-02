CREATE TABLE workflows (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(120) NOT NULL, description VARCHAR(500),
 version INT NOT NULL DEFAULT 1, definition_json LONGTEXT NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 created_by BIGINT NOT NULL, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 INDEX idx_workflows_creator_updated (created_by, updated_at),
 CONSTRAINT fk_workflows_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE workflow_runs (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, workflow_id BIGINT NOT NULL, conversation_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'RUNNING', current_node_id VARCHAR(100),
 variables_json LONGTEXT NOT NULL, started_at DATETIME(6) NOT NULL, ended_at DATETIME(6), error_message VARCHAR(2000),
 INDEX idx_workflow_runs_user_started (user_id, started_at), INDEX idx_workflow_runs_conversation (conversation_id, id),
 CONSTRAINT fk_workflow_runs_workflow FOREIGN KEY (workflow_id) REFERENCES workflows(id),
 CONSTRAINT fk_workflow_runs_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
 CONSTRAINT fk_workflow_runs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE workflow_node_runs (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, run_id BIGINT NOT NULL, node_id VARCHAR(100) NOT NULL,
 node_type VARCHAR(40) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'RUNNING', input_json LONGTEXT,
 output_json LONGTEXT, started_at DATETIME(6) NOT NULL, ended_at DATETIME(6), error_message VARCHAR(2000),
 INDEX idx_workflow_node_runs_run (run_id, id),
 CONSTRAINT fk_workflow_node_runs_run FOREIGN KEY (run_id) REFERENCES workflow_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB;
