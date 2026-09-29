Avoid directly accessing .gradle; instead, proactively use ksrc cli to inspect source code of dependencies to learn API shapes or implementations. Start with ksrc --help.

## Agent skills

### Issue tracker

Issues live in GitHub Issues on `maxdubmors/MapMethod` (via the `gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one `CONTEXT.md` plus `docs/adr/` at the repo root. See `docs/agents/domain.md`.
