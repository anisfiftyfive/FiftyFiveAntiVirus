#!/usr/bin/env python3
import json
import re
import sys

payload = json.load(sys.stdin)
path = payload.get("file_path") or payload.get("path") or ""
text = payload.get("content") or payload.get("file_content") or ""

patterns = [
    r"sk-[A-Za-z0-9]{20,}",
    r"AKIA[0-9A-Z]{16}",
    r"-----BEGIN (RSA|OPENSSH|PRIVATE) KEY-----",
    r"api[_-]?key\s*=\s*['\"][^'\"]{8,}['\"]",
]

sensitive = path.endswith("local.properties") or path.endswith(".keystore") or path.endswith(".jks")
hits = [pattern for pattern in patterns if re.search(pattern, text, re.I)]
if sensitive or hits:
    print(json.dumps({
        "additional_context": "Possible secret material was written. Remove credentials from source. Do not commit local.properties, keystores, or .env files."
    }))
else:
    print(json.dumps({}))
