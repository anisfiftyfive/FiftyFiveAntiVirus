# AI-assisted workflow audit (evidence)

Scoring target: the workplace 11-parameter rubric. This file records **evidence**, not a guaranteed score.

| Parameter | Evidence in this repo |
| --- | --- |
| P1 Instructions and memory | `AGENTS.md`, `.cursor/rules/droidsentinel.mdc`, `.cursor/rules/scanner-semantics.mdc` |
| P2 Skills and commands | `.cursor/commands/run-checks.md`, `.cursor/commands/review-security.md` |
| P3 MCP / plugins | No extra MCP servers were installed. Browser tools were used only to read the ChatGPT share. Least privilege. |
| P4 Guardrails and hooks | `.cursor/hooks.json`, `protect-secrets.sh`, `scan-secrets.py` |
| P5 Permissions and secrets | `.gitignore` excludes `local.properties`, keystores, APKs. No API keys in source. |
| P6 Context and cost | Tasks were phased. Hypatia was inspected outside the repo. See `docs/AI_WORKFLOW.md`. |
| P7 Knowledge base | README + `docs/` + `THIRD_PARTY_NOTICES.md` |
| P8 Verification | Unit tests under `app/src/test`. Record actual Gradle output in `docs/QA_REPORT.md`. |
| P9 Git and review | Small files, security checklist command, no fabricated PRs |
| P10 Governance | `docs/SECURITY_MODEL.md`, AGPL license, database provenance |
| P11 Observability | Explicit scanner statuses, database errors, scan duration in results. No telemetry. |

Gradle evidence is in `docs/QA_REPORT.md`: 19/19 unit tests passed and `assembleDebug` produced `app/build/outputs/apk/debug/app-debug.apk`.
