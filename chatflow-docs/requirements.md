# ChatFlow Requirements (Week 1 baseline)

## Product goal
Build an Android chat client backed by a single Spring Boot application. The target end-to-end flow is: sign in, view/create a conversation, send a message, persist it, receive real-time updates, trigger a workflow, and inspect message history.

## MVP capabilities (planned)
1. User registration, login, and JWT authentication.
2. Conversation listing, creation, detail, and deletion.
3. Text messages, history pagination, and real-time reception.
4. WebSocket events for messages, read receipts, typing, errors, and workflow status.
5. Workflow listing and execution, including start, send-message, wait-input, condition, HTTP request, variable, delay, and end nodes.
6. Workflow run and node execution records.
7. Docker Compose local dependencies and project documentation.

## Out of scope for the initial MVP
- BPMN visual editor, microservices, audio/video calling, advanced group permissions, recommendation algorithms, and AI model integration.

## Week 1 deliverables
- [x] Requirements, architecture, API, and database design documents.
- [x] Spring Boot and Android source scaffolds.
- [x] Docker Compose configuration for MySQL and Redis.
- [x] CI workflows for backend and Android builds.
- [ ] Verify builds in GitHub Actions and inspect the run logs.
- [ ] Start MySQL/Redis with Docker on a machine with Docker installed.

## Acceptance criteria
- Backend uses Java 17 and Spring Boot 3.2.x and has a passing Maven verification build.
- Android uses Kotlin, Jetpack Compose, minSdk 26, targetSdk 34, and can assemble a debug APK.
- Compose defines MySQL 8 and Redis 7 with persistent MySQL data and health checks.
- Documentation describes architecture, API contracts, database entities, and local setup.

Business endpoints and database migrations are intentionally not implemented in Week 1.
