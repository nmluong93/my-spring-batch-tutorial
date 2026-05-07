---
name: merge-to
description: Merges the current git branch into a target branch specified as a parameter. Use this skill whenever the user says "merge to <branch>", "merge current branch into <branch>", "merge this branch to <branch>", or "merge into <branch>". Also trigger when the user wants to land their changes onto another branch. The parameter is the target branch name.
---

# merge-to

Merge the current branch into a target branch.

## Steps

1. **Capture state** — record the current branch name with `git branch --show-current`.

2. **Validate the target branch** — run `git fetch origin` then check if the target branch exists locally or remotely. If it doesn't exist at all, stop and tell the user clearly.

3. **Check for uncommitted changes** — run `git status --porcelain`. If there are uncommitted changes, warn the user and ask whether to stash them first, or abort. Don't proceed silently with a dirty working tree.

4. **Checkout the target branch** — `git checkout <target>`. If the branch only exists remotely, check it out with tracking: `git checkout -b <target> origin/<target>`.

5. **Merge** — run `git merge <source-branch> --no-ff` (no fast-forward keeps the merge commit for traceability). Use `--no-ff` by default; if the user explicitly asked for a fast-forward merge, use `git merge <source-branch>`.

6. **Handle merge conflicts** — if the merge exits non-zero:
   - Show the conflicting files with `git diff --name-only --diff-filter=U`.
   - Tell the user which files conflict and that they need to resolve them manually.
   - Do NOT attempt to auto-resolve conflicts.
   - Leave the repo in the mid-merge state so the user can resolve and commit.

7. **Report outcome** — on success, print a short summary: source branch, target branch, and the new HEAD commit hash (`git rev-parse --short HEAD`).

8. **Return to the original branch** — after a successful merge, check out the original branch again so the user lands back where they started. If the user explicitly asked to stay on the target branch, skip this step.

## Commit message for the merge

Use the default git merge message. Do not customize it unless the user asks.

## Example usage

```
/merge-to main
/merge-to develop
/merge-to release/1.2
```

## What NOT to do

- Don't push to remote unless the user explicitly asks.
- Don't rebase instead of merge.
- Don't skip the dirty-tree check.
- Don't amend or squash commits automatically.