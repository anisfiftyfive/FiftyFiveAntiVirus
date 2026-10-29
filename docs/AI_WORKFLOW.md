# AI workflow

DroidSentinel was specified in a shared ChatGPT thread and implemented in Cursor.

## How context is supplied

- `AGENTS.md` and `.cursor/rules/` are the durable instructions
- `.cursor/commands/` holds reusable review and check prompts
- Domain code is small enough to inspect without dumping the whole APK tree
- Hypatia was cloned to `/tmp` for source inspection; only documented constants live in this repo

## How work was split

1. Inspect Hypatia source and license
2. Scaffold Gradle / Compose
3. Implement hasher, BloomFilter scanner, metadata engine
4. Wire UI and history
5. Add tests, rules, hooks, docs
6. Run Gradle

## Cost control

- One coding agent
- No extra MCP servers added for scoring
- Remote model explanations are off; the in-app advisor is local and evidence-bound
