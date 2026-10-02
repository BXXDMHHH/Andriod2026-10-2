import os
import subprocess
import time
import urllib.error
import urllib.request

process_id = None
try:
    with open("/tmp/chatflow-backend.pid", encoding="utf-8") as handle:
        process_id = int(handle.read().strip())
except (OSError, ValueError):
    pass

for _ in range(60):
    try:
        with urllib.request.urlopen("http://127.0.0.1:8080/actuator/health", timeout=2) as response:
            if response.status == 200:
                print("Spring Boot health endpoint is ready")
                raise SystemExit(0)
    except (urllib.error.URLError, TimeoutError, OSError):
        pass
    if process_id is not None and subprocess.run(["bash", "-lc", f"kill -0 {process_id}"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode != 0:
        break
    time.sleep(2)

if os.path.exists("/tmp/chatflow-backend.log"):
    with open("/tmp/chatflow-backend.log", encoding="utf-8", errors="replace") as log:
        print(log.read()[-16000:])
raise SystemExit("Spring Boot failed to become healthy")
