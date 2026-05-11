"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.runGeneratePhase = runGeneratePhase;
const vscode = __importStar(require("vscode"));
const path = __importStar(require("path"));
const fs = __importStar(require("fs"));
const MAX_CONTEXT_CHARS = 60_000; // Stay within LM context window for the spec
async function runGeneratePhase(stream, workspaceRoot, outputDir, token) {
    const wsPath = workspaceRoot.fsPath;
    // ── 1. Find extracted content ─────────────────────────────────────────────
    stream.progress('Reading extracted design document content…');
    const extractedText = readExtractedText(wsPath);
    if (!extractedText) {
        stream.markdown('**Extracted content not found.**\n\n' +
            'Run `/extract` first to produce `extracted/extracted_text.txt` from your design documents.');
        return {};
    }
    // ── 2. Select a language model ────────────────────────────────────────────
    let model;
    try {
        const models = await vscode.lm.selectChatModels({ vendor: 'copilot', family: 'gpt-4o' });
        if (!models.length) {
            throw new Error('No model available');
        }
        model = models[0];
    }
    catch {
        stream.markdown('**Language model unavailable.**\n\n' +
            'Make sure GitHub Copilot is enabled and you have an active subscription.');
        return {};
    }
    if (token.isCancellationRequested) {
        return {};
    }
    const absOutputDir = path.isAbsolute(outputDir)
        ? outputDir
        : path.join(wsPath, outputDir);
    stream.markdown(`Generating Spring Boot application into \`${outputDir}\` using **${model.name}**…\n\n` +
        `Spec size: ${extractedText.length.toLocaleString()} chars (truncated to ${MAX_CONTEXT_CHARS.toLocaleString()} for context)\n`);
    // ── 3. Analyse spec to derive metadata ────────────────────────────────────
    stream.progress('Analysing specifications…');
    const analysis = await analyseSpec(model, extractedText, token);
    if (!analysis) {
        return {};
    }
    stream.markdown(`### Spec Analysis\n` +
        `- **App name**: ${analysis.appName}\n` +
        `- **Package**: \`${analysis.basePackage}\`\n` +
        `- **Tables detected**: ${analysis.tables.join(', ') || '(none detected)'}\n` +
        `- **Screens detected**: ${analysis.screens.join(', ') || '(none detected)'}\n` +
        `- **Batch jobs detected**: ${analysis.batches.join(', ') || '(none detected)'}\n\n`);
    if (token.isCancellationRequested) {
        return {};
    }
    // ── 4. Generate each artifact layer ───────────────────────────────────────
    const layers = [
        {
            name: 'schema.sql',
            relPath: `src/main/resources/schema.sql`,
            prompt: buildPrompt('Generate `schema.sql`', analysis, extractedText, schemaInstructions()),
        },
        {
            name: 'pom.xml',
            relPath: 'pom.xml',
            prompt: buildPrompt('Generate `pom.xml`', analysis, extractedText, pomInstructions()),
        },
        {
            name: 'Main Application class',
            relPath: `src/main/java/${packageToPath(analysis.basePackage)}/${toPascalCase(analysis.appName)}Application.java`,
            prompt: buildPrompt('Generate the Spring Boot main application class', analysis, extractedText, mainAppInstructions()),
        },
        {
            name: 'application.yml',
            relPath: 'src/main/resources/application.yml',
            prompt: buildPrompt('Generate `application.yml`', analysis, extractedText, appYmlInstructions()),
        },
        {
            name: 'JPA entities',
            relPath: `src/main/java/${packageToPath(analysis.basePackage)}/entity/`,
            prompt: buildPrompt('Generate ALL JPA entity classes (one per table)', analysis, extractedText, entityInstructions()),
        },
        {
            name: 'Repositories',
            relPath: `src/main/java/${packageToPath(analysis.basePackage)}/repository/`,
            prompt: buildPrompt('Generate Spring Data JPA repository interfaces', analysis, extractedText, repositoryInstructions()),
        },
        {
            name: 'Service layer',
            relPath: `src/main/java/${packageToPath(analysis.basePackage)}/service/`,
            prompt: buildPrompt('Generate service classes', analysis, extractedText, serviceInstructions()),
        },
        {
            name: 'Controllers',
            relPath: `src/main/java/${packageToPath(analysis.basePackage)}/controller/`,
            prompt: buildPrompt('Generate Spring MVC controllers', analysis, extractedText, controllerInstructions()),
        },
        {
            name: 'Security config',
            relPath: `src/main/java/${packageToPath(analysis.basePackage)}/config/SecurityConfig.java`,
            prompt: buildPrompt('Generate Spring Security 6.x configuration class', analysis, extractedText, securityInstructions()),
        },
        {
            name: 'Thymeleaf templates',
            relPath: 'src/main/resources/templates/',
            prompt: buildPrompt('Generate Thymeleaf HTML templates', analysis, extractedText, templateInstructions()),
        },
        {
            name: 'messages.properties',
            relPath: 'src/main/resources/messages.properties',
            prompt: buildPrompt('Generate `messages.properties`', analysis, extractedText, messagesInstructions()),
        },
        {
            name: 'data.sql (seed data)',
            relPath: 'src/main/resources/data.sql',
            prompt: buildPrompt('Generate `data.sql` seed data', analysis, extractedText, dataSqlInstructions()),
        },
    ];
    // If batch specs exist, add batch configs
    if (analysis.batches.length > 0) {
        layers.push({
            name: 'Spring Batch job configurations',
            relPath: `src/main/java/${packageToPath(analysis.basePackage)}/batch/`,
            prompt: buildPrompt('Generate Spring Batch 5.x job configuration classes', analysis, extractedText, batchInstructions()),
        });
    }
    const generated = [];
    for (const layer of layers) {
        if (token.isCancellationRequested) {
            break;
        }
        stream.progress(`Generating ${layer.name}…`);
        const code = await callLm(model, layer.prompt, token);
        if (!code) {
            continue;
        }
        const files = parseGeneratedFiles(code, layer.name, analysis);
        for (const [relFile, content] of Object.entries(files)) {
            const absFile = path.join(absOutputDir, relFile);
            fs.mkdirSync(path.dirname(absFile), { recursive: true });
            fs.writeFileSync(absFile, content, 'utf-8');
            generated.push(`\`${path.join(outputDir, relFile)}\``);
        }
        stream.markdown(`Generated **${layer.name}** (${Object.keys(files).length} file(s))\n`);
    }
    // ── 5. Final summary ──────────────────────────────────────────────────────
    if (generated.length === 0) {
        stream.markdown('**No files were generated.** The language model may have been cancelled or returned no content.');
        return {};
    }
    stream.markdown(`\n---\n` +
        `### Phase 2 Complete ✓\n\n` +
        `Generated **${generated.length} file(s)** in \`${outputDir}\`.\n\n` +
        `To build and run the application:\n` +
        `\`\`\`bash\n` +
        `cd ${outputDir}\n` +
        `mvn spring-boot:run\n` +
        `\`\`\`\n\n` +
        `Then open http://localhost:8080`);
    return { metadata: { phase: 'generate', outputDir, fileCount: generated.length } };
}
async function analyseSpec(model, spec, token) {
    const prompt = `You are analysing extracted Japanese software design documents.\n` +
        `Return ONLY valid JSON (no markdown fences) with this exact structure:\n` +
        `{"appName":"<short lowercase app name>","basePackage":"com.example.<appName>",` +
        `"tables":["<table1>",...],"screens":["<WA10101>",...],"batches":["<BA10601>",...]}\n\n` +
        `SPEC (first ${MAX_CONTEXT_CHARS} chars):\n${spec.slice(0, MAX_CONTEXT_CHARS)}`;
    const raw = await callLm(model, prompt, token);
    if (!raw) {
        return undefined;
    }
    try {
        // Strip any accidental markdown fences
        const json = raw.replace(/```json?\n?/g, '').replace(/```/g, '').trim();
        return JSON.parse(json);
    }
    catch {
        // Fallback defaults
        return { appName: 'app', basePackage: 'com.example.app', tables: [], screens: [], batches: [] };
    }
}
// ── LM call ───────────────────────────────────────────────────────────────────
async function callLm(model, prompt, token) {
    try {
        const messages = [vscode.LanguageModelChatMessage.User(prompt)];
        const response = await model.sendRequest(messages, {}, token);
        let text = '';
        for await (const fragment of response.text) {
            text += fragment;
        }
        return text.trim() || undefined;
    }
    catch {
        return undefined;
    }
}
// ── File parsing ──────────────────────────────────────────────────────────────
/**
 * Parse LM output that may contain one or more files.
 * Looks for fenced code blocks with filenames or falls back to a single file.
 */
function parseGeneratedFiles(raw, layerName, analysis) {
    const result = {};
    // Pattern: ```java\n// FILE: path/to/File.java\n<content>\n```
    const fenceWithFile = /```(?:\w+)?\n\/[/*]\s*FILE:\s*(\S+)[^\n]*\n([\s\S]*?)```/g;
    let match;
    while ((match = fenceWithFile.exec(raw)) !== null) {
        result[match[1]] = match[2].trimEnd();
    }
    if (Object.keys(result).length > 0) {
        return result;
    }
    // Pattern: ### filename.java\n```...```
    const headerWithFence = /###\s+(\S+)\n```(?:\w+)?\n([\s\S]*?)```/g;
    while ((match = headerWithFence.exec(raw)) !== null) {
        result[match[1]] = match[2].trimEnd();
    }
    if (Object.keys(result).length > 0) {
        return result;
    }
    // Fall back: treat entire response as one file, derive name from layer
    const ext = layerName.includes('yml') ? 'yml'
        : layerName.includes('sql') ? 'sql'
            : layerName.includes('pom') ? 'xml'
                : layerName.includes('properties') ? 'properties'
                    : 'java';
    const slug = layerName.toLowerCase().replace(/[^a-z0-9]/g, '_');
    const relPath = ext === 'java'
        ? `src/main/java/${packageToPath(analysis.basePackage)}/${slug}/${toPascalCase(slug)}.java`
        : `src/main/resources/${slug}.${ext}`;
    // Strip outer fence if present
    const code = raw.replace(/^```\w*\n?/, '').replace(/\n?```$/, '');
    result[relPath] = code;
    return result;
}
// ── Helpers ───────────────────────────────────────────────────────────────────
function readExtractedText(wsPath) {
    const candidates = [
        path.join(wsPath, 'extracted', 'extracted_text.txt'),
        path.join(wsPath, 'docs_output.txt'),
    ];
    for (const c of candidates) {
        if (fs.existsSync(c)) {
            return fs.readFileSync(c, 'utf-8');
        }
    }
    return undefined;
}
function packageToPath(pkg) {
    return pkg.replace(/\./g, '/');
}
function toPascalCase(str) {
    return str.replace(/[-_](.)/g, (_, c) => c.toUpperCase())
        .replace(/^(.)/, (_, c) => c.toUpperCase());
}
function buildPrompt(task, analysis, spec, instructions) {
    return (`You are generating a Spring Boot 3.x application from Japanese design documents.\n` +
        `App name: ${analysis.appName} | Base package: ${analysis.basePackage}\n` +
        `Tables: ${analysis.tables.join(', ') || 'unknown'}.\n` +
        `Screens: ${analysis.screens.join(', ') || 'none'}.\n` +
        `Batch jobs: ${analysis.batches.join(', ') || 'none'}.\n\n` +
        `TASK: ${task}\n` +
        `RULES:\n${instructions}\n\n` +
        `SPEC:\n${spec.slice(0, MAX_CONTEXT_CHARS)}`);
}
// ── Per-layer instructions ────────────────────────────────────────────────────
function schemaInstructions() {
    return (`- Use H2-compatible DDL (MERGE INTO ... KEY(...) for upserts, not ON CONFLICT)\n` +
        `- No BLOB type — use BINARY VARYING or VARCHAR for binary data\n` +
        `- Create referenced tables before referencing tables\n` +
        `- Include all NOT NULL constraints and DEFAULT values from the design\n` +
        `- Output: a single schema.sql file`);
}
function pomInstructions() {
    return (`- Spring Boot 3.x parent\n` +
        `- Java 17 source/target\n` +
        `- Include: web, thymeleaf, data-jpa, validation, security, session-jdbc, batch (if batches exist)\n` +
        `- Include H2 (runtime) and postgresql (runtime) drivers\n` +
        `- Include thymeleaf-extras-springsecurity6\n` +
        `- Output: a single pom.xml file`);
}
function mainAppInstructions() {
    return (`- @SpringBootApplication\n` +
        `- Standard main method\n` +
        `- Class name: {AppName}Application\n` +
        `- Output as: // FILE: src/main/java/{package}/{AppName}Application.java`);
}
function appYmlInstructions() {
    return (`- H2 in-memory datasource (url: jdbc:h2:mem:testdb)\n` +
        `- spring.jpa.hibernate.ddl-auto: none (use schema.sql)\n` +
        `- spring.sql.init.mode: always\n` +
        `- spring.session.store-type: jdbc\n` +
        `- server.port: 8080\n` +
        `- Output: a single application.yml`);
}
function entityInstructions() {
    return (`- One class per table\n` +
        `- All entities MUST implement java.io.Serializable (Spring Session JDBC requirement)\n` +
        `- Use @Entity, @Table, @Id, @GeneratedValue, @Column annotations\n` +
        `- Map Japanese column names to camelCase Java field names\n` +
        `- Use @ManyToOne / @OneToMany for foreign key relationships\n` +
        `- Output each class as: // FILE: src/main/java/{package}/entity/{ClassName}.java`);
}
function repositoryInstructions() {
    return (`- Extend JpaRepository<Entity, IdType>\n` +
        `- Add custom query methods derived from screen/batch requirements\n` +
        `- Use @Query with JPQL where needed\n` +
        `- Output each as: // FILE: src/main/java/{package}/repository/{Entity}Repository.java`);
}
function serviceInstructions() {
    return (`- @Service, @Transactional on write methods\n` +
        `- Inject repositories via constructor\n` +
        `- Implement business rules described in the screen/batch specs\n` +
        `- Output each as: // FILE: src/main/java/{package}/service/{Name}Service.java`);
}
function controllerInstructions() {
    return (`- @Controller, @RequestMapping\n` +
        `- POST handlers use @Valid @ModelAttribute with BindingResult\n` +
        `- Return Thymeleaf view names (no .html extension)\n` +
        `- Use redirect-after-POST for success flows\n` +
        `- Output each as: // FILE: src/main/java/{package}/controller/{Name}Controller.java`);
}
function securityInstructions() {
    return (`- Spring Security 6.x (use SecurityFilterChain bean, not WebSecurityConfigurerAdapter)\n` +
        `- Form login at /login, logout at /logout\n` +
        `- BCryptPasswordEncoder bean\n` +
        `- Permit /css/**, /js/**, /login, /error without authentication\n` +
        `- Protect all other paths\n` +
        `- Output as: // FILE: src/main/java/{package}/config/SecurityConfig.java`);
}
function templateInstructions() {
    return (`- Thymeleaf 3.x with layout dialect or fragments\n` +
        `- Create a shared fragments/layout.html with header/nav/footer\n` +
        `- Each screen (WA*) gets its own HTML file\n` +
        `- Use th:field for form inputs, th:errors for validation messages\n` +
        `- Use th:text, th:each for data display\n` +
        `- Output each as: // FILE: src/main/resources/templates/{feature}/{screen}.html`);
}
function messagesInstructions() {
    return (`- UTF-8 properties file\n` +
        `- Include all message IDs from メッセージ設計 (message design) docs\n` +
        `- Include Bean Validation default messages\n` +
        `- Format: MSG_00001=メッセージ本文\n` +
        `- Output: a single messages.properties file`);
}
function dataSqlInstructions() {
    return (`- Use MERGE INTO ... KEY(...) syntax (H2 compatible)\n` +
        `- Include system account(s) with real BCrypt hashes — use \$2a\$10\$ prefix\n` +
        `- IMPORTANT: never fabricate or shorten BCrypt hashes; use full 60-char hashes\n` +
        `- Include code master / classification seed data from コード設計 docs\n` +
        `- Output: a single data.sql file`);
}
function batchInstructions() {
    return (`- Spring Batch 5.x API: use JobBuilder / StepBuilder / JobRepository\n` +
        `- Do NOT use deprecated JobBuilderFactory or StepBuilderFactory\n` +
        `- One @Configuration class per batch job\n` +
        `- Implement ItemReader, ItemProcessor, ItemWriter as inner classes or beans\n` +
        `- Output each as: // FILE: src/main/java/{package}/batch/{JobName}BatchConfig.java`);
}
//# sourceMappingURL=generate.js.map