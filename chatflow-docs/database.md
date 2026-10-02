# Database Design and Migration
Week 2 Flyway migration `V1__create_users_conversations_messages.sql` creates the first three tables in MySQL 8.
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
- Unique key: `conversation_id, client_msg_id`. Duplicate-key conflicts are not yet translated to a friendly API error.
## Data access rules
- Every conversation detail/history/send request checks ownership against the authenticated user.
- Passwords use BCrypt.
- Schema changes must be added as versioned Flyway migrations; Hibernate uses `ddl-auto: validate` outside tests.
Workflow, workflow_run, workflow_node_run, attachment, refresh_token and richer conversation fields remain design-only.
