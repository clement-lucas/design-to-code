# Design-to-Code Workspace Instructions

This workspace contains a **design-to-code** system that transforms Japanese design documents (Excel/Word) into runnable Spring Boot applications.

## Custom Agent

Use `@design-to-code` agent for the full workflow. It orchestrates three phases:
1. **Extract** — Parse Office documents to extract specifications, images, and diagrams
2. **Generate** — Build a complete Spring Boot application from the extracted specs
3. **Build & Run** — Compile, fix errors, and start the application

## Design Document Conventions

- Design documents are stored in `samples/design-docs/` with Japanese folder names
- Document IDs follow patterns: `WA*` (screens), `BA*` (batch), `N21AA*` (interfaces)
- All text content is in Japanese (UTF-8)

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| Language | Java 17 |
| Template Engine | Thymeleaf |
| Security | Spring Security 6.x |
| Database | H2 (dev), PostgreSQL (prod) |
| ORM | Spring Data JPA |
| Batch | Spring Batch 5.x |
| Build | Maven |
| Session | Spring Session JDBC |

## Critical Rules

1. **Never fabricate BCrypt hashes** — Always generate real hashes using `BCryptPasswordEncoder.encode()`
2. **Entities in security context must be Serializable** — Spring Session JDBC serializes the SecurityContext
3. **H2 compatibility** — Use `MERGE INTO ... KEY(...)` not `ON CONFLICT`, use `BYTEA` not `BLOB`
4. **Schema DDL order matters** — Create referenced tables before referencing tables
5. **Spring Batch 5.x API** — Use `JobBuilder`/`StepBuilder` with `JobRepository`, not deprecated factory classes
