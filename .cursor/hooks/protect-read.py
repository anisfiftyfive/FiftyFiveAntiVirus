#!/usr/bin/env python3
import json
import os
import sys

payload = json.load(sys.stdin)
path = (payload.get("file_path") or "").replace("\\", "/")
base = os.path.basename(path).lower()

denied_names = {
    "local.properties",
    "keystore.properties",
    "secrets.env",
    ".env",
}
denied_suffixes = (".jks", ".keystore", ".pem", ".p12", ".pfx")

deny = base in denied_names or base.endswith(denied_suffixes) or "/.env." in f"/{base}" or base.startswith(".env.")
if deny:
    print(json.dumps({
        "permission": "deny",
        "user_message": "Agents cannot read secret or keystore files in this repo.",
    }))
    sys.exit(0)

print(json.dumps({"permission": "allow"}))
