# ChatFlow

Android chat client with a Spring Boot backend and a lightweight JSON workflow engine.

## Current milestone

Week 6 deployment and acceptance baseline:
- JWT authentication and conversation/message REST APIs
- STOMP WebSocket real-time delivery
- Workflow execution with WAITING input and SUCCESS completion
- Android Compose client with reconnect and duplicate-message protection
- Docker Compose deployment: MySQL + Redis + Backend + Nginx
- OpenAPI 3.0.3 contract and deployment/acceptance documents
- GitHub Actions backend, Android, deployment smoke and real-backend E2E workflows

## Repository layout

- chatflow-backend/ — Spring Boot service and Dockerfile
- chatflow-android/ — Kotlin/Compose Android app
- chatflow-deploy/ — Docker Compose, Nginx and environment template
- chatflow-docs/ — requirements, architecture, API, workflow, deployment and test reports

## Local backend stack

Create chatflow-deploy/.env from chatflow-deploy/.env.example and change secrets.

Start the full stack:
docker compose --env-file chatflow-deploy/.env -f chatflow-deploy/docker-compose.yml up -d --build

Health:
curl http://localhost/actuator/health

Backend direct port:
http://localhost:8080

Nginx entry:
http://localhost

## Android

Default debug endpoints target the Android emulator:
API_BASE_URL=http://10.0.2.2:8080/
WS_URL=ws://10.0.2.2:8080/ws

For a deployed server, override Gradle properties:
gradle -PAPI_BASE_URL=https://example.com/ -PWS_URL=wss://example.com/ws assembleDebug

## Tests

Backend:
mvn --batch-mode --no-transfer-progress clean verify

Android:
gradle assembleDebug
gradle testDebugUnitTest
gradle connectedDebugAndroidTest

The CI workflows also run MySQL/Redis API smoke and a real Android emulator E2E flow.

## Acceptance demo

Registration → login → conversation → send message → WebSocket receive → trigger workflow → WAITING → submit input → SUCCESS → automatic reply.

See chatflow-docs/demo-script.md and chatflow-docs/deployment.md.
