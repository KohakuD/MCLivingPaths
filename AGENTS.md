# AGENTS.md

These instructions apply to the entire MCLivingPath repository.

## Repository boundaries

- Treat MCLivingPath as an independent Git and implementation scope.
- Kohaku repositories and other Minecraft repositories may be read as references when relevant, but must not be modified, staged, or committed as part of an MCLivingPath task without an explicit separate request.
- Keep Kohaku and Minecraft work organizationally separate at all times.

## Codex model routing

- Prefer GPT-5.3-Codex-Spark (Spark) for clearly bounded, sufficiently specified implementation work, especially small bugs, UI polish, strings and documentation, builds and tests, and small refactors.
- Use a stronger Codex model for architecture decisions, large or new systems, cross-project analysis, hard-to-localize bugs, and extensive refactors.
- If a Spark task becomes unexpectedly complex or Spark cannot find a clean solution after reasonable analysis, stop and recommend switching to a stronger Codex model instead of continuing speculatively.
- For roadmaps, use a stronger Codex model for planning and larger architecture work; use Spark for clearly defined sub-items and their build and test steps.
- Model routing does not expand the requested scope or override any repository-specific instruction.
