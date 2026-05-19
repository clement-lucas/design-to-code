---
name: generate-code
description: "Generate complete Spring Boot application code from extracted design document content. Use when: generate code from specs, create Spring Boot app from design, scaffold application from requirements, build entities controllers services from design docs, データモデルからコード生成."
---

# Generate Application Code from Design Documents

Transform extracted design document content into a complete, compilable Spring Boot application.

## When to Use

- After Phase 1 (extract-design-docs) has produced extracted text, images, and DrawingML
- When you have a consolidated view of all design specifications
- When building a new Spring Boot application from Japanese design documents

## Prerequisites

- Extracted text file at `project/extracted/extracted_text.txt` from the extract-design-docs skill
- Extracted images in `project/extracted/images/` for visual reference (screen mockups, ER diagrams)
- Understanding of the target technology stack (default: Spring Boot 3.x + Thymeleaf)

## Output Directory

Generate all application code into `project/generated-app/`. This is the working directory for the current project execution.

## Procedure

### Step 1: Analyze the Design Documents

Read through the extracted content systematically in this order:

1. **Data Model Design (データモデル設計)** — Table definitions, domains, ER relationships
   - Build a list of all tables, columns, types, constraints
   - Note primary keys, foreign keys, unique constraints
   - Map domain definitions to Java types

2. **Code Design (コード設計)** — Code masters, classification values
   - Identify enum-like values and status codes
   - Note code groups and their valid values

3. **Message Design (メッセージ設計)** — Screen and batch messages
   - Extract message IDs and text for `messages.properties`
   - Separate validation messages, info messages, error messages

4. **Screen Design (画面設計)** — Screen list and transitions
   - Map screen IDs (WA*) to their flow
   - Understand navigation paths

5. **Function Design (機能設計)** — Individual screen and batch specs
   - For each screen (WA*): fields, validations, events, request/response
   - For each batch (BA*): input, processing, output, scheduling

6. **Interface Design (インタフェース設計)** — File formats
   - CSV/TSV layouts for import/export
   - Header/detail record structures

7. **Job Flow Design (ジョブフロー設計)** — Batch orchestration
   - Job dependencies and execution order

### Step 2: Create Project Structure

Initialize a Maven project with this structure:

```
src/
├── pom.xml
└── src/main/
    ├── java/com/example/{appname}/
    │   ├── {AppName}Application.java
    │   ├── batch/           # Spring Batch job configs
    │   ├── config/          # Security, Web, Message configs
    │   ├── controller/      # Spring MVC controllers
    │   ├── entity/          # JPA entities
    │   ├── form/            # Form backing beans with validation
    │   ├── repository/      # Spring Data JPA repositories
    │   ├── security/        # UserDetails, UserDetailsService, event handlers
    │   └── service/         # Business logic services
    └── resources/
        ├── application.yml
        ├── messages.properties
        ├── schema.sql
        ├── data.sql
        ├── static/css/      # Stylesheets
        ├── static/js/       # JavaScript
        └── templates/       # Thymeleaf HTML templates
            ├── fragments/   # Shared layout fragments
            └── {feature}/   # Feature-specific templates
```

### Step 3: Generate pom.xml

Include these standard dependencies for a design-doc-based project:

```xml
<!-- Core -->
spring-boot-starter-web
spring-boot-starter-thymeleaf
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-security

<!-- Session -->
spring-session-jdbc

<!-- Batch (if batch specs exist) -->
spring-boot-starter-batch

<!-- Database -->
h2 (runtime, for local testing)
postgresql (runtime, for production)

<!-- UI -->
webjars-locator-core
bootstrap (webjars)
jquery (webjars)

<!-- Security integration for Thymeleaf -->
thymeleaf-extras-springsecurity6

<!-- Dev tools -->
spring-boot-devtools
```

### Step 4: Generate Database Layer

**schema.sql**: Create DDL for all tables from data model design.
- Use table/column definitions exactly as specified in design docs
- Respect column types, lengths, NOT NULL constraints
- Create tables in dependency order (referenced tables first)
- Add comments matching the Japanese column descriptions

**data.sql**: Generate seed data.
- Code master values from code design
- Initial admin accounts with VALID BCrypt hashes
- Business date initialization
- Use H2-compatible syntax (MERGE INTO instead of ON CONFLICT)

**Entities**: One `@Entity` class per table.
- Map column types per the [type mapping reference](./references/type-mapping.md)
- Add `@OneToMany`, `@ManyToOne`, `@OneToOne` relationships per ER diagram
- **CRITICAL**: Implement `java.io.Serializable` on all entities stored in session security context

### Step 5: Generate Repository Layer

Create a `@Repository` interface extending `JpaRepository` for each entity:
- Add custom query methods for search/filter operations defined in screen specs
- Use `@Query` with JPQL for complex queries
- Define projections for list/search screens that don't need full entity

### Step 6: Generate Service Layer

Create `@Service` classes for business logic:
- One service per major feature area (not per screen)
- Transaction management with `@Transactional`
- Validation logic beyond bean validation
- File upload/download processing

### Step 7: Generate Controller Layer

Create `@Controller` classes per feature area:
- Map URLs per screen transition design
- Use `@AuthenticationPrincipal` for logged-in user context
- PRG pattern: POST → redirect → GET for form submissions
- Confirmation screens: store form in session or hidden fields

Standard URL patterns:
```
GET  /feature/search         → Search screen
POST /feature/search         → Execute search
GET  /feature/create         → Create form
POST /feature/create/confirm → Confirm creation
POST /feature/create/execute → Execute creation
GET  /feature/{id}           → Detail view
GET  /feature/{id}/update    → Update form
POST /feature/{id}/update/confirm → Confirm update
POST /feature/{id}/update/execute → Execute update
```

### Step 8: Generate Templates

Create Thymeleaf HTML templates:
- Shared layout with `th:fragment` (header, footer, head)
- Use Bootstrap 5 grid system for layout
- `sec:authorize` for role-based visibility
- `th:errors` for field-level validation messages
- CSRF tokens in all forms

### Step 9: Generate Security Configuration

- Form-based login with custom login page
- BCrypt password encoding
- Role-based access control (from design doc role definitions)
- Session management with Spring Session JDBC
- Remember to permit static resources (`/css/**`, `/js/**`, `/webjars/**`)

### Step 10: Generate Batch Jobs (if applicable)

For each batch spec (BA*):
- Spring Batch `@Configuration` with Job and Step beans
- `FlatFileItemReader`/`FlatFileItemWriter` for CSV operations
- `JdbcPagingItemReader` for DB-sourced batches
- Proper chunk size and transaction management

## Lessons Learned (Common Pitfalls)

These are verified issues discovered during actual design-to-code builds:

1. **BCrypt hashes must be real** — Never fabricate BCrypt hash strings. Generate them programmatically using `BCryptPasswordEncoder.encode("password")`.

2. **Serializable entities** — When using `spring-session-jdbc`, the SecurityContext is serialized to the DB. `UserDetails` implementation and all referenced entities must implement `Serializable`.

3. **H2 PostgreSQL mode quirks** — Use `BYTEA` not `BLOB` for binary columns. Use `MERGE INTO` instead of `ON CONFLICT`. Use `FORMATDATETIME` instead of `TO_CHAR`.

4. **Table creation order** — Tables with foreign keys must be created after the referenced tables. Order your DDL carefully.

5. **Spring Batch type compatibility** — `JobBuilderFactory`/`StepBuilderFactory` are deprecated in Spring Batch 5.x. Use `JobBuilder`/`StepBuilder` with `JobRepository` parameter.

6. **Thymeleaf Security dialect** — `sec:authorize` and `#authentication` expressions require `thymeleaf-extras-springsecurity6` dependency.

## Output

After this skill completes:
- Complete Maven project with all source files
- All layers generated: entity → repository → service → controller → template
- Database schema and seed data
- Security configuration with working login
- Batch jobs (if design docs specify them)
