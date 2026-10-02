import json
import os
import time
import urllib.error
import urllib.request

BASE = "http://127.0.0.1:8080"
USERNAME = "ci_" + os.environ.get("GITHUB_RUN_ID", str(int(time.time())))
PASSWORD = "strong-pass-123"

def request(path, method="GET", payload=None, token=None, expected=200):
    headers = {"Accept": "application/json"}
    data = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(payload).encode("utf-8")
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            status, body = response.status, response.read().decode("utf-8")
    except urllib.error.HTTPError as error:
        status, body = error.code, error.read().decode("utf-8")
    if status != expected:
        raise AssertionError(f"{method} {path}: expected {expected}, got {status}: {body}")
    return json.loads(body) if body else {}

registered = request("/api/v1/auth/register", "POST", {
    "username": USERNAME, "password": PASSWORD, "nickname": "CI"
})
token = registered.get("accessToken")
assert token, "register did not return an access token"
request("/api/v1/auth/me", token=token)
conversation = request("/api/v1/conversations", "POST", {"title": "MySQL CI conversation"}, token)
conversation_id = conversation["id"]
sent = request(f"/api/v1/conversations/{conversation_id}/messages", "POST",
               {"content": "Persisted in MySQL", "clientMsgId": "mysql-ci-message"}, token)
assert sent["content"] == "Persisted in MySQL"
history = request(f"/api/v1/conversations/{conversation_id}/messages", token=token)
assert history and history[0]["content"] == "Persisted in MySQL"
request("/api/v1/conversations", expected=401)
print("PASS: register, JWT auth, conversation create, message write/read, unauthenticated rejection")
