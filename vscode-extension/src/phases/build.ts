import * as vscode from 'vscode';
import * as path from 'path';
import * as cp from 'child_process';
import * as fs from 'fs';

const MAX_FIX_ITERATIONS = 5;
const MAX_ERROR_CHARS = 8_000;
const MAX_FILE_CHARS = 4_000;

interface ExecResult {
    stdout: string;
    stderr: string;
    exitCode: number;
}

export async function runBuildPhase(
    stream: vscode.ChatResponseStream,
    workspaceRoot: vscode.Uri,
    appDir: string,
    token: vscode.CancellationToken
): Promise<vscode.ChatResult> {

    const wsPath = workspaceRoot.fsPath;
    const absAppDir = path.isAbsolute(appDir) ? appDir : path.join(wsPath, appDir);

    // ── 1. Find pom.xml ───────────────────────────────────────────────────────
    stream.progress('Locating Maven project…');
    const pomPath = await findPom(absAppDir, wsPath);
    if (!pomPath) {
        stream.markdown(
            '**No `pom.xml` found.**\n\n' +
            `Searched in \`${appDir}\` and the workspace.\n` +
            'Run `/generate` first, or specify the correct directory:\n' +
            '```\n@design-to-code /build app directory: samples/generated-app\n```'
        );
        return {};
    }

    const projectDir = path.dirname(pomPath);
    stream.markdown(`Found Maven project at \`${path.relative(wsPath, projectDir)}\`\n`);

    // ── 2. Check java and mvn ─────────────────────────────────────────────────
    stream.progress('Checking Java and Maven…');
    const javaOut = await checkTool('java', ['-version']);
    const mvnOut = await checkTool('mvn', ['-version']);

    if (!javaOut) {
        stream.markdown(
            '**Java not found.**\n\n' +
            'Install JDK 17+ and ensure `java` is on your `PATH`.\n' +
            'Download: https://adoptium.net/'
        );
        return {};
    }
    if (!mvnOut) {
        stream.markdown(
            '**Maven not found.**\n\n' +
            'Install Maven 3.9+ and ensure `mvn` is on your `PATH`.\n' +
            'Download: https://maven.apache.org/download.cgi'
        );
        return {};
    }

    const javaLine = javaOut.trim().split('\n')[0];
    const mvnLine = mvnOut.trim().split('\n')[0];
    stream.markdown(`- Java: ${javaLine}\n- Maven: ${mvnLine}\n\n`);

    // ── 3. Select LM for error fixing ─────────────────────────────────────────
    let model: vscode.LanguageModelChat | undefined;
    try {
        const models = await vscode.lm.selectChatModels({ vendor: 'copilot', family: 'gpt-4o' });
        if (models.length) { model = models[0]; }
    } catch { /* proceed without model; report errors manually */ }

    if (token.isCancellationRequested) { return {}; }

    // ── 4. Compile loop ───────────────────────────────────────────────────────
    stream.markdown(`### Phase 3: Build\n`);
    stream.progress('Running mvn clean compile…');
    let compileResult = await execMaven(projectDir, ['clean', 'compile', '-e']);
    let iteration = 0;

    while (compileResult.exitCode !== 0 && iteration < MAX_FIX_ITERATIONS) {
        iteration++;
        const combined = (compileResult.stdout + '\n' + compileResult.stderr);
        const errorSnippet = combined.slice(-MAX_ERROR_CHARS);

        stream.markdown(
            `\n**Compilation error** (attempt ${iteration}/${MAX_FIX_ITERATIONS}):\n` +
            `\`\`\`\n${errorSnippet.slice(0, 2_000)}\n\`\`\`\n`
        );

        if (!model || token.isCancellationRequested) { break; }

        stream.progress(`Diagnosing errors (attempt ${iteration})…`);
        const failingFiles = extractFailingFiles(combined, projectDir);
        const fileContext = readFileContext(failingFiles, projectDir, MAX_FILE_CHARS);

        stream.progress(`Applying fixes (attempt ${iteration})…`);
        const lmResponse = await askLmToFix(model, errorSnippet, fileContext, token);
        if (!lmResponse || token.isCancellationRequested) { break; }

        const written = applyFixes(lmResponse, projectDir);
        if (written.length === 0) {
            stream.markdown('No file changes were proposed. Stopping fix loop.\n');
            break;
        }
        stream.markdown(
            `Applied fixes to: ${written.map(f => `\`${path.relative(projectDir, f)}\``).join(', ')}\n`
        );

        stream.progress(`Recompiling (attempt ${iteration})…`);
        compileResult = await execMaven(projectDir, ['clean', 'compile', '-e']);
    }

    if (compileResult.exitCode !== 0) {
        const finalErrors = (compileResult.stdout + '\n' + compileResult.stderr).slice(-3_000);
        stream.markdown(
            `\n**Build failed** after ${iteration} fix attempt(s).\n\n` +
            `\`\`\`\n${finalErrors}\n\`\`\`\n\n` +
            '**Common manual fixes:**\n' +
            '- `NotSerializableException` → add `implements Serializable` to entities in the security context\n' +
            '- `JobBuilderFactory not found` → use `new JobBuilder(name, jobRepository)` (Spring Batch 5.x)\n' +
            '- `ON CONFLICT` syntax error → replace with `MERGE INTO ... KEY(...)`\n' +
            '- Table not found → check DDL creation order in `schema.sql`\n' +
            '- Login always fails → regenerate BCrypt hash with `BCryptPasswordEncoder`\n\n' +
            'Fix the issues above, then run `/build` again.'
        );
        return {};
    }

    stream.markdown(`\n**Build successful!** ✓\n`);

    if (token.isCancellationRequested) { return {}; }

    // ── 5. Launch the application ─────────────────────────────────────────────
    stream.markdown(`\n### Starting Application\n`);
    stream.progress('Launching Spring Boot application…');

    const terminal = vscode.window.createTerminal({
        name: 'Spring Boot — design-to-code',
        cwd: projectDir,
    });
    terminal.show(/* preserveFocus */ false);
    terminal.sendText('mvn spring-boot:run');

    stream.markdown(
        `Application starting in the **Spring Boot — design-to-code** terminal.\n\n` +
        `Once you see \`Started ... in ... seconds\`, open:\n\n` +
        `| URL | Purpose |\n` +
        `|-----|---------|\n` +
        `| [http://localhost:8080](http://localhost:8080) | Application |\n` +
        `| [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | H2 database console |\n\n` +
        `Login credentials are defined in \`data.sql\` (typically \`admin\` / \`password\`).\n\n` +
        `To stop the application, press **Ctrl+C** in the terminal.\n`
    );

    return { metadata: { phase: 'build' } };
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

/**
 * Find pom.xml — check the specified directory first, then search the workspace.
 */
async function findPom(startDir: string, wsRoot: string): Promise<string | undefined> {
    const direct = path.join(startDir, 'pom.xml');
    if (fs.existsSync(direct)) { return direct; }

    const pattern = new vscode.RelativePattern(wsRoot, '**/pom.xml');
    const uris = await vscode.workspace.findFiles(pattern, '**/node_modules/**', 5);
    if (uris.length > 0) {
        const inAppDir = uris.find(u => u.fsPath.startsWith(startDir));
        return (inAppDir ?? uris[0]).fsPath;
    }
    return undefined;
}

/** Run a command and return combined stdout+stderr, or undefined if not found on PATH. */
async function checkTool(cmd: string, args: string[]): Promise<string | undefined> {
    return new Promise(resolve => {
        const proc = cp.spawn(cmd, args, { shell: true });
        let out = '';
        proc.stdout.on('data', (d: Buffer) => { out += d.toString(); });
        proc.stderr.on('data', (d: Buffer) => { out += d.toString(); }); // java -version uses stderr
        proc.on('close', code => resolve(code === 0 ? out : undefined));
        proc.on('error', () => resolve(undefined));
    });
}

/** Run mvn with given arguments inside cwd. */
async function execMaven(cwd: string, args: string[]): Promise<ExecResult> {
    return new Promise(resolve => {
        const proc = cp.spawn('mvn', args, { cwd, shell: true });
        let stdout = '';
        let stderr = '';
        proc.stdout.on('data', (d: Buffer) => { stdout += d.toString(); });
        proc.stderr.on('data', (d: Buffer) => { stderr += d.toString(); });
        proc.on('close', exitCode => resolve({ stdout, stderr, exitCode: exitCode ?? 1 }));
        proc.on('error', err => resolve({ stdout, stderr: err.message, exitCode: 1 }));
    });
}

/**
 * Parse javac-style error output to find which source files are involved.
 * Supports both absolute paths (Maven) and relative src/ paths.
 */
function extractFailingFiles(output: string, projectDir: string): string[] {
    const files = new Set<string>();

    // Maven format: [ERROR] /abs/path/to/File.java:[line,col] error:
    const absPat = /\[ERROR\]\s+(.+?\.java):\[/gm;
    let m: RegExpExecArray | null;
    while ((m = absPat.exec(output)) !== null) {
        const f = m[1].trim();
        if (fs.existsSync(f)) { files.add(f); }
    }

    // Relative format: src/main/java/.../File.java:[line] error:
    const relPat = /\b(src[\\/]main[\\/]java[\\/]\S+?\.java)/g;
    while ((m = relPat.exec(output)) !== null) {
        const abs = path.join(projectDir, m[1].replace(/\\/g, '/'));
        if (fs.existsSync(abs)) { files.add(abs); }
    }

    return [...files].slice(0, 6); // cap to keep prompt size manageable
}

/** Build a context string from the source files involved in errors. */
function readFileContext(files: string[], projectDir: string, maxCharsPerFile: number): string {
    if (files.length === 0) { return ''; }
    return files.map(f => {
        const rel = path.relative(projectDir, f).replace(/\\/g, '/');
        const content = fs.readFileSync(f, 'utf-8').slice(0, maxCharsPerFile);
        return `// FILE: ${rel}\n\`\`\`java\n${content}\n\`\`\``;
    }).join('\n\n');
}

/** Write LM-proposed file fixes back to disk. Returns full paths of written files. */
function applyFixes(lmResponse: string, projectDir: string): string[] {
    const written: string[] = [];
    const blockRe = /\/\/\s*FILE:\s*([^\n]+)\n```[a-z]*\n([\s\S]*?)```/g;
    let m: RegExpExecArray | null;
    while ((m = blockRe.exec(lmResponse)) !== null) {
        const relPath = m[1].trim().replace(/\\/g, '/');
        const content = m[2];
        const abs = path.join(projectDir, relPath);
        try {
            fs.mkdirSync(path.dirname(abs), { recursive: true });
            fs.writeFileSync(abs, content, 'utf-8');
            written.push(abs);
        } catch {
            // Skip files that can't be written (e.g. bad path)
        }
    }
    return written;
}

const FIX_SYSTEM_PROMPT = `You are a Spring Boot expert fixing Java compilation errors.
The project uses: Spring Boot 3.x, Java 17, Spring Security 6.x, Spring Batch 5.x, Thymeleaf, H2, Spring Data JPA.

Rules:
- Spring Batch 5.x: use new JobBuilder(name, jobRepository) and new StepBuilder(name, jobRepository). JobBuilderFactory/StepBuilderFactory do not exist.
- Spring Security 6.x: use lambda-style .authorizeHttpRequests(auth -> auth.requestMatchers(...).permitAll()). No antMatchers.
- Entities stored in the Spring Security context (UserDetails impl and everything it references) must implement java.io.Serializable with a serialVersionUID.
- H2 data.sql upserts: use MERGE INTO table (cols) KEY(pk) VALUES (...); — not INSERT ... ON CONFLICT.
- BCrypt hashes must start with $2a$10$ followed by exactly 53 base64 characters. Never fabricate hashes.
- Schema DDL: create referenced tables before tables that reference them (FK order).

Return ONLY the corrected file(s) in this exact format — one fenced block per file:
// FILE: src/main/java/com/example/.../ClassName.java
\`\`\`java
<full corrected file content>
\`\`\`

Do not include any explanation outside the code blocks.`;

/** Call the Copilot LM to propose fixes for compilation errors. */
async function askLmToFix(
    model: vscode.LanguageModelChat,
    errors: string,
    fileContext: string,
    token: vscode.CancellationToken
): Promise<string | undefined> {
    const userContent =
        `## Compilation errors\n\`\`\`\n${errors.slice(0, MAX_ERROR_CHARS)}\n\`\`\`\n` +
        (fileContext ? `\n## Source files\n${fileContext}` : '');

    const messages = [
        vscode.LanguageModelChatMessage.User(FIX_SYSTEM_PROMPT + '\n\n---\n\n' + userContent),
    ];

    try {
        const response = await model.sendRequest(messages, {}, token);
        let text = '';
        for await (const chunk of response.text) {
            text += chunk;
        }
        return text;
    } catch {
        return undefined;
    }
}
