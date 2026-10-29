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
]
SKIP_PREFIX = ("ai-audit/",)
SKIP_SUFFIX = (".jar", ".png", ".webp", ".json")
SKIP_NAMES = {"LICENSE"}

files = subprocess.check_output(["git", "ls-files"], text=True).splitlines()
for path in files:
    if path in SKIP_NAMES or path.startswith(SKIP_PREFIX) or path.endswith(SKIP_SUFFIX):
        continue
    try:
        with open(path, errors="replace") as handle:
            text = handle.read()
    except OSError:
        continue
    for pattern in PATTERNS:
        if pattern.search(text):
            print(f"secret-like pattern in {path}", file=sys.stderr)
            sys.exit(1)
