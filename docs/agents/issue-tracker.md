# Issue tracker: GitHub

Issues and specs for this project live in GitHub Issues in maxdubmors/MapMethod.
Use the `gh` CLI from the repository directory; it infers the repository from git remote.

## Operations

- Create an issue: `gh issue create --title "..." --body-file <file>`.
- Read an issue with labels and discussion:
  `gh issue view <number> --json number,title,body,labels,comments`.
- List open issues:
  `gh issue list --state open --json number,title,body,labels,comments`.
  Add a `--label` filter when needed.
- Add a comment: `gh issue comment <number> --body-file <file>`.
- Apply or remove a label:
  `gh issue edit <number> --add-label "..."` / `--remove-label "..."`.
- Close an issue: `gh issue close <number>`.

For multiline text, pass a file with `--body-file`.

## Pull requests as a triage surface

**PRs as a request surface: no.**

## Skill instructions

- "Publish to the issue tracker": create a GitHub issue.
- "Fetch the relevant ticket": read the issue and its comments.
- If a reference could identify a PR, resolve its type with
  `gh pr view <number>`; use `gh issue view` for an issue.

## Wayfinding operations

Rules for `/wayfinder`:

- The map is a single issue labelled `wayfinder:map` with
  Notes / Decisions-so-far / Fog sections.
- Link child tickets to the map through GitHub sub-issues.
  If unavailable, use a task list in the map body and
  a `Part of #<map>` line in each child ticket.
- The ticket type is recorded with a `wayfinder:<type>` label, where type is
  research, prototype, grilling, or task.
- Record blockers through native issue dependencies.
  If unavailable, use a `Blocked by: #<n>, #<n>` line.
  A ticket is unblocked when all its blockers are closed.
- The next ticket is the first open child in map order
  with no open blockers and no assignee.
- Before starting work, assign the ticket to yourself:
  `gh issue edit <number> --add-assignee @me`.
- After resolving it, add the answer as a comment, close the ticket,
  and append a short result with a link to the map's Decisions-so-far.
