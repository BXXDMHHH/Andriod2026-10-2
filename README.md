# ChatFlow

Android chat client with a Spring Boot backend and a lightweight workflow engine.

## Week 1 scope

- Product requirements and architecture documentation
- REST API and database design documents
- Spring Boot backend scaffold (Java 17, Maven)
- Android scaffold (Kotlin, Jetpack Compose, minSdk 26)
- Docker Compose services for MySQL 8 and Redis 7
- GitHub Actions build checks for backend and Android

> This repository is currently a project scaffold. Authentication, persistence entities, messaging, WebSocket delivery, and workflow execution are planned for later milestones.

## Repository layout

- `chatflow-backend/` — Spring Boot service
- `chatflow-android/` — Android app
- `chatflow-deploy/` — local dependency services
- `chatflow-docs/` — requirements, architecture, API, database, and week-one checklist

## Quick start

1. Read `chatflow-docs/requirements.md` and `chatflow-docs/architecture.md`.
2. Start local dependencies: `docker compose -f chatflow-deploy/docker-compose.yml up -d`.
3. Build the backend from `chatflow-backend/` with `mvn verify`.
4. Build the Android app from `chatflow-android/` with Gradle 8.7 and an Android SDK installed.

Do not use production credentials in local environment files. The Compose file uses development-only credentials intended for local development.
