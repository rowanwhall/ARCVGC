#!/bin/bash
# Claude Code Stop hook: remind to review docs when non-doc source files were modified.
# Only outputs if there are unstaged/staged changes to non-documentation files.

cd "$(git rev-parse --show-toplevel 2>/dev/null)" || exit 0

# Collect modified files (staged + unstaged + untracked), exclude docs and config
changed_files=$(git diff --name-only HEAD 2>/dev/null; git diff --name-only --cached 2>/dev/null; git ls-files --others --exclude-standard 2>/dev/null)

# Filter to only source files (exclude docs/, CLAUDE.md, .claude/, *.md in root)
source_changes=$(echo "$changed_files" | grep -v '^$' | sort -u | grep -v -E '^(docs/|CLAUDE\.md|\.claude/|README\.md)')

if [ -z "$source_changes" ]; then
  exit 0
fi

# Check which doc files might be relevant based on changed paths
echo "Source files were modified this session. Review whether any docs in docs/ or CLAUDE.md need updating to reflect these changes."
echo ""
echo "Changed source files:"
echo "$source_changes" | head -20
echo ""
echo "Current docs: $(ls docs/)"
