# Design to Code — VS Code Extension

A GitHub Copilot Chat participant (`@design-to-code`) that transforms Japanese design documents (Excel/Word) into runnable Spring Boot applications.

## Setup

```bash
cd vscode-extension
npm install
npm run compile
```

Press **F5** in VS Code to launch the Extension Development Host.

## Usage

Open the Copilot Chat panel and invoke the participant:

| Command | What it does |
|---------|-------------|
| `@design-to-code /extract` | Phase 1 — Parses all `.xlsx`/`.docx` files in the workspace and writes output to `extracted/` |
| `@design-to-code /generate` | Phase 2 — Reads `extracted/extracted_text.txt` and generates a Spring Boot app |
| `@design-to-code /generate output directory: path/to/dir` | Generate into a custom output path |
| `@design-to-code /help` | Show workflow overview |

After generation, build and run the app:

```bash
cd src/generated-app   # or your chosen output dir
mvn spring-boot:run
```

## Prerequisites

| Tool | Purpose |
|------|---------|
| Python 3.x | Document extraction scripts |
| JDK 17+ | Compile the generated Spring Boot app |
| Maven 3.9+ | Build the generated app |
| GitHub Copilot subscription | Language model access |

## Project Structure

```
vscode-extension/
├── package.json          # Extension manifest — declares @design-to-code participant
├── tsconfig.json
├── .vscodeignore
└── src/
    ├── extension.ts      # Activation entry point
    ├── participant.ts    # Chat participant registration and routing
    └── phases/
        ├── extract.ts    # Phase 1: run Python extraction scripts
        └── generate.ts   # Phase 2: call LM to generate Spring Boot code
```

## Packaging for Distribution

```bash
npm install -g @vscode/vsce
vsce package          # produces design-to-code-0.1.0.vsix
code --install-extension design-to-code-0.1.0.vsix
```

To publish to the VS Code Marketplace, update `publisher` in `package.json` and run `vsce publish`.
