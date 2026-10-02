# Database Design (planned)

The following schema is a design baseline only. Entities, repositories, and Flyway migrations are deferred to Week 2.

## user
Fields: id, username, password_hash, nickname, avatar_url, status, created_at, updated_at. Unique index on username.

## conversation
Fields: id, user_id, title, type, workflow_id, last_message_id, last_message_at, status, created_at, updated_at. Index on user_id + last_message_at.

## message
Fields: id, conversation_id, sender_type, sender_id, content_type, content, metadata_json, status, client_msg_id, created_at. sender_type: USER, ASSISTANT, SYSTEM. content_type: TEXT, IMAGE, FILE, FLOW. client_msg_id should support idempotency.

## workflow
Fields: id, name, description, version, definition_json, status, created_by, created_at, updated_at.

## workflow_run
Fields: id, workflow_id, conversation_id, user_id, status, current_node_id, variables_json, started_at, ended_at, error_message. Status: RUNNING, WAITING, SUCCESS, FAILED, CANCELED.

## workflow_node_run
Fields: id, run_id, node_id, node_type, status, input_json, output_json, started_at, ended_at, error_message.

## attachment
Fields: id, message_id, file_name, file_url, mime_type, size, created_at.

## refresh_token (optional)
Fields: id, user_id, token, expires_at, revoked, created_at.

## Design notes
- Every conversation/message read must enforce user ownership.
- Passwords must be stored as BCrypt hashes; never store plaintext passwords.
- JWT and refresh-token lifecycle needs explicit expiry and revocation rules.
- Use Flyway versioned migrations from Week 2 onward.
