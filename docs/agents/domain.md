# Domain Docs

## Layout

This repository uses a single-context layout:

- Root `CONTEXT.md`: the domain model and glossary.
- `docs/adr/`: architecture decisions.

## Before exploring the codebase

Read the root `CONTEXT.md` and ADRs relevant to the area you are working in.

If these documents do not exist, proceed silently: do not flag their absence
or suggest creating them upfront. The `/domain-modeling` skill creates them
as terms and decisions are agreed upon.

## Vocabulary

Use terms from `CONTEXT.md` in issues, proposals, hypotheses, and test names.

If a concept is missing, check whether the project actually uses it.
Note a real gap for `/domain-modeling`.

## ADR conflicts

If a proposal contradicts an existing ADR, explicitly identify
the decision number and explain why you propose revisiting it.
