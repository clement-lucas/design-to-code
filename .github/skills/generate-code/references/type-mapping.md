# Database-to-Java Type Mapping Reference

## Standard Column Type Mapping

| Design Doc Type (Japanese) | SQL Type (PostgreSQL) | SQL Type (H2) | Java Type |
|---------------------------|----------------------|---------------|-----------|
| 文字列 (string) | VARCHAR(n) | VARCHAR(n) | String |
| 固定長文字列 | CHAR(n) | CHAR(n) | String |
| 数値 (numeric) | INTEGER | INTEGER | Integer |
| 長整数 | BIGINT | BIGINT | Long |
| 小数 | NUMERIC(p,s) | NUMERIC(p,s) | BigDecimal |
| 日付 (date) | DATE | DATE | LocalDate |
| 日時 (datetime) | TIMESTAMP | TIMESTAMP | LocalDateTime |
| 時刻 (time) | TIME | TIME | LocalTime |
| フラグ (flag) | BOOLEAN | BOOLEAN | Boolean |
| バイナリ (binary) | BYTEA | BYTEA | byte[] |
| テキスト (text/memo) | TEXT | CLOB | String |

## Domain Mapping Patterns

Common domain patterns found in Japanese design documents:

| Domain Name | Meaning | Typical Java Type | Validation |
|------------|---------|-------------------|------------|
| コード (code) | Code values | String | Pattern, Size |
| 名称 (name) | Names | String | NotBlank, Size |
| 日付 (date) | Dates | LocalDate | NotNull |
| フラグ (flag) | Boolean flags | Boolean | — |
| 金額 (amount) | Money amounts | BigDecimal | Min, Digits |
| 数量 (quantity) | Quantities | Integer | Min |
| パスワード (password) | Passwords | String | Size |
| メールアドレス (email) | Email addresses | String | Email |
| 電話番号 (phone) | Phone numbers | String | Pattern |
| 備考 (remarks) | Free-text notes | String | Size |

## JPA Annotation Patterns

```java
// Primary key (auto-generated)
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

// Primary key (business key from design)
@Id
@Column(name = "project_id", length = 10)
private String projectId;

// Standard string field
@Column(name = "project_name", length = 128, nullable = false)
private String projectName;

// Date field
@Column(name = "start_date")
private LocalDate startDate;

// Money field
@Column(name = "budget", precision = 15, scale = 0)
private BigDecimal budget;

// Boolean flag
@Column(name = "delete_flag", nullable = false)
private Boolean deleteFlag = false;

// Many-to-One (FK)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "organization_id")
private Organization organization;

// One-to-Many (reverse)
@OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
private List<ProjectMember> members = new ArrayList<>();

// Audit columns (common in Japanese enterprise systems)
@Column(name = "created_at", updatable = false)
private LocalDateTime createdAt;

@Column(name = "updated_at")
private LocalDateTime updatedAt;

@Column(name = "created_by", length = 128, updatable = false)
private String createdBy;

@Column(name = "updated_by", length = 128)
private String updatedBy;

// Version for optimistic locking
@Version
@Column(name = "version")
private Long version;
```

## H2 Compatibility Notes

When using H2 in PostgreSQL mode (`MODE=PostgreSQL`):
- Use `BYTEA` instead of `BLOB` for binary data
- Use `MERGE INTO ... KEY(...) VALUES(...)` instead of `INSERT ... ON CONFLICT`
- `SERIAL` / `BIGSERIAL` → Use `IDENTITY` or `AUTO_INCREMENT`
- `BOOLEAN` works natively (TRUE/FALSE)
- `TEXT` → Use `CLOB` or `VARCHAR(MAX)`
- `JSONB` is not fully supported — use `CLOB` as fallback
