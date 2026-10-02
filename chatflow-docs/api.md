# REST API Contract (planned)

Base prefix: `/api/v1`. This document records the planned contract; endpoints are not implemented in Week 1.

## Response envelope
```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "traceId": "request-correlation-id"
}
```

## Authentication
| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/auth/register` | Register |
| POST | `/auth/login` | Login |
| POST | `/auth/refresh` | Refresh token |
| POST | `/auth/logout` | Logout |
| GET | `/auth/me` | Current user |

## Conversations
| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/conversations` | List conversations |
| POST | `/conversations` | Create conversation |
| GET | `/conversations/{id}` | Get conversation |
| PATCH | `/conversations/{id}` | Update title/pin |
| DELETE | `/conversations/{id}` | Delete conversation |

## Messages
| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/conversations/{id}/messages?before=&limit=` | Paginated history |
| POST | `/conversations/{id}/messages` | Send text message |
| POST | `/messages/{id}/read` | Mark read |
| GET | `/messages/search?keyword=` | Search (optional) |

## Workflows
| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/workflows` | List workflows |
| GET | `/workflows/{id}` | Workflow detail |
| POST | `/workflows/{id}/run` | Start run |
| GET | `/workflow-runs/{id}` | Run status |
| POST | `/workflow-runs/{id}/cancel` | Cancel run |
| POST | `/workflow-runs/{id}/input` | Resume waiting run |

## Files and health
- POST `/files/upload`
- GET `/files/{id}`
- GET `/actuator/health`
- GET `/actuator/info`
- GET `/actuator/metrics`

## WebSocket contract (planned)
Endpoint: `/ws` (STOMP). Planned subscriptions: `/user/queue/messages`, `/user/queue/flow`, and `/topic/conversations/{id}`. Planned application destinations: `/app/chat.send`, `/app/flow.input`, `/app/chat.typing`, `/app/chat.read`.

Planned events: `MESSAGE_CREATED`, `MESSAGE_UPDATED`, `MESSAGE_READ`, `TYPING`, `FLOW_RUN_STATUS`, `FLOW_NODE_STATUS`, `ERROR`.

## Error codes (planned)
- 0 success; 1001 invalid parameters
- 2001 unauthenticated; 2002 expired token; 2003 invalid token
- 3001 forbidden; 4001 not found; 4002 duplicate request
- 5001 invalid workflow; 5002 node execution failed; 5003 input timeout; 5004 run canceled
- 9001 internal error
