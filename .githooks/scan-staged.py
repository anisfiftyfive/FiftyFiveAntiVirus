#!/usr/bin/env python3
import os
import re
import subprocess
import sys

PATTERNS = [
    re.compile(r"sk-[A-Za-z0-9]{20,}"),
    re.compile(r"AKIA[0-9A-Z]{16}"),
    re.compile(r"-----BEGIN (RSA|OPENSSH|PRIVATE) KEY-----"),
    re.compile(r"github_pat_[A-Za-z0-9_]{20,}"),
    re.compile(r"api[_-]?key\s*=\s*['\"][^'\"]{8,}['\"]", re.I),
]
BLOCKED_NAMES = {
    "local.properties",
    "keystore.properties",
    "secrets.env",
    ".env",
}

def blocked_path(path: str) -> bool:
    base = os.path.basename(path).lower()
    return base in BLOCKED_NAMES or base.endswith((".jks", ".keystore", ".pem", ".p12", ".pfx")) or base.startswith(".env.")

names = subprocess.check_output(
    ["git", "diff", "--cached", "--name-only", "-z"],
    text=True,
).split("\0")
names = [name for name in names if name]
for path in names:
    if blocked_path(path):
        print(f"pre-commit: refusing to commit secret file {path}", file=sys.stderr)
        sys.exit(1)

diff = subprocess.check_output(["git", "diff", "--cached", "-U0"], text=True, errors="replace")
for line in diff.splitlines():
    if not line.startswith("+") or line.startswith("+++"):
        continue
    for pattern in PATTERNS:
        if pattern.search(line):
            print("pre-commit: refusing commit; staged diff looks like a secret", file=sys.stderr)
            sys.exit(1)
