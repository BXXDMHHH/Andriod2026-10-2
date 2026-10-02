CREATE TABLE users (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(64) NOT NULL, password_hash VARCHAR(100) NOT NULL,
 nickname VARCHAR(80), status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
 CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB;
CREATE TABLE conversations (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, owner_id BIGINT NOT NULL, title VARCHAR(160) NOT NULL,
 created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, INDEX idx_conversation_owner_updated (owner_id, updated_at),
 CONSTRAINT fk_conversations_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE messages (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, conversation_id BIGINT NOT NULL, sender_type VARCHAR(20) NOT NULL,
 sender_id BIGINT NOT NULL, content_type VARCHAR(20) NOT NULL DEFAULT 'TEXT', content TEXT NOT NULL, client_msg_id VARCHAR(100),
 created_at DATETIME(6) NOT NULL, INDEX idx_message_conversation_id (conversation_id, id),
 CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
 CONSTRAINT uk_message_client_id UNIQUE (conversation_id, client_msg_id)
) ENGINE=InnoDB;
