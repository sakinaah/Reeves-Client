# Contributing to Reeves Client

Thanks for your interest in improving Reeves Client! Contributions of all kinds
are welcome — bug fixes, new informational features, documentation, and more.

## Ground Rules (Compliance)

Reeves Client is an **informational** companion client. Every contribution must
follow these rules — PRs that violate them will be closed regardless of code quality:

- **No automation** of any kind (combat, farming, mining, fishing, movement, clicking).
- **No macros**, auto-clickers, or scripted gameplay actions.
- **No aim assistance**, reach modification, or velocity modification.
- **No packet manipulation** or server-interaction spoofing.
- **No hidden-information exploitation** (X-ray, seeing through walls, etc.).
- **No fully automated puzzle/dungeon solving.**
- Nothing that plays the game for the user.

All features must require direct player input and only provide visual or
informational assistance.

## Development Setup

1. Fork and clone the repository.
2. Run `./gradlew genSources` to generate Minecraft sources.
3. Open the project in IntelliJ IDEA (File → Open → select the project root).
4. Use the `Minecraft Client` run configuration to test in-game.

Requires **Java 21**.

## Submitting Changes

1. Create a branch off `main` for your change.
2. Keep the code style consistent with the surrounding files.
3. Build and verify it compiles: `./gradlew build`.
4. Test in-game on Minecraft 1.21.10 before opening a PR.
5. Open a pull request with a clear description of what changed and why.

## Reporting Bugs

Open an issue and include:

- Your Minecraft version, Fabric Loader version, and Reeves Client version.
- Steps to reproduce.
- The full crash report or log (from `.minecraft/crash-reports/` or the launcher console), inside a code block.

By contributing, you agree that your contributions are licensed under the
project's [MIT License](LICENSE).
