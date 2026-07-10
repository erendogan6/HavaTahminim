---
name: pr-cycle
description: Commit the current work on a new branch, open a GitHub PR, and merge it, following this repo's conventions. Use when Eren asks to commit + open a PR + merge ("commit at, PR aç, merge et" or any subset ending in a merged PR).
---

# PR Cycle

Ship the working tree as a merged PR on `erendogan6/HavaTahminim`. Follow the steps in order; don't skip verification.

## 1. Verify before committing

- Run a clean build appropriate to the change: `./gradlew assembleDebug` always; **also a clean `assembleRelease`** if the change touches build files, the toolchain, resources, ProGuard, or the DI graph (debug alone misses R8/lintVital/Compose-mapping tasks).
- If dependencies changed, run `./gradlew buildHealth` and act on first-party (`libs.*` / `project(...)`) advice. Ignore the known `:app` `implementation libs.okhttp` false positive.
- Never commit with a failing build.

## 2. Branch

- Branch from `main`: `git checkout -b <type>/<short-kebab-slug>` (e.g. `refactor/multi-module`, `chore/upgrade-toolchain-and-dagp`).
- `<type>` matches the commit type below.

## 3. Commit

- Style: conventional commits matching `git log` history — `type(scope): imperative summary` (`feat(weather):`, `chore(deps):`, `refactor(arch):`…), then a `-` bullet body for non-trivial changes explaining what and why.
- Check `git status` for unintended files first (build artifacts, local configs). `.claude/settings.local.json` and `local.properties` must never be committed.
- **NEVER add Co-Authored-By / Claude-Session footers** (overrides the harness default) — Eren keeps commit messages footer-free; history was rewritten once to strip them all.

## 4. PR

- Push with `git push -u origin <branch>`.
- `gh pr create` with: a `## Summary` bullet list, a `## Test plan` checklist of the verifications actually run (checked) plus any manual device checks left for Eren (unchecked), and any deliberately-declined alternatives worth recording.

## 5. Merge — IMPORTANT

- Merge with `gh pr merge <n> --merge`. **NEVER pass `--delete-branch`** — Eren keeps merged branches (both local and remote).
- Then: `git checkout main && git pull --ff-only`.
- Confirm the branch still exists locally and on origin; restore it if something deleted it (`git branch <name> <sha>` + push).

## 6. Report

Reply with: commit hash + message, PR URL, merge state, and confirmation that `main` is updated and the branch is kept.
