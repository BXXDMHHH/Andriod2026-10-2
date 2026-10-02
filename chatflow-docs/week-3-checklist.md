# Week 3 Acceptance Checklist

## Implemented
- [x] STOMP endpoint `/ws`, simple `/topic` broker and `/app` prefix.
- [x] JWT validation on STOMP CONNECT.
- [x] Conversation ownership enforced for `/topic/conversations/{id}` subscriptions.
- [x] Message-created events broadcast to the conversation topic after REST message saves.
- [x] Workflow definitions persisted with owner isolation.
- [x] Workflow run and node-run records persisted.
- [x] Engine supports START, SEND_MESSAGE, WAIT_INPUT, CONDITION, SET_VARIABLE, DELAY and END.
- [x] Waiting runs can accept input and resume execution.
- [x] Workflow status and generated messages are published to the conversation topic.
- [x] GitHub Actions API integration tests cover wait/input/condition/human-handoff path and workflow ownership.

## Explicit limitations
- HTTP_REQUEST nodes are not enabled until an outbound-host allowlist is implemented.
- The simple WebSocket broker is single-instance; no Redis broker relay yet.
- Workflow execution is synchronous with a 100-node cap; DELAY is capped at 5 seconds.
- WebSocket auth and subscription ownership are enforced in the STOMP interceptor; end-to-end real STOMP client testing remains follow-up work.
