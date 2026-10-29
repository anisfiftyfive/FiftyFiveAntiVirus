# AI workflow policy

DroidSentinel is developed in **Cursor** on GitHub `anisfiftyfive/FiftyFiveAntiVirus`. Use the GitHub account **anisfiftyfive** and the team Cursor account only. Do not copy this repo onto a personal shadow remote.

Canonical instructions: [`AGENTS.md`](../AGENTS.md). Cursor rules and commands must point at that file.

## Tools, MCP, dangerous modes, review

- Tooling: Cursor agent + `.cursor/commands/run-checks.md` and `review-security.md`. No extra MCP servers. Do not add `mcp.json`.
- Dangerous modes: never enable bypass / yolo / `dangerously-skip-permissions`. Hooks in `.cursor/hooks.json` are `failClosed`.
- Review: PRs use `.github/pull_request_template.md`. CODEOWNERS is `@anisfiftyfive`. Human review is required for scanner, GPG, permissions, and AI config. Agents must not commit or push on `main`; AI config (`AGENTS.md`, `.cursor/`, hooks) goes through a pull request.

## Data agents may see

Allowed: source, tests, docs, synthetic hashes in unit tests.
Denied: `local.properties`, keystores, `.env`, APKs, user inventories, Hypatia database blobs, production dumps. Enforced by `.cursorignore`, `beforeReadFile`, and gitignore. Do not paste those into chats.

## Model per task

- Cursor Auto / Composer: UI, glue, docs.
- A stronger model: scanner status mapping, GPG, BloomFilter adapter.
- No remote LLM for in-app advisor text. The advisor is local and evidence-bound.

## Session hygiene and cost

New chat per feature. Plan first for multi-file scanner work. Compact or start over when context is bloated. Gradle commands use `--quiet`. Owner `@anisfiftyfive` reviews Cursor billing weekly; no automated spend webhook is configured yet. Do not enable `OTEL_LOG_USER_PROMPTS`.

## Telemetry and failures

Usage telemetry for this project is Cursor’s account dashboard; owner `@anisfiftyfive`. Agent mistakes: GitHub label `ai-bug` plus a note in [`AI_DECISIONS.md`](AI_DECISIONS.md). AI-authored PRs: label `ai-generated`. Outcome: count those labels when reporting.

## IP

AI-generated code in this repository is contributed under AGPL-3.0-or-later. Do not paste proprietary third-party source. Humans remain the committers.

## Cadence and retention

Review `AGENTS.md`, hooks, CODEOWNERS, and this file at least every 90 days. Cursor transcripts stay in the local Cursor product and are not committed. Retention follows Cursor defaults.

## Git hooks

```
git config core.hooksPath .githooks
```

Optional: `pre-commit install` for gitleaks from `.pre-commit-config.yaml`.
