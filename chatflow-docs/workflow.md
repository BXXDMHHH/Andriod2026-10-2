# Workflow Engine (Week 3)

## Definition format
A workflow is a JSON object with `startNodeId`, `nodes`, and `edges`. Node IDs must be unique and every edge must reference existing nodes.

```json
{
  "startNodeId": "start",
  "nodes": [
    {"id": "start", "type": "START"},
    {"id": "welcome", "type": "SEND_MESSAGE", "content": "Hello ${userInput}"},
    {"id": "wait", "type": "WAIT_INPUT", "variable": "userInput"},
    {"id": "branch", "type": "CONDITION"},
    {"id": "done", "type": "END"}
  ],
  "edges": [
    {"from": "start", "to": "welcome"},
    {"from": "welcome", "to": "wait"},
    {"from": "wait", "to": "branch"},
    {"from": "branch", "to": "done", "condition": "userInput contains 'help'"}
  ]
}
```

Supported node types: `START`, `SEND_MESSAGE`, `WAIT_INPUT`, `CONDITION`, `SET_VARIABLE`, `DELAY`, `END`. `DELAY.delayMs` is clamped to 0–5000 ms. `SEND_MESSAGE.content` supports `${variable}` substitution. Condition syntax supports `variable == 'value'`, `variable != 'value'`, and `variable contains 'text'`; `default`/`else` is the fallback edge. A run has a 100-node safety limit. HTTP requests are not enabled yet; outbound network nodes need an explicit host allowlist to avoid SSRF.

## API
- `POST /api/v1/workflows`: create an owned workflow with `name`, optional `description`, and `definition`.
- `GET /api/v1/workflows`: list current user's active workflows.
- `GET /api/v1/workflows/{id}`: retrieve an owned workflow.
- `POST /api/v1/workflows/{id}/run`: body `{"conversationId":123,"variables":{"key":"value"}}`.
- `GET /api/v1/workflow-runs/{id}`: get run status, variables, error and node history.
- `POST /api/v1/workflow-runs/{id}/input`: body `{"input":"value"}` to resume a `WAITING` run.
- `POST /api/v1/workflow-runs/{id}/cancel`: cancel an active/waiting run.

Workflow definitions and run records are owner-scoped. The run and node-run tables persist statuses, variables, timestamps, outputs, and errors.

## WebSocket / STOMP
- Connect to `/ws` with a STOMP `CONNECT` native header `Authorization: Bearer <accessToken>`.
- Subscribe only to `/topic/conversations/{conversationId}` for a conversation owned by the authenticated user.
- Events are JSON objects with a `type` field. Message event: `message.created`; workflow events include `workflow.started`, `workflow.node.completed`, `workflow.waiting`, `workflow.resumed`, `workflow.completed`, `workflow.failed`, and `workflow.canceled`.
- The built-in simple broker is in-memory and intended for this single-instance MVP. A multi-instance deployment would require a broker relay or shared event transport.

## Security and limitations
- The STOMP CONNECT frame must carry a valid JWT. SUBSCRIBE checks conversation ownership.
- Browser origins are configurable with `chatflow.websocket.allowed-origin-patterns`; defaults are local development origins.
- Execution currently runs synchronously in the request thread. DELAY blocks that thread briefly (maximum 5 seconds); durable background scheduling, retries, timeout policies, and HTTP request nodes are later work.
