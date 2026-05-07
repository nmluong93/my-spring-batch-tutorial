Analyze all staged and unstaged changes in the working tree, then commit them with a short, precise message that captures what actually changed.

Steps:
1. Run `git diff HEAD` and `git status` in parallel to understand all changes (staged and unstaged).
2. Stage all modified and new files: `git add -A` — but first warn the user and skip any `.env`, credential files, or secrets you spot.
3. Draft a commit message:
   - Format: `<type>: <what changed>` — keep it under 72 characters.
   - Types: `feat`, `fix`, `refactor`, `test`, `chore`, `docs`, `style`.
   - Focus on the *what* and *why*, not the *how*.
   - If changes span multiple concerns, use the dominant one and mention the rest in a short body (blank line after subject).
4. Show the user the drafted message and the list of files to be committed, then ask for confirmation before committing.
5. Once confirmed, commit using:
   ```
   git commit -m "$(cat <<'EOF'
   <message>

   Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
   EOF
   )"
   ```
6. Report the resulting commit hash and subject line.

If there is nothing to commit, say so clearly and stop.
