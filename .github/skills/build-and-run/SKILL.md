---
name: build-and-run
description: "Build, fix compilation errors, and run a Spring Boot application generated from design documents. Use when: build Spring Boot app, fix compilation errors, run application, mvn compile, start server, fix runtime errors, H2 database issues, BCrypt hash errors, Serializable errors."
---

# Build and Run the Application

Build the generated Spring Boot application, diagnose and fix errors iteratively until it compiles and starts successfully.

## When to Use

- After Phase 2 (generate-code) has created the full project source
- When a generated project has compilation errors
- When fixing runtime startup failures
- When setting up the development environment (JDK, Maven)

## Prerequisites

- A generated Maven project with `pom.xml`, `src/main/java/`, `src/main/resources/`
- JDK 17+ installed (or will be installed in Step 1)
- Maven 3.9+ installed (or will be installed in Step 1)

## Procedure

### Step 1: Ensure JDK and Maven Are Installed

Check availability:
```bash
java -version
mvn -version
```

If not available, install them:
- **JDK 17**: Download from Adoptium/Eclipse Temurin. On Windows, use the `.zip` distribution and set `JAVA_HOME` and `PATH`.
- **Maven 3.9.x**: Download the binary `.zip` from maven.apache.org, extract, and add `bin/` to `PATH`.

Verify the setup:
```bash
java -version   # Should show 17+
mvn -version    # Should show 3.9+ and correct JDK
```

### Step 2: First Compilation Attempt

Navigate to the project root (where `pom.xml` is) and compile:
```bash
mvn clean compile -e
```

This will download dependencies and attempt compilation. Expect errors on the first run — proceed to Step 3.

### Step 3: Fix Compilation Errors (Iterative)

Common compilation errors and their fixes:

**1. Missing imports**
- Error: `cannot find symbol`
- Fix: Add the correct import statement. Search for the class in the dependency tree.

**2. Deprecated Spring Batch API**
- Error: `JobBuilderFactory`/`StepBuilderFactory` not found
- Fix: Use `new JobBuilder(name, jobRepository)` and `new StepBuilder(name, jobRepository)` instead.
- Spring Batch 5.x removed the factory classes.

**3. Type mismatches in Batch configs**
- Error: incompatible types in `ItemReader`/`ItemWriter`/`ItemProcessor`
- Fix: Ensure generic types match across the reader→processor→writer chain.

**4. Missing Spring Security methods**
- Error: methods not found on `HttpSecurity`
- Fix: Spring Security 6.x uses lambda-style configuration. Use `.authorizeHttpRequests(auth -> auth.requestMatchers(...))` not `.authorizeRequests().antMatchers(...)`.

**5. Thymeleaf Security dialect errors**
- Error: `sec:authorize` not resolving
- Fix: Add `thymeleaf-extras-springsecurity6` dependency.

Repeat `mvn clean compile -e` after each fix until compilation succeeds.

### Step 4: Package and Run

Once compilation succeeds:
```bash
mvn clean package -DskipTests -e
```

Then start the application:
```bash
mvn spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/{app-name}-0.0.1-SNAPSHOT.jar
```

### Step 5: Fix Runtime Errors (Iterative)

Common runtime errors after startup:

**1. H2 Schema Errors**
- Error: `Table "X" not found` or `Column "X" not found`
- Fix: Check `schema.sql` table creation order. Tables referenced by foreign keys must be created first.
- Fix: Check for H2/PostgreSQL syntax differences (`BYTEA` not `BLOB`, `CLOB` not `TEXT`).

**2. Invalid BCrypt Hashes**
- Error: Login always fails with correct credentials
- Fix: BCrypt hashes in `data.sql` must be real. Generate proper hashes:
  ```java
  System.out.println(new BCryptPasswordEncoder().encode("password"));
  ```
  A valid BCrypt hash looks like: `$2a$10$` followed by 53 base64 characters.

**3. NotSerializableException**
- Error: `java.io.NotSerializableException: com.example.entity.SomeEntity`
- Fix: Add `implements Serializable` and `serialVersionUID` to:
  - The `UserDetails` implementation
  - The entity stored inside `UserDetails` (e.g., `SystemAccount`)
  - Any entity referenced by the above (e.g., `Users`)

**4. MERGE INTO syntax error (H2 seed data)**
- Error: Syntax error in `data.sql` on `INSERT ... ON CONFLICT`
- Fix: Use H2-compatible upsert syntax:
  ```sql
  MERGE INTO table_name (col1, col2) KEY(col1) VALUES ('val1', 'val2');
  ```

**5. Spring Session table conflict**
- Error: `Table "SPRING_SESSION" already exists`
- Fix: Set `spring.session.jdbc.initialize-schema: always` and don't create the table in `schema.sql`.

**6. Spring Batch table conflict**
- Error: Batch metadata tables already exist
- Fix: Set `spring.batch.jdbc.initialize-schema: always` and remove batch tables from `schema.sql`.

### Step 6: Verify Application

Once the application starts successfully:

1. **Open browser**: Navigate to `http://localhost:8080`
2. **Verify login**: Should redirect to login page. Test with initial credentials (e.g., `admin`/`password`)
3. **Test navigation**: Check that all major screens load without errors
4. **Check H2 console**: Navigate to `http://localhost:8080/h2-console` to verify database tables

### Step 7: Document Results

After successful startup, report:
- Application URL and port
- Login credentials
- List of available screens/features
- Any known limitations or remaining TODO items

## Error Reference

| Error Pattern | Root Cause | Fix |
|-------------|-----------|-----|
| `cannot find symbol` | Missing import or incorrect class name | Add correct import |
| `JobBuilderFactory not found` | Spring Batch 5.x API change | Use `JobBuilder` with `JobRepository` |
| `Table not found` | DDL order or H2 syntax | Fix creation order, use H2-compatible SQL |
| `NotSerializableException` | Spring Session JDBC serialization | Add `Serializable` to entity chain |
| `Login always fails` | Fabricated BCrypt hash | Generate real hash with `BCryptPasswordEncoder` |
| `ON CONFLICT syntax error` | PostgreSQL-only syntax in H2 | Use `MERGE INTO ... KEY(...)` |
| `Circular view path` | Controller returns same name as mapping | Use explicit view name with directory prefix |
| `No qualifying bean` | Missing `@Service`/`@Repository`/`@Component` | Add appropriate annotation |
| `LazyInitializationException` | Accessing lazy-loaded relation outside session | Use `@Transactional` or `JOIN FETCH` |

## Output

After this skill completes:
- Application running on localhost:8080
- All compilation and runtime errors resolved
- Login functional with test credentials
- Database initialized with seed data
