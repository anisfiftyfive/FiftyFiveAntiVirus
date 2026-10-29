# FiftyFive Technologies — AI-Assisted Workflow Audit

droid-sentinel · host none (no origin) · default `main` (unborn) · HEAD none (0 commits) · 2026-10-29 · audited by Cursor Agent · prompt v2.0

Independent AI assessment. To be compared with the project lead's self-assessment form.

## Score summary

| Weighted Score | Score % | Security Avg | Maturity | Rated | Critical | High | AI tools detected |
|---|---|---|---|---|---|---|---|
| 2.83 | 57 | 2.25 | Defined | 11/11 | 0 | 4 | Cursor |

Biggest risk: uncommitted AI config with hooks that do not gate tests, secrets, or destructive git. Biggest score opportunity: link `docs/ARCHITECTURE.md` from `AGENTS.md` and add a directory map (P7 3→4 = +0.20).

Working-tree AI signals (3.0 baseline, all untracked): `AGENTS.md`, `.cursor/rules/*.mdc`, `.cursor/hooks.json`, `.cursor/commands/`, `.cursor/hooks/`. Tracked files = 0; `.gitignore` has no AI entries. 3.3 would be zero-signal on tracked/history only; working-tree files were scored.

## Scorecard

| # | Parameter | Weight | SEC | Score | Confidence | Why (≤ 15 words, with key evidence path) |
|---|---|---|---|---|---|---|
| 1 | Instructions & Memory | 15% | | 3 | High | `AGENTS.md` 38 lines; untracked; no lint cmd |
| 2 | Skills & Commands | 10% | | 4 | High | `.cursor/commands/` two lean workflows |
| 3 | MCP Servers & Plugins | 10% | ✓ | N/A | High | No `mcp.json` / plugins in tree |
| 4 | Guardrails & Hooks | 5% | ✓ | 2 | High | Hooks exist; lint/test/gitleaks/`--no-verify` absent |
| 5 | Permissions & Secrets | 5% | ✓ | 3 | High | No Cursor deny/sandbox; gitignore secrets |
| 6 | Context & Cost | 10% | | 3 | High | Lean always-on; no model/spend guidance |
| 7 | Knowledge Base & Docs | 20% | | 3 | High | `docs/` present; not linked from `AGENTS.md` |
| 8 | Verification & Testing | 10% | | 2 | High | Gradle test/build documented; no CI/e2e |
| 9 | Git & Review Workflow | 5% | ✓ | 2 | Low | No remote, CODEOWNERS, or attribution |
| 10 | Governance & Data Compliance | 5% | ✓ | 2 | High | Workflow docs; config uncommitted, no owner |
| 11 | Observability & Feedback | 5% | | 3 | Medium | `docs/AI_DECISIONS.md`; no telemetry |

P8 +1 vs ratio 1: agents can run checks (`AGENTS.md:24-25`). Hard caps: none.

## Security actions required

| Severity | P# | Finding (sub-check IDs) | Evidence (`file:line`, redacted) | Fix (exact file/setting) |
|---|---|---|---|---|
| High | 4 | No lint/test gates, approval list, pre-commit secrets, or `--no-verify` block (4.2, 4.3, 4.7, 4.8, 4.9) | `.cursor/hooks.json:4-16`; `scan-secrets.py:18-21` additional_context only | Add pre-commit gitleaks; Stop hook `./gradlew testDebugUnitTest`; deny `git --no-verify` |
| High | 5 | No Cursor allow/ask/deny, sandbox, or egress policy (5.1, 5.2, 5.9, 5.10) | No `.cursorignore`; no permissions config | Add `.cursorignore` + deny `local.properties`/`*.jks`; document sandbox |
| High | 9 | No AI attribution policy or CODEOWNERS (9.1, 9.4) | No `CODEOWNERS`; 0 commits | Add `.github/CODEOWNERS`; attribution section in `AGENTS.md` |
| High | 10 | AI config uncommitted; no owner, PR path, CI validate, cadence, or AI IP policy (10.1, 10.2, 10.3, 10.5, 10.7, 10.12) | `git ls-files` empty; no CI | First commit of `AGENTS.md`/`.cursor/`; CODEOWNERS; CI schema check; IP note in `docs/AI_WORKFLOW.md` |

## Changes required (priority order)

| # | Change | P# / sub-checks fixed | Effort (Quick ≤1d / Short ≤2w / Long) | Impact (e.g. "P7 2→4 = +0.40") |
|---|---|---|---|---|
| 1 | Commit `AGENTS.md`, `.cursor/`, `docs/`; gitignore `.cursor/*.local.md`, `CLAUDE.local.md` | 1.1, 1.9, 10.1 | Quick | P1 3→4 = +0.15 |
| 2 | Add `.github/CODEOWNERS` covering `AGENTS.md` `.cursor/` with owner + backup | 9.4, 10.2 | Quick | P9 2→4 = +0.10 |
| 3 | Extend `.cursor/hooks/protect-secrets.sh` to deny `git push --force`, `rm -rf`, `git --no-verify` | 4.4, 4.9 | Quick | P4 2→4 = +0.10 |
| 4 | Add `.pre-commit-config.yaml` gitleaks on staged files | 4.8 | Short | (included in P4) |
| 5 | Agent Stop/pre-commit: ktlint + `./gradlew testDebugUnitTest`; human-approval list in `AGENTS.md` | 4.2, 4.3, 4.7 | Short | (included in P4) |
| 6 | Add `.cursorignore` for `local.properties` `*.jks` `*.keystore`; Cursor deny/ask policy | 5.1, 5.2, 5.3, 5.9 | Short | P5 3→4 = +0.05 |
| 7 | `AGENTS.md` AI attribution (`Co-authored-by` / labels) | 9.1 | Quick | (included in P9) |
| 8 | Link `docs/ARCHITECTURE.md` from `AGENTS.md`; add directory map | 7.1, 7.4 | Quick | P7 3→4 = +0.20 |
| 9 | `.github/workflows/ci.yml` `testDebugUnitTest` + `assembleDebug`; document `./gradlew test --tests` | 8.2, 8.5, 1.4 | Short | P8 2→4 = +0.20 |
| 10 | `.github/pull_request_template.md` test-evidence checklist; CI lint of `AGENTS.md` | 8.8, 8.10, 10.5 | Short | P10 2→4 = +0.10 |
| 11 | `docs/AI_WORKFLOW.md`: model-per-task, dangerous modes, AI-generated IP | 6.4, 10.4, 10.12 | Quick | P6 3→4 = +0.10 |
| 12 | Document telemetry owner + `ai-bug` label in `docs/AI_WORKFLOW.md` | 11.1, 11.3 | Quick | P11 3→4 = +0.05 |

**Projected Weighted Score if all changes are done: 4.00 (Managed)**

## Sub-check evidence

P1 · Score 3 · Pass: 1.3 (38 lines), 1.5, 1.6, 1.7, 1.10 · Partial: 1.2 (no `@AGENTS.md`), 1.4 (test/build only `AGENTS.md:24-25`), 1.9 (no AI gitignore) · Fail: 1.1 (untracked), 1.11 (0 PRs) · Unknown: — · N/A: 1.8 · ratio 0.65 → 3 · caps: none
P2 · Score 4 · Pass: 2.1, 2.2 (run-checks + review-security), 2.5 · Partial: 2.3 (md titles, no frontmatter), 2.4, 2.6, 2.7 (only run-checks verifies) · Fail: — · Unknown: — · N/A: 2.8, 2.9 · ratio 0.71 → 4 · caps: none
P3 · Score N/A · no MCP/plugin config · caps: none
P4 · Score 2 · Pass: — · Partial: 4.1 (`.cursor/hooks.json` untracked), 4.4 (`protect-secrets.sh:5-10` git-add only), 4.5, 4.6 (`scan-secrets.py` additional_context) · Fail: 4.2, 4.3, 4.7, 4.8, 4.9 · Unknown: — · N/A: — · ratio 0.22 → 2 · caps: none (hooks+deny exist)
P5 · Score 3 · Pass: 5.4, 5.5 (empty tracked/history), 5.8, 5.11 · Partial: 5.3, 5.6 (`.gitignore:3,14-16,20`) · Fail: 5.1, 5.2, 5.9, 5.10 · Unknown: 5.7, 5.12 · N/A: — · ratio 0.50 → 3 · caps: none
P6 · Score 3 · Pass: 6.1 (59 always-on lines), 6.2 (`scanner-semantics.mdc:3`) · Partial: 6.5 (`droidsentinel.mdc:13`), 6.7 · Fail: 6.4, 6.6 · Unknown: 6.8 · N/A: 6.3 · ratio 0.50 → 3 · caps: none
P7 · Score 3 · Pass: 7.2 (`docs/AI_DECISIONS.md`), 7.5, 7.7 · Partial: 7.3 (`README.md:30-37`), 7.6 (`gradle/libs.versions.toml`) · Fail: 7.1 (arch not linked from AGENTS), 7.4 · Unknown: — · N/A: 7.8 · ratio 0.57 → 3 · caps: none
P8 · Score 2 · Pass: — · Partial: 8.1 · Fail: 8.2, 8.5, 8.7, 8.8, 8.9 (no androidTest), 8.10 · Unknown: 8.6 · N/A: 8.3, 8.4 · ratio 0.07 → 1 · +1 anchor (runnable Gradle) · caps: none
P9 · Score 2 · Pass: 9.8 · Partial: — · Fail: 9.1, 9.4 · Unknown: 9.2, 9.3, 9.5, 9.6 · N/A: 9.7 · ratio 0.33 → 2 · coverage 3/7 cap n/a · caps: none
P10 · Score 2 · Pass: 10.4 (`docs/AI_WORKFLOW.md` tools/data/MCP), 10.10 (5 unit tests, 0 PII hits), 10.11 · Partial: 10.6, 10.9 (`docs/SECURITY_MODEL.md:4-8` defined, not denied) · Fail: 10.1, 10.2, 10.3, 10.5, 10.7, 10.12 · Unknown: 10.8, 10.14 · N/A: 10.13 · ratio 0.36 → 2 · caps: none
P11 · Score 3 · Pass: 11.7 · Partial: 11.3, 11.5 (`docs/AI_DECISIONS.md`) · Fail: 11.1 · Unknown: 11.2, 11.4 · N/A: 11.6 · ratio 0.50 → 3 · caps: none

## Not verifiable from the repository

- 5.7 host secret scanning / push protection — no origin; `gh` token invalid
- 5.12 central/managed org policy — no org docs
- 6.8 spend tracking with alerts — no billing docs
- 8.6 CI required for merge — no host
- 9.2 default-branch protection — no origin; `gh` unauthenticated
- 9.3 agent/bot identities merge/push rights — no workflows/host
- 9.5 AI-attributed commit size — 0 commits
- 9.6 PR size median — no PRs
- 10.8 approved org accounts — no policy
- 10.14 transcript/data retention — no policy
- 11.2 cost spike alerts — no billing docs
- 11.4 outcome metrics split by AI work — no labels/host

## Method note

CLIs: local git only. `gh auth status` failed (invalid keyring token for 3100anre). `gitleaks`/`trufflehog` not installed; 5.5 is empty-history sample. Sampled: 5 unit-test files for 10.10; instruction line counts via `wc -l`. 90-day authors: none. Read-only audit; nothing was executed or modified except this report.
