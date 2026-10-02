# Week 3 Acceptance Checklist

## Implemented
- [x] STOMP endpoint `/ws`, simple `/topic` broker and `/app` prefix.
- [x] JWT validation on STOMP CONNECT.
- [x] Conversation ownership enforced for `/topic/conversations/{id}` subscriptions.
- [x] Message-created events broadcast to the conversation topic after REST message saves.
- [x] Workflow definitions persisted with owner isolation.
- [x] Workflow run and node-run records persisted.
- [x] Engine supports START, SEND_MESSAGE, WAIT_INPUT, CONDITION, SET_VARIABLE, DELAY and END.
- [x] Waiting runs accept input and resume execution.
- [x] Workflow status and generated messages are published to the conversation topic.
- [x] Automated tests cover workflow wait/input/conditional routing/generated messages and owner isolation.
- [x] STOMP interceptor tests cover valid JWT connection, missing token rejection, and cross-user subscription rejection.
- [x] GitHub Actions validates Flyway V1/V2 against MySQL 8 and runs a workflow from WAITING through input/condition to SUCCESS against MySQL.

## Latest CI evidence
- Backend CI: 8 tests passed, 0 failures, 0 errors.
- MySQL integration CI: Compose validation, MySQL/Redis startup, both Flyway migrations, REST message persistence, workflow definition/run/node history, WAITING -> input -> condition -> SUCCESS all passed.
- The CI tests validate the interceptor and workflow engine; a full Android client-to-server STOMP end-to-end run is not yet included.

## Explicit limitations
- HTTP_REQUEST nodes are not enabled until an outbound-host allowlist is implemented.
- The simple WebSocket broker is single-instance; no Redis broker relay yet.
- Workflow execution is synchronous with a 100-node cap; DELAY is capped at 5 seconds.
- No durable background scheduler, retry policy, or workflow timeout service yet.
