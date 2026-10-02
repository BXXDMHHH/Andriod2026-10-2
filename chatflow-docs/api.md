# REST API Contract
Base prefix: `/api/v1`. Week 2 implemented endpoints are marked below. Responses currently use direct JSON DTOs; the common response envelope in the original design is not yet applied.
## Authentication
| Method | Path | Purpose | Status |
|---|---|---|---|
| POST | `/auth/register` | Register and return JWT access token | Implemented |
| POST | `/auth/login` | Login and return JWT access token | Implemented |
| GET | `/auth/me` | Current authenticated user | Implemented |
| POST | `/auth/refresh` | Refresh token | Planned |
| POST | `/auth/logout` | Revoke token | Planned |
Registration accepts `{"username":"alice","password":"at-least-8-chars","nickname":"Alice"}`. Usernames are 3–64 chars and accept letters, digits, underscore, dot, hyphen. Passwords are 8–72 chars and stored as BCrypt hashes.
## Conversations
| Method | Path | Purpose | Status |
|---|---|---|---|
| GET | `/conversations` | List current user's conversations | Implemented |
| POST | `/conversations` | Create conversation | Implemented |
| GET | `/conversations/{id}` | Get owned conversation | Implemented |
| PATCH | `/conversations/{id}` | Update title/pin | Planned |
| DELETE | `/conversations/{id}` | Delete conversation | Planned |
Create request: `{"title":"New conversation"}`.
## Messages
| Method | Path | Purpose | Status |
|---|---|---|---|
| GET | `/conversations/{id}/messages?before=&limit=50` | Paginated history | Implemented |
| POST | `/conversations/{id}/messages` | Send text message | Implemented |
| POST | `/messages/{id}/read` | Mark read | Planned |
| GET | `/messages/search?keyword=` | Search | Planned |
Send request: `{"content":"Hello","clientMsgId":"optional-client-id"}`. History pages are returned oldest-first within the page; limit is clamped to 1–100. Assistant replies and WebSocket delivery are later milestones.
## Health and security
- GET `/actuator/health`
- GET `/actuator/info`
All endpoints except registration, login, health and info require `Authorization: Bearer <accessToken>`. Conversation detail/history/send routes enforce ownership. JWT access tokens expire after 3600 seconds by default. Set `JWT_SECRET` to a strong base64-encoded secret outside development. Refresh/revocation, rate limiting, and password reset are not implemented.
Workflow APIs, file APIs and WebSocket endpoint `/ws` are planned for later weeks.
