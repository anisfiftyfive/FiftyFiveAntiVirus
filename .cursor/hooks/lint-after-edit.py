#!/usr/bin/env python3
import json
import os
import subprocess
import sys

payload = json.load(sys.stdin)
path = payload.get("file_path") or payload.get("path") or ""
lower = path.lower()
if not lower.endswith((".kt", ".kts", ".xml")):
    print(json.dumps({}))
    sys.exit(0)

root = os.getcwd()
gradlew = os.path.join(root, "gradlew")
if not os.path.isfile(gradlew):
    print(json.dumps({"additional_context": "Edited a source file; run ./gradlew lintDebug --quiet"}))
    sys.exit(0)

try:
    result = subprocess.run(
        [gradlew, "lintDebug", "--quiet"],
        cwd=root,
        capture_output=True,
        text=True,
        timeout=170,
    )
except Exception as exc:
    print(json.dumps({"additional_context": f"lintDebug did not run ({exc}). Run ./gradlew lintDebug --quiet"}))
    sys.exit(0)

if result.returncode != 0:
    tail = (result.stdout or result.stderr or "lintDebug failed")[-2000:]
    print(json.dumps({
        "additional_context": "lintDebug failed after the edit. Fix lint before claiming done.\n" + tail
    }))
else:
    print(json.dumps({"additional_context": "lintDebug passed after the edit."}))
