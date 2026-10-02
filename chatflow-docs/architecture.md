# Architecture (Week 1)

## System context

```text
Android app (Kotlin + Compose)
  | REST / future WebSocket
  v
Spring Boot monolith (Java 17)
  | JPA / future Flyway migrations
  v
MySQL 8 ---- Redis 7 (optional cache / rate limiting)
```

## Backend package plan
Base package: `com.chatflow`
- `common`: shared response types, errors, constants
- `config`: security, web, websocket, OpenAPI and serialization configuration
- `auth`: registration, login and token lifecycle
- `user`: user profile
- `conversation`: conversation lifecycle
- `message`: message persistence and history
- `workflow`: workflow definitions, execution and run records
- `file`: attachment metadata and uploads
- `websocket`: real-time push

Week 1 contains only the application entry point; business packages will be introduced with their corresponding features.

## Android package plan
- `data/remote`: Retrofit, WebSocket and DTOs
- `data/local`: Room and DataStore
- `data/repository`: repository implementations
- `domain/model`, `domain/repository`, `domain/usecase`
- `ui/auth`, `ui/conversation`, `ui/chat`, `ui/workflow`, `ui/settings`
- `di`: Hilt modules
- `common`: shared utilities and constants

The initial Android app is a minimal Compose shell, not a completed chat client.

## Local services
Docker Compose provides MySQL 8 and Redis 7. Backend defaults target localhost:3306 and localhost:6379; credentials in Compose are development-only. Production secrets and HTTPS configuration are not part of this scaffold.

## Planned message flow
Android sends a message -> REST endpoint validates ownership -> service persists message -> workflow engine optionally runs -> generated message is persisted -> WebSocket event is published -> Android updates UI and local cache.

## Quality gates
GitHub Actions runs Maven verification and Android debug assembly on pushes and pull requests. These gates check compilation/build packaging, not end-to-end behavior or emulator UI correctness.
