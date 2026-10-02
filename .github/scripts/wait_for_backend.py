import os
import time
import urllib.error
import urllib.request

for _ in range(90):
    try:
        with urllib.request.urlopen("http://127.0.0.1:8080/actuator/health", timeout=2) as response:
            if response.status == 200:
                print("Spring Boot health endpoint is ready")
                raise SystemExit(0)
    except (urllib.error.URLError, TimeoutError, OSError):
        time.sleep(2)

if os.path.exists("/tmp/chatflow-backend.log"):
    with open("/tmp/chatflow-backend.log", encoding="utf-8", errors="replace") as log:
        print(log.read()[-12000:])
raise SystemExit("Spring Boot failed to become healthy within 180 seconds")
