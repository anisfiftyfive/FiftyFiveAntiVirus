#!/usr/bin/env python3
import json
import re
import subprocess
import sys

payload = json.load(sys.stdin)
command = payload.get("command") or ""
plain = re.sub(r"'[^']*'", " ", command)
plain = re.sub(r'"[^"]*"', " ", plain)

def deny(user, agent):
    print(json.dumps({
        "permission": "deny",
        "user_message": user,
        "agent_message": agent,
    }))
    sys.exit(0)

def ask(user, agent):
    print(json.dumps({
        "permission": "ask",
        "user_message": user,
        "agent_message": agent,
    }))
    sys.exit(0)

if re.search(r"local\.properties|\.keystore\b|\.jks\b|keystore\.properties", command) and re.search(
    r"\bgit\b.*\b(add|commit|push)\b", command
):
    deny(
        "Refusing to stage or commit local.properties or keystore files.",
        "A project hook blocked a git command that might commit secrets.",
    )

if re.search(r"\bgit\b", plain) and re.search(r"--no-verify\b", plain):
    deny(
        "git --no-verify is blocked for agents.",
        "Do not bypass git hooks. Ask a human if a hook is wrong.",
    )

if re.search(r"\bgit\b", plain) and re.search(
    r"\bpush\b.*(--force|--force-with-lease|\s-f\b|\s-f$)", plain
):
    deny(
        "Force-push is blocked for agents.",
        "Do not force-push. Ask a human.",
    )

if re.search(r"\brm\s+-[a-zA-Z]*r[a-zA-Z]*f|\brm\s+-[a-zA-Z]*f[a-zA-Z]*r", command):
    deny(
        "Recursive force-delete is blocked for agents.",
        "rm -rf is denied. Ask a human for destructive cleanup.",
    )

if re.search(r"\b(DROP|TRUNCATE)\s+(TABLE|DATABASE)\b", command, re.I):
    deny(
        "Destructive SQL is blocked for agents.",
        "DROP/TRUNCATE is denied.",
    )

if re.search(r"\bterraform\s+destroy\b|\bkubectl\s+delete\b", command):
    deny(
        "Destructive infra commands are blocked for agents.",
        "terraform destroy and kubectl delete are denied.",
    )

if re.search(r"curl\s+[^\n|]*\|\s*(ba)?sh", command):
    deny(
        "Piping curl to a shell is blocked.",
        "Do not run curl | sh.",
    )

def current_branch():
    try:
        return subprocess.check_output(
            ["git", "rev-parse", "--abbrev-ref", "HEAD"],
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
    except Exception:
        return ""

branch = current_branch()
on_default = branch in {"main", "master"}
git_mutating = re.search(r"\bgit\b", plain) and re.search(r"\b(commit|push|merge)\b", plain)
push_default = re.search(r"\bgit\b", plain) and re.search(r"\bpush\b", plain) and re.search(
    r"\b(origin/)?(main|master)\b", plain
)
if (on_default and git_mutating) or push_default:
    deny(
        "Direct commits and pushes to main are blocked for agents. Use a feature branch and a pull request.",
        "Do not commit or push on main. Checkout a branch, push it, and open a PR into anisfiftyfive/FiftyFiveAntiVirus.",
    )

print(json.dumps({"permission": "allow"}))
