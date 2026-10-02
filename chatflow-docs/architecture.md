# Architecture (Updated Week 3)

## System context
```text
Android app (Kotlin + Compose)
  | REST API and STOMP over WebSocket
  v
Spring Boot monolith
  | JPA / Flyway
  v
MySQL 8 ---- Redis 7 (optional cache / rate limiting)
```

## Backend packages
- `auth`, `user`: registration, login and JWT
- `conversation`, `message`: owned conversations, persistence, message history and realtime events
- `websocket`: STOMP endpoint, JWT CONNECT validation and conversation-topic ownership checks
- `workflow`: definitions, execution engine, workflow runs and node-run audit records

## Realtime protocol
- WebSocket/STOMP endpoint: `/ws`
- Clients send a JWT bearer token in the STOMP CONNECT native header.
- Clients subscribe to `/topic/conversations/{id}`; the interceptor checks that the authenticated user owns the conversation.
- REST message saves and workflow status transitions publish JSON events to the conversation topic.
- The simple broker is in-memory and intended for a single backend instance.

## Workflow engine
Definitions are JSON graphs with nodes and directed edges. The synchronous MVP engine supports START, SEND_MESSAGE, WAIT_INPUT, CONDITION, SET_VARIABLE, DELAY and END. WAIT_INPUT persists the current node and variables; a subsequent input request resumes from the outgoing edge. Each node execution is persisted in `workflow_node_runs`. A 100-node cap prevents runaway loops, and DELAY is capped at 5 seconds.

## Security and quality gates
REST routes continue to require JWT except public registration/login and health endpoints. WebSocket CONNECT validates JWT and subscriptions enforce conversation ownership. GitHub Actions runs Maven verification and MySQL/Redis Compose smoke tests; workflow integration tests exercise waiting, input, conditional routing and generated messages.

Known MVP limits: no HTTP_REQUEST workflow node until outbound host allowlisting is implemented; no distributed broker relay, asynchronous job scheduler, or retry policy.
