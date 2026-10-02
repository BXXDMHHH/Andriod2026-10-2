# Week 2 Acceptance Checklist
## Delivered
- [x] Registration and login with BCrypt password hashes and signed JWT access tokens.
- [x] Current-user endpoint: GET /api/v1/auth/me.
- [x] Flyway V1 migration for users, conversations, and messages.
- [x] Authenticated conversation create/list/detail endpoints.
- [x] Authenticated message send/history endpoints with bounded pagination.
- [x] Conversation ownership enforced on detail and message routes.
- [x] GitHub Actions backend verification and H2-backed API integration tests.
- [x] GitHub Actions starts Spring Boot against real MySQL 8, applies Flyway V1, and exercises authenticated API persistence.
## Test coverage
- Register, login, create conversation, send message, read history.
- Unauthenticated access is rejected.
- Wrong password is rejected.
- One user cannot read another user's conversation.
- MySQL migration and API smoke test: register, current user, create conversation, save message, read history.
## Explicit scope limits
- Refresh tokens, logout/revocation, password reset, rate limiting and identity verification are not implemented.
- No WebSocket, workflow engine, assistant replies or Android login screens yet.
- Automated API integration tests use H2 in MySQL compatibility mode; a separate CI job also validates the Flyway migration and API read/write path against a real MySQL 8 container.
