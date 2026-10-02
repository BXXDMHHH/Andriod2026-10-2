# Database Design and Migration
Flyway migrations create the initial application tables and Week 3 workflow tables in MySQL 8.

## users
- `id`: BIGINT primary key, auto-increment
- `username`: unique and required
- `password_hash`: BCrypt hash; plaintext passwords are never persisted
- `nickname`, `status`, `created_at`, `updated_at`

## conversations
- `id`: BIGINT primary key
- `owner_id`: FK to users, cascade on user deletion
- `title`, `created_at`, `updated_at`
- Index: `owner_id, updated_at`

## messages
- `id`: BIGINT primary key
- `conversation_id`: FK to conversations, cascade on conversation deletion
- `sender_type`, `sender_id`, `content_type`, `content`, optional `client_msg_id`, `created_at`
- Index: `conversation_id, id`
- Unique key: `conversation_id, client_msg_id`

## workflows
- `id`, `name`, `description`, `version`, `definition_json`, `status`, `created_by`, timestamps
- `created_by` references users and cascades on user deletion
- Index: `created_by, updated_at`
- Definition is versioned JSON content; V1 supports START, SEND_MESSAGE, WAIT_INPUT, CONDITION, SET_VARIABLE, DELAY and END nodes.

## workflow_runs
- `id`, `workflow_id`, `conversation_id`, `user_id`, `status`, `current_node_id`, `variables_json`, `started_at`, `ended_at`, `error_message`
- FKs reference workflow, conversation and user
- Indexes: `user_id, started_at`; `conversation_id, id`
- Statuses used by the engine: RUNNING, WAITING, SUCCESS, FAILED, CANCELED.

## workflow_node_runs
- `id`, `run_id`, `node_id`, `node_type`, `status`, `input_json`, `output_json`, timestamps, `error_message`
- FK to workflow_runs with cascade delete
- Index: `run_id, id`

Schema changes must be added as versioned Flyway migrations; Hibernate uses `ddl-auto: validate` outside tests. API integration tests use H2 in MySQL compatibility mode, and a separate GitHub Actions job verifies Flyway migrations against real MySQL 8.
