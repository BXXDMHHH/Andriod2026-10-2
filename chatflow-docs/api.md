# REST API Contract
Base prefix: `/api/v1`. Week 2 and Week 3 endpoints are listed by status. Responses are direct JSON DTOs; a common response envelope is not yet applied.

## Authentication
| Method | Path | Purpose | Status |
|---|---|---|---|
| POST | `/auth/register` | Register and return JWT access token | Implemented |
| POST | `/auth/login` | Login and return JWT access token | Implemented |
| GET | `/auth/me` | Current authenticated user | Implemented |
| POST | `/auth/refresh` | Refresh token | Planned |
| POST | `/auth/logout` | Revoke token | Planned |

Registration accepts `{"username":"alice","password":"at-least-8-chars","nickname":"Alice"}`. Passwords are stored as BCrypt hashes.

## Conversations and messages
| Method | Path | Purpose | Status |
|---|---|---|---|
| GET | `/conversations` | List current user's conversations | Implemented |
| POST | `/conversations` | Create conversation | Implemented |
| GET | `/conversations/{id}` | Get owned conversation | Implemented |
| PATCH | `/conversations/{id}` | Update title/pin | Planned |
| DELETE | `/conversations/{id}` | Delete conversation | Planned |
| GET | `/conversations/{id}/messages?before=&limit=50` | Paginated history | Implemented |
| POST | `/conversations/{id}/messages` | Send text message | Implemented; emits `message.created` WebSocket event |
| POST | `/messages/{id}/read` | Mark read | Planned |

History pages are returned oldest-first within the page; limit is clamped to 1–100.

## Workflows
| Method | Path | Purpose |
|---|---|---|
| GET | `/workflows` | List current user's active workflow definitions |
| POST | `/workflows` | Create a workflow definition |
| GET | `/workflows/{id}` | Get owned workflow definition |
| POST | `/workflows/{id}/run` | Start a workflow against an owned conversation |
| GET | `/workflow-runs/{id}` | Get run state, variables and node history |
| POST | `/workflow-runs/{id}/input` | Resume a run waiting for input |
| POST | `/workflow-runs/{id}/cancel` | Cancel an active/waiting run |

See `workflow.md` for definition JSON, node types, condition syntax and WebSocket event contracts.

## WebSocket / STOMP
- Endpoint: `/ws`
- STOMP CONNECT native header: `Authorization: Bearer <accessToken>`
- Subscribe destination: `/topic/conversations/{conversationId}`
- Subscription is allowed only if the authenticated user owns that conversation.
- Events include `message.created` and workflow lifecycle events.

## Health and security
- GET `/actuator/health`
- GET `/actuator/info`
- All REST endpoints except registration, login, health and info require a JWT bearer token.
- Set `JWT_SECRET` to a strong base64-encoded secret outside development.
- Refresh/revocation, rate limiting, password reset, workflow HTTP-request nodes, and multi-instance WebSocket broker support are not implemented.
