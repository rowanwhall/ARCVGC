---
name: ship
description: Review uncommitted changes for quality issues, fix agreed-upon findings, and update documentation. Use when finishing a feature or before committing.
disable-model-invocation: true
---

# Ship Review Workflow

## Step 1: Review

Launch a review subagent (Agent tool, subagent_type: general-purpose) to review all uncommitted changes. The agent should:

1. Run `git diff` to see all uncommitted changes
2. Review every changed file for:
   - Unused imports
   - Dark mode violations (hardcoded `Color.Black`/`Color.White` — see `.claude/rules/code-quality.md`)
   - Missing previews (`@Preview` / `#Preview` blocks)
   - Consistency with existing codebase patterns
   - Bugs or edge cases (nil handling, empty collections, off-by-one errors)
   - SwiftUI and Compose best practices
   - Compiler warnings (redundant `!!`, `println()` debug statements)
   - Spelling: user-facing "Pokemon" must use accented "Pokémon"
3. Report findings clearly, categorized by severity (bugs > quality > style)
4. The agent must NOT make any edits — research only

## Step 2: Triage

Present the agent's findings to the user. For each finding, indicate whether it's a bug, quality issue, or style nit. Ask the user which items they want addressed.

## Step 3: Fix

Fix the agreed-upon issues. After fixes, verify the build still succeeds if Swift or Kotlin files were modified.

## Step 4: Update documentation

Review the docs listed in CLAUDE.md's "Detailed Documentation" table and determine whether any docs or CLAUDE.md itself need updating to reflect the uncommitted changes. Specifically check:

- `CLAUDE.md` — Key Screens, Architecture, API sections
- `docs/` — Any doc whose domain area was touched
- `.claude/rules/` — Coding conventions, code quality rules

Update any docs that are now stale. Do NOT update docs for changes that are purely internal refactors with no architectural or behavioral impact.
