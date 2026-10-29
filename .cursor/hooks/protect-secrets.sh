#!/bin/bash
input=$(cat)
command=$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("command",""))' <<<"$input")

if echo "$command" | grep -Eq 'git +(commit|add).*local\.properties|git +add +.*keystore'; then
  printf '%s\n' '{
    "permission": "deny",
    "user_message": "Refusing to stage or commit local.properties or keystore files.",
    "agent_message": "A project hook blocked a git command that might commit secrets."
  }'
  exit 0
fi

printf '%s\n' '{ "permission": "allow" }'
exit 0
