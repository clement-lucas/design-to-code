---
name: test-automation
description: "Automatically create test specifications, generate test code, execute tests, and review results for Spring Boot applications. Use when: create tests, run tests, test automation, テスト自動化, 単体テスト, 結合テスト, システムテスト, UI テスト, 画面操作テスト, スクリーンショット, テスト仕様書作成, テストコード作成, テスト実施."
---

# Test Automation — テスト自動化

Automatically generate test specifications, create test code, execute tests, and verify results for generated Spring Boot applications. Covers unit tests (単体テスト), integration tests (結合テスト), system tests (システムテスト), and UI/screen operation tests (画面操作テスト).

## When to Use

- After Phase 3 (build-and-run) has produced a running application
- When the user requests test creation for a generated project
- When the user wants to validate generated code against design specifications

## Prerequisites

- A generated and compilable Spring Boot project with `pom.xml`
- Design documents (extracted text) available for deriving test cases
- Test specification documents in `030_アプリ設計/110_テスト仕様書/` (if available)
- JDK 17+ and Maven 3.9+ installed

## Test Scope

| テスト種別 | English | Scope | Framework |
|-----------|---------|-------|-----------|
| 単体テスト | Unit Test | Individual classes: Service, Repository, Entity, Batch | JUnit 5 + Mockito |
| 結合テスト | Integration Test | Controller + Service + Repository together, HTTP layer | Spring Boot Test + MockMvc |
| システムテスト（機能テスト） | System/Functional Test | End-to-end screen flows with authentication | Spring Boot Test + MockMvc + Spring Security Test |
| システムテスト（業務スルー） | Business Flow Test | Complete business scenarios across multiple screens | Spring Boot Test + TestRestTemplate |
| UI/画面操作テスト | UI/Screen Operation Test | Browser-based visual verification with screenshots | Playwright (VS Code browser tools) |

## Procedure

### Phase 1: テスト仕様書作成 (Test Specification Creation)

Create a structured test specification document for each test level.

#### Step 1.1: Analyze Source Code and Design Docs

1. **Read all source code** in the project:
   - Entities in `entity/` — identify fields, constraints, relationships
   - Services in `service/` — identify business methods, validations, transactions
   - Controllers in `controller/` — identify endpoints, request mappings, form validations
   - Repositories in `repository/` — identify custom queries
   - Batch configs in `batch/` — identify jobs, steps, readers, writers
   - Security config in `config/` — identify access rules, login flow

2. **Read design documents** (if available):
   - Test specification documents in `110_テスト仕様書/`
   - Screen specifications (WA*) for expected behavior
   - Batch specifications (BA*) for expected processing

3. **Read existing schema and seed data**:
   - `schema.sql` — table structures for test data setup
   - `data.sql` — seed data patterns to replicate in tests

#### Step 1.2: Generate Test Specification

Create `test-spec.md` in the project root with the following structure:

```markdown
# テスト仕様書 — {Application Name}

## 1. 単体テスト仕様 (Unit Test Specification)

### 1.1 Service層テスト
| No. | テスト対象クラス | テストメソッド | テスト条件 | 期待結果 | 区分 |
|-----|----------------|-------------|----------|---------|------|

### 1.2 Repository層テスト
| No. | テスト対象クラス | テストメソッド | テスト条件 | 期待結果 | 区分 |

### 1.3 Entity/Validationテスト
| No. | テスト対象クラス | テスト項目 | テスト条件 | 期待結果 | 区分 |

### 1.4 Batch処理テスト
| No. | テスト対象Job | テストステップ | テスト条件 | 期待結果 | 区分 |

## 2. 結合テスト仕様 (Integration Test Specification)

### 2.1 Controller結合テスト
| No. | テスト対象URL | HTTPメソッド | テスト条件 | 期待結果 | 区分 |

### 2.2 認証・認可テスト
| No. | テスト対象URL | ロール | テスト条件 | 期待結果 | 区分 |

## 3. システムテスト仕様 (System Test Specification)

### 3.1 機能テスト
| No. | 機能ID | 機能名 | テストシナリオ | 期待結果 | 区分 |

### 3.2 業務スルーテスト
| No. | シナリオ名 | 操作手順 | 期待結果 | 区分 |
```

For each test case, populate:
- **テスト条件**: Input data, preconditions, user role
- **期待結果**: Expected HTTP status, response content, DB state changes, error messages
- **区分**: 正常 (normal), 異常 (error), 境界値 (boundary)

#### Step 1.3: Test Case Derivation Rules

Apply these rules to systematically derive test cases:

**For Services:**
- Normal case: valid input → expected output
- Null/empty input → appropriate handling
- Not-found case: invalid ID → Optional.empty or exception
- Boundary values: pagination limits, max-length strings

**For Controllers:**
- GET request → 200 + correct view name
- POST with valid form → redirect to completion
- POST with validation errors → return to form with errors
- POST with back parameter → return to input form
- Unauthenticated access → redirect to login
- Wrong role access → 403 Forbidden

**For Batch Jobs:**
- Normal execution → job completes COMPLETED
- Empty input → job completes with 0 items processed
- Invalid data rows → appropriate skip/error handling

**For Security:**
- Login with valid credentials → success redirect
- Login with invalid credentials → error message
- Access permitted URLs per role
- CSRF token validation

---

### Phase 2: テスト仕様書レビュー (Test Specification Review)

#### Step 2.1: Coverage Check

Verify the test specification covers:
- [ ] All public methods in Service classes
- [ ] All endpoints (GET/POST) in Controller classes
- [ ] All custom Repository query methods
- [ ] All Batch Jobs and Steps
- [ ] All validation rules on Form classes
- [ ] All security access rules
- [ ] Normal / Error / Boundary cases for each

#### Step 2.2: Gap Analysis

Compare test cases against:
1. **Design document test specs** (110_テスト仕様書) — ensure all documented cases are covered
2. **Screen specs** (WA*) — ensure all screen flows are tested
3. **Batch specs** (BA*) — ensure all batch scenarios are tested
4. **Message design** — ensure all error messages are triggered by at least one test

Report any gaps and add missing test cases to the specification.

---

### Phase 3: テストコード作成 (Test Code Creation)

#### Step 3.1: Ensure Test Dependencies

Verify `pom.xml` contains:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.batch</groupId>
    <artifactId>spring-batch-test</artifactId>
    <scope>test</scope>
</dependency>
```

If missing, add them.

#### Step 3.2: Create Test Directory Structure

```
src/test/
├── java/com/example/{appname}/
│   ├── unit/
│   │   ├── service/          # Service unit tests
│   │   ├── repository/       # Repository tests with @DataJpaTest
│   │   ├── entity/           # Entity validation tests
│   │   └── batch/            # Batch job unit tests
│   ├── integration/
│   │   ├── controller/       # MockMvc controller tests
│   │   └── security/         # Authentication/authorization tests
│   └── system/
│       ├── functional/       # End-to-end functional tests
│       └── businessflow/     # Business scenario through-tests
└── resources/
    ├── application-test.yml  # Test-specific configuration
    └── test-data.sql         # Test data setup
```

#### Step 3.3: Create Test Configuration

**application-test.yml**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  sql:
    init:
      mode: always
  session:
    jdbc:
      initialize-schema: always
  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false
logging:
  level:
    org.springframework.security: DEBUG
```

#### Step 3.4: Generate Unit Tests (単体テスト)

**Service Tests** — Use Mockito to isolate service logic:
```java
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {
    @Mock private XxxRepository repository;
    @InjectMocks private XxxService service;

    @Test
    @DisplayName("正常系: ...")
    void testNormalCase() { ... }

    @Test
    @DisplayName("異常系: ...")
    void testErrorCase() { ... }
}
```

**Repository Tests** — Use @DataJpaTest with H2:
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class XxxRepositoryTest {
    @Autowired private XxxRepository repository;

    @Test
    @DisplayName("検索: 条件に一致するレコードが返却される")
    void testSearch() { ... }
}
```

**Batch Tests** — Use @SpringBatchTest:
```java
@SpringBootTest
@ActiveProfiles("test")
class XxxBatchJobTest {
    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    @DisplayName("正常系: ジョブが正常に完了する")
    void testJobCompletion() { ... }
}
```

#### Step 3.5: Generate Integration Tests (結合テスト)

**Controller Integration Tests** — Use MockMvc:
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class XxxControllerTest {
    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("GET /xxx — 画面が表示される")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetScreen() throws Exception {
        mockMvc.perform(get("/xxx"))
            .andExpect(status().isOk())
            .andExpect(view().name("xxx/screen"));
    }

    @Test
    @DisplayName("POST /xxx — バリデーションエラー時にフォームに戻る")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPostValidationError() throws Exception {
        mockMvc.perform(post("/xxx")
                .with(csrf())
                .param("field", ""))
            .andExpect(status().isOk())
            .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("未認証アクセス — ログインページにリダイレクト")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/xxx"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }
}
```

#### Step 3.6: Generate System Tests (システムテスト)

**Functional Tests** — Test complete screen flows:
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class XxxFunctionalTest {
    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("機能テスト: 登録→確認→実行→完了の画面遷移")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateFlow() throws Exception {
        // Step 1: GET create form
        // Step 2: POST confirm with valid data
        // Step 3: POST execute
        // Step 4: Verify redirect to complete
        // Step 5: Verify DB record created
    }
}
```

**Business Flow Tests** — Test end-to-end business scenarios:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BusinessFlowTest {

    @Test
    @DisplayName("業務スルー: ログイン→プロジェクト登録→検索→詳細→更新→完了")
    void testProjectManagementFlow() throws Exception {
        // Step 1: Login
        // Step 2: Create a new project
        // Step 3: Search for the project
        // Step 4: View project detail
        // Step 5: Update the project
        // Step 6: Verify final state
    }
}
```

#### Step 3.7: Test Naming Convention

Follow this pattern for test class and method names:
- Class: `{TargetClass}Test.java`
- Method: `test{Scenario}_{Condition}_{Expected}` or use `@DisplayName` with Japanese description
- Use `@DisplayName` with format: `"{区分}: {テスト内容}"` — e.g., `"正常系: プロジェクトが登録される"`

---

### Phase 4: テストコードレビュー (Test Code Review)

#### Step 4.1: Code Quality Check

Review all generated test code for:
- [ ] Each test method has a single clear assertion focus
- [ ] `@DisplayName` describes the scenario in Japanese
- [ ] Proper use of `@BeforeEach` for shared setup
- [ ] No test interdependencies (each test is self-contained)
- [ ] Mock setup matches actual dependencies
- [ ] CSRF tokens included in all POST requests (`with(csrf())`)
- [ ] Security context set for authenticated tests (`@WithMockUser`)
- [ ] `@ActiveProfiles("test")` on all test classes
- [ ] Test data setup/teardown is clean

#### Step 4.2: Compilation Check

Run compilation to verify test code compiles:
```bash
mvn clean compile test-compile -e
```

Fix any compilation errors before proceeding.

---

### Phase 5: テスト実施 (Test Execution)

#### Step 5.1: Run All Tests

Execute the full test suite:
```bash
mvn clean test -e
```

If using profiles:
```bash
mvn clean test -Dspring.profiles.active=test -e
```

#### Step 5.2: Run Tests by Category (if needed)

Run only unit tests:
```bash
mvn test -pl . -Dtest="com.example.*.unit.**" -e
```

Run only integration tests:
```bash
mvn test -pl . -Dtest="com.example.*.integration.**" -e
```

Run only system tests:
```bash
mvn test -pl . -Dtest="com.example.*.system.**" -e
```

#### Step 5.3: Fix Failing Tests (Iterative)

For each failing test:
1. Read the error message and stack trace
2. Determine if the failure is in:
   - **Test code** → fix the test
   - **Application code** → fix the application code (and note as a bug found by testing)
   - **Test configuration** → fix application-test.yml or test data
3. Re-run the specific failing test to verify the fix
4. Re-run the full suite to check for regressions

Common test failures and fixes:

| Error | Cause | Fix |
|-------|-------|-----|
| `NoSuchBeanDefinitionException` | Missing test config | Add `@SpringBootTest` or mock the bean |
| `AccessDeniedException` | Missing security context | Add `@WithMockUser` with correct role |
| `InvalidCsrfTokenException` | Missing CSRF in POST | Add `.with(csrf())` to MockMvc POST |
| `Table not found` in test | Test DB not initialized | Check `application-test.yml` SQL init config |
| `LazyInitializationException` | Lazy-loaded relation outside TX | Use `@Transactional` on test or fetch eagerly |
| `JobInstanceAlreadyCompleteException` | Batch job re-run | Use unique job parameters per test |

---

### Phase 6: テスト結果確認 (Test Result Verification)

#### Step 6.1: Parse Test Results

After test execution, read the Surefire reports:
```bash
# Summary
mvn test | grep -E "Tests run:|BUILD"

# Detailed failures (if any)
cat target/surefire-reports/*.txt
```

On Windows:
```powershell
Get-Content target/surefire-reports/*.txt | Select-String "Tests run:|FAILED|ERROR"
```

#### Step 6.2: Generate Test Report

Create `test-report.md` in the project root with:

```markdown
# テスト結果報告書 — {Application Name}

## 実施日: {date}
## テスト環境: JDK {version}, Spring Boot {version}, H2 (in-memory)

## サマリ
| テスト種別 | テスト件数 | 成功 | 失敗 | エラー | スキップ | 成功率 |
|-----------|----------|------|------|-------|---------|-------|
| 単体テスト | | | | | | |
| 結合テスト | | | | | | |
| システムテスト（機能テスト） | | | | | | |
| システムテスト（業務スルー） | | | | | | |
| **合計** | | | | | | |

## 失敗テスト一覧
| No. | テストクラス | テストメソッド | 失敗理由 | 対応状況 |
|-----|------------|-------------|---------|---------|

## 検出バグ一覧
| No. | 検出テスト | バグ内容 | 重要度 | 対応状況 |
|-----|----------|---------|-------|---------|

## カバレッジ
| パッケージ | クラス数 | テスト対象 | カバレッジ |
|-----------|---------|----------|-----------|
```

#### Step 6.3: Verify All Tests Pass

The goal is **100% pass rate**. If any tests still fail after iteration:
1. Document the failure reason in the test report
2. Classify as:
   - **テストコードバグ** — test code issue (fix the test)
   - **アプリケーションバグ** — application bug found by testing (fix the app)
   - **既知の制約** — known limitation (document and skip with reason)

Report the final results to the user with:
- Total test count and pass rate
- List of any remaining failures with explanations
- Bugs found during testing (if any)
- Test coverage summary

## Lessons Learned

1. **Always use `@ActiveProfiles("test")`** — Prevents interference with main application config
2. **Always include CSRF tokens** — Spring Security rejects POST without CSRF by default
3. **Use `@WithMockUser` not manual auth setup** — Cleaner and more maintainable
4. **Set `spring.batch.job.enabled=false` in test profile** — Prevents batch jobs from auto-running during tests
5. **Use `@Sql` for test data** — More reliable than shared `data.sql` which may conflict
6. **Each test must be independent** — No reliance on test execution order
7. **H2 compatibility** — Test SQL must use H2-compatible syntax (same rules as application)

---

### Phase 7: UI/画面操作テスト (UI/Screen Operation Test)

Browser-based visual testing that verifies actual screen rendering, layout, navigation, and user interactions. Captures screenshots as evidence and attaches them to the test report.

#### Prerequisites

- Application must be running (`mvn spring-boot:run` or already started on a known port)
- VS Code browser tools (Playwright) available: `open_browser_page`, `click_element`, `type_in_page`, `screenshot_page`, `read_page`
- A `screenshots/` directory in the project root for saving evidence

#### Step 7.1: Define UI Test Scenarios

Derive UI test scenarios from the application's screen flows:

| Category | Typical Scenarios |
|----------|------------------|
| 認証画面 | Login page display, login success, login failure with error message, logout |
| メニュー/TOP画面 | Top menu display after login, user name shown, navigation links present |
| 検索画面 | Search form display with all fields, search execution with results, empty results |
| 登録画面 | Create form display with required field markers, validation error display |
| 更新画面 | Update form pre-filled with existing data, confirmation display |
| 詳細画面 | Detail view with all fields displayed correctly |
| ダウンロード/アップロード | File operation screens, success/error messages |
| エラー画面 | 403/404/500 error page display |

Create UI test cases in the test specification with IDs prefixed `UI` (e.g., UI01, UI02, ...).

#### Step 7.2: Start the Application

Ensure the app is running before UI tests:
```powershell
# Check if already running
Try { Invoke-WebRequest -Uri http://localhost:8080/login -UseBasicParsing -TimeoutSec 3 } Catch { }

# If not running, start it
cd {project-root}
mvn spring-boot:run -Dspring-boot.run.profiles=default
```

Wait for the startup log message: `Started {AppName} in X seconds`

#### Step 7.3: Execute UI Tests with Browser Tools

Use VS Code's built-in browser tools (Playwright) to navigate and interact:

**1. Open the application:**
```
open_browser_page(url: "http://localhost:8080/login")
```

**2. Navigate and interact using proper CSS selectors:**
- Use `#id` selectors for form fields (e.g., `#loginId`, `#userPassword`)
- Use `button[type="submit"]` for submit buttons
- Use `a[href="/path"]` for navigation links
- Use `read_page` to inspect current DOM structure when selectors are unclear

**3. Take screenshots at each verification point:**
```
screenshot_page(path: "screenshots/01_login.png")
```

**4. Fill forms and submit:**
```
type_in_page(selector: "#loginId", text: "admin")
type_in_page(selector: "#userPassword", text: "password")
click_element(selector: "button[type='submit']")
```

#### Step 7.4: Screenshot Naming Convention

Use sequential numbering with descriptive suffixes:
```
screenshots/
├── 01_login.png              # Login page initial display
├── 02_top_menu.png           # TOP menu after successful login
├── 03_project_search.png     # Search form (empty)
├── 04_project_search_results.png  # Search results
├── 05_project_detail.png     # Project detail view
├── 06_project_create.png     # Create form
├── 07_project_update.png     # Update form with pre-filled data
├── 08_project_download.png   # Download page
├── 09_login_error.png        # Login failure error message
└── ...
```

#### Step 7.5: Verification Checklist per Screen

For each screenshot, verify and document:
- [ ] Page title / heading is correct
- [ ] All expected form fields are present
- [ ] Required field markers (*) are displayed where expected
- [ ] Data is correctly populated (for edit/detail screens)
- [ ] Navigation links/buttons are present and labeled correctly
- [ ] Error messages are displayed correctly (for error scenarios)
- [ ] User information (logged-in name) is displayed in header
- [ ] Layout matches design document expectations

#### Step 7.6: Add UI Test Results to Report

Append a UI test section to `test-report.md`:

```markdown
## N. UI/画面操作テスト

ブラウザ自動操作（Playwright）により、各画面の表示・遷移を実機確認しました。

### N.1 テストシナリオ一覧

| ID | シナリオ | 操作内容 | 結果 |
|----|---------|---------|------|
| UI01 | ログイン画面表示 | `/login` にアクセス、フォーム表示を確認 | ✅ |
| UI02 | ログイン失敗 | 無効な認証情報でログイン → エラーメッセージ表示 | ✅ |
| ... | ... | ... | ... |

### N.2 画面スクリーンショット

#### UI01: ログイン画面
![ログイン画面](screenshots/01_login.png)

- ログインID・パスワード入力欄が表示されること
- ログインボタンが表示されること

### N.3 UI テスト結果

| 項目 | 結果 |
|------|------|
| テストシナリオ数 | **N** |
| 成功 | **N** |
| 失敗 | **0** |
| スクリーンショット取得数 | **N枚** |
```

#### Step 7.7: Common Issues and Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| `type_in_page` timeout | Wrong selector — field `name` ≠ field `id` | Use `read_page` to inspect DOM, then use correct `#id` or `input[name="..."]` |
| Login POST fails via browser | CSRF token required | Use the login form's submit button (CSRF is embedded as hidden field) |
| Logout via GET shows error | `/logout` requires POST with CSRF | Navigate to `/login?logout` or use form-based logout button |
| Screenshots blank/empty | Page not fully loaded | Wait for navigation to complete; use `read_page` to confirm content before screenshot |
| `[ref=eNN]` attributes don't work | VS Code internal attributes, not real DOM | Use standard CSS selectors (`#id`, `.class`, `[name="..."]`) |
| Port already in use | App already running from previous session | Skip start step, proceed directly with browser tests |
