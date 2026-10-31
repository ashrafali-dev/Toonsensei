# UI/UX Pro Max — Codex project skill

- Upstream: https://github.com/nextlevelbuilder/ui-ux-pro-max-skill
- Skill version source: UI/UX Pro Max v2.5.0 CLI asset set
- Codex discovery path: `.agents/skills/ui-ux-pro-max/SKILL.md`
- Install scope: repository/project (not the user-level `$HOME/.agents/skills` location)
- Search runner: `python3 .agents/skills/ui-ux-pro-max/scripts/search.py`
- Local catalog: `data/*.csv`
- ToonSensei-specific platform constraints: `references/toonsensei-android-views.md`

The official UI/UX Pro Max instructions, BM25 search code, design-system generator, reasoning contract, UX references, and catalog CSVs are stored in this folder. The repository does not add the CLI package as a runtime/build dependency.

Codex discovers repository skills from `.agents/skills`. If this skill is not visible after checking out the branch, restart Codex or refresh its skill list.

Note: The current execution environment does not contain the Codex CLI. The project-scoped skill files have been installed into the repository branch, but loading/activation in the owner's separate Codex client must be verified there.
