# Design-to-Code: GitHub Copilot Custom Agent for Automated Application Generation

A GitHub Copilot custom agent that transforms design documents (Excel/Word) into runnable Spring Boot applications. This repository demonstrates how to build custom agents and skills for GitHub Copilot to automate multi-phase software engineering workflows.

## What This Does

The `@design-to-code` agent reads software design documents (Excel, Word design documents) — screen specs, data models, batch definitions, interface layouts — and produces a complete, working Spring Boot application through a 3-phase workflow:

```
📄 Design Documents  →  🤖 Extract  →  🛠️ Generate  →  🏗️ Build & Run  →  🚀 Running App
   (Excel/Word)         Phase 1        Phase 2          Phase 3          (localhost:8080)
```

**Phase 1 — Extract**: Parses `.xlsx` and `.docx` files to extract text, tables, images, and DrawingML diagrams using Python scripts.

**Phase 2 — Generate**: Analyzes the extracted specifications and generates a full Spring Boot application — entities, repositories, services, controllers, Thymeleaf templates, security config, batch jobs, and SQL schemas.

**Phase 3 — Build & Run**: Compiles the project, iteratively fixes errors, and starts the application.

## Repository Structure

```
.github/
├── agents/
│   └── design-to-code.agent.md       # Custom agent definition
├── skills/
│   ├── extract-design-docs/           # Skill: parse Office documents
│   │   ├── SKILL.md
│   │   └── scripts/
│   │       ├── extract_text.py        # Text extraction from xlsx/docx
│   │       ├── extract_images.py      # Image extraction (mockups, ER diagrams)
│   │       └── extract_drawingml.py   # DrawingML shape/diagram extraction
│   ├── generate-code/                 # Skill: generate Spring Boot app
│   │   ├── SKILL.md
│   │   └── references/
│   │       ├── type-mapping.md        # Japanese type → SQL → Java mapping
│   │       └── code-templates.md      # Spring Boot code patterns
│   └── build-and-run/                 # Skill: build, fix errors, run
│       └── SKILL.md
└── copilot-instructions.md            # Workspace-level Copilot instructions

samples/
├── design-docs/                       # Sample input: 53 Japanese design documents
│   └── A1_プロジェクト管理システム/
│       ├── 010_要件定義/              # Requirements & screen designs
│       ├── 020_方式設計/              # Architecture & dev standards
│       ├── 030_アプリ設計/            # Application design (screens, batch, DB)
│       ├── 080_ツール/                # Tools
│       └── 100_レビュー記録/          # Review records
└── generated-app/                     # Sample output: generated Spring Boot app
    ├── pom.xml
    └── src/main/
        ├── java/com/example/proman/   # 46 Java source files
        │   ├── batch/                 # Spring Batch job configurations
        │   ├── config/                # Security, Web, Message configs
        │   ├── controller/            # MVC controllers
        │   ├── entity/                # JPA entities (10 tables)
        │   ├── form/                  # Form beans with validation
        │   ├── repository/            # Spring Data JPA repositories
        │   ├── security/              # UserDetails, auth handlers
        │   └── service/               # Business logic
        └── resources/
            ├── schema.sql             # DDL for 17 tables
            ├── data.sql               # Seed data with BCrypt hashes
            ├── templates/             # 19 Thymeleaf HTML templates
            └── static/                # CSS and JavaScript
```

## How It Works

### Custom Agent (`design-to-code.agent.md`)

The agent file defines the orchestrator that coordinates the 3-phase workflow. It specifies:
- **Description** — When Copilot should activate this agent (`@design-to-code`)
- **Tools** — Permissions for file I/O, terminal execution, search, and sub-agent delegation
- **Workflow** — The ordered phases and what each skill handles
- **Constraints** — Rules to prevent common errors (e.g., never fabricate BCrypt hashes)

### Skills

Each skill is a self-contained knowledge module with a `SKILL.md` file and optional supporting scripts/references:

| Skill | Purpose | Key Assets |
|-------|---------|------------|
| `extract-design-docs` | Parse Office files to extract specs | 3 Python scripts for text, images, and DrawingML |
| `generate-code` | Transform specs into Spring Boot code | Type mapping tables, code templates |
| `build-and-run` | Compile, fix errors, start the app | Error reference table with common fixes |

### Workspace Instructions (`copilot-instructions.md`)

Provides global context to Copilot about the technology stack, design document conventions, and critical rules that apply across all interactions in the workspace.

## Getting Started

### Prerequisites

- [VS Code](https://code.visualstudio.com/) with [GitHub Copilot](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot) extension
- GitHub Copilot subscription (Individual, Business, or Enterprise)
- Python 3.x (for document extraction scripts)
- JDK 17+ and Maven 3.9+ (for building the generated app)

### Try It Yourself

1. **Clone this repository**:
   ```bash
   git clone https://github.com/<your-username>/design-to-code.git
   cd design-to-code
   ```

2. **Open in VS Code**:
   ```bash
   code .
   ```

3. **Invoke the agent** in Copilot Chat:
   ```
   @design-to-code Please read the design documents in samples/design-docs/ and generate the application
   ```

4. The agent will execute the 3-phase workflow automatically:
   - Extract all 53 design documents
   - Generate a complete Spring Boot project
   - Build and start the application on `http://localhost:8080`

### Run the Sample Generated App Directly

If you want to run the pre-generated sample application without invoking the agent:

```bash
cd samples/generated-app
# Set JAVA_HOME to JDK 17+
mvn spring-boot:run
```

Then open `http://localhost:8080` and log in with `admin` / `password`.

## Creating Your Own Design-to-Code Agent

You can adapt this pattern for your own projects:

### 1. Define the Agent

Create `.github/agents/<name>.agent.md` with YAML frontmatter:

```yaml
---
description: "When to activate this agent"
tools: [read, edit, search, execute, todo, agent]
---
```

The body describes the workflow, capabilities, and constraints.

### 2. Create Skills

Create `.github/skills/<skill-name>/SKILL.md` for each phase. Each skill should include:

- **When to Use** — Trigger conditions
- **Prerequisites** — Required tools/context
- **Procedure** — Step-by-step instructions the agent follows
- **Output** — Expected artifacts

### 3. Add Workspace Instructions

Create `.github/copilot-instructions.md` with project-wide context: tech stack, naming conventions, critical rules.

### 4. Include Supporting Assets

Add scripts, templates, or reference docs alongside skills. The agent can execute scripts and reference docs during its workflow.

## Design Document Format

The sample design documents follow a standard Japanese software engineering documentation structure:

| Category | ID Pattern | Content |
|----------|-----------|---------|
| Screen Design (画面設計) | WA1xxxx | Screen specs, UI mockups, field validations |
| Batch Design (バッチ設計) | BA1xxxx | Batch jobs, I/O, scheduling, error handling |
| Interface Design (インタフェース設計) | N21AAxx | File formats, data mapping, import/export |
| Data Model (データモデル設計) | — | Table definitions, ER diagrams, domains |
| Message Design (メッセージ設計) | — | Screen/batch messages, error codes |
| Code Design (コード設計) | — | Code masters, enum-like classifications |
| Job Flow (ジョブフロー設計) | — | Batch orchestration, dependencies |

## Technology Stack (Generated App)

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| Language | Java 17 |
| Template Engine | Thymeleaf |
| Security | Spring Security 6.x |
| Database | H2 (dev) / PostgreSQL (prod) |
| ORM | Spring Data JPA |
| Batch | Spring Batch 5.x |
| Build | Maven |
| Session | Spring Session JDBC |

## Key Lessons Learned

These are patterns discovered during development that are encoded into the agent's skills:

1. **BCrypt hashes must be real** — LLMs tend to fabricate hash strings. The agent generates hashes using `BCryptPasswordEncoder.encode()`.
2. **Entities in security context must be `Serializable`** — Spring Session JDBC serializes the `SecurityContext`.
3. **H2 and PostgreSQL syntax differ** — `MERGE INTO ... KEY(...)` instead of `ON CONFLICT`, `BYTEA` instead of `BLOB`.
4. **DDL order matters** — Referenced tables must be created before referencing tables in `schema.sql`.
5. **Spring Batch 5.x dropped factory classes** — Use `new JobBuilder(name, jobRepository)` instead of `JobBuilderFactory`.
6. **DrawingML in Excel** — Architecture diagrams are often embedded as DrawingML XML, not images. Standard extraction misses them.

## License

This project is provided as a demonstration of GitHub Copilot custom agent capabilities. The sample design documents are adapted from publicly available Japanese software engineering templates.
