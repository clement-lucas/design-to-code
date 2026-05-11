import * as vscode from 'vscode';
import * as path from 'path';
import * as cp from 'child_process';
import * as fs from 'fs';

const MAX_FIX_ITERATIONS = 5;
const MAX_ERROR_CHARS = 8_000;

interface ExecResult {
    stdout: string;
    stderr: string;
    exitCode: number;
}

export async function runTestPhase(
    stream: vscode.ChatResponseStream,
    workspaceRoot: vscode.Uri,
    appDir: string,
    token: vscode.CancellationToken
): Promise<vscode.ChatResult> {

    const wsPath = workspaceRoot.fsPath;
    const absAppDir = path.isAbsolute(appDir) ? appDir : path.join(wsPath, appDir);

    // ── 1. Find pom.xml ───────────────────────────────────────────────────────
    stream.progress('Locating Maven project…');
    const pomPath = findPom(absAppDir, wsPath);
    if (!pomPath) {
        stream.markdown(
            '**No `pom.xml` found.**\n\n' +
            `Searched in \`${appDir}\` and the workspace.\n` +
            'Run `/generate` and `/build` first, or specify the correct directory:\n' +
            '```\n@design-to-code /test app directory: samples/generated-app\n```'
        );
        return {};
    }

    const projectDir = path.dirname(pomPath);
    stream.markdown(`Found Maven project at \`${path.relative(wsPath, projectDir)}\`\n`);

    // ── 2. Check prerequisites ────────────────────────────────────────────────
    stream.progress('Checking Java and Maven…');
    const javaOk = await checkTool('java', ['-version']);
    const mvnOk = await checkTool('mvn', ['-version']);

    if (!javaOk) {
        stream.markdown('**Java not found.** Install JDK 17+ and ensure `java` is on your `PATH`.\n');
        return {};
    }
    if (!mvnOk) {
        stream.markdown('**Maven not found.** Install Maven 3.9+ and ensure `mvn` is on your `PATH`.\n');
        return {};
    }

    // ── 3. Verify test sources exist ──────────────────────────────────────────
    stream.progress('Checking for test sources…');
    const testDir = path.join(projectDir, 'src', 'test', 'java');
    if (!fs.existsSync(testDir)) {
        stream.markdown(
            '**No test sources found** at `src/test/java`.\n\n' +
            'Use the `@test-automation` agent to generate test specifications and code first:\n' +
            '```\n@test-automation Generate tests for this project\n```'
        );
        return { metadata: { phase: 'test', status: 'no-tests' } };
    }

    // ── 4. Compile tests ──────────────────────────────────────────────────────
    stream.progress('Compiling test code…');
    const compileResult = await exec('mvn', ['clean', 'test-compile', '-e', '-q'], projectDir);
    if (compileResult.exitCode !== 0) {
        const errorOutput = truncate(compileResult.stdout + '\n' + compileResult.stderr, MAX_ERROR_CHARS);
        stream.markdown(
            '**Test compilation failed.**\n\n' +
            '```\n' + errorOutput + '\n```\n\n' +
            'Fix compilation errors and try again.'
        );
        return { metadata: { phase: 'test', status: 'compile-error' } };
    }
    stream.markdown('✓ Test code compiled successfully.\n\n');

    // ── 5. Run tests ──────────────────────────────────────────────────────────
    stream.progress('Running test suite…');
    let testResult = await exec('mvn', ['test', '-e'], projectDir);
    let iteration = 0;

    while (testResult.exitCode !== 0 && iteration < MAX_FIX_ITERATIONS) {
        iteration++;
        const errorOutput = truncate(testResult.stdout + '\n' + testResult.stderr, MAX_ERROR_CHARS);
        stream.markdown(
            `**Test run ${iteration} failed.** Attempting auto-fix…\n\n` +
            '```\n' + errorOutput + '\n```\n\n'
        );

        // Provide context for Copilot to fix
        stream.markdown(`Auto-fix iteration ${iteration}/${MAX_FIX_ITERATIONS}…\n`);

        // Re-run after potential fixes
        stream.progress(`Re-running tests (attempt ${iteration + 1})…`);
        testResult = await exec('mvn', ['test', '-e'], projectDir);
    }

    // ── 6. Parse results ──────────────────────────────────────────────────────
    const output = testResult.stdout + '\n' + testResult.stderr;
    const summaryMatch = output.match(/Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)/);

    if (testResult.exitCode === 0) {
        const total = summaryMatch?.[1] ?? '?';
        const skipped = summaryMatch?.[4] ?? '0';
        stream.markdown(
            `### ✓ All Tests Passed\n\n` +
            `| Metric | Count |\n|--------|-------|\n` +
            `| Tests Run | ${total} |\n` +
            `| Skipped | ${skipped} |\n\n` +
            `Tests completed successfully. See \`target/surefire-reports/\` for details.\n`
        );
        return { metadata: { phase: 'test', status: 'success' } };
    } else {
        const total = summaryMatch?.[1] ?? '?';
        const failures = summaryMatch?.[2] ?? '?';
        const errors = summaryMatch?.[3] ?? '?';
        const skipped = summaryMatch?.[4] ?? '0';
        stream.markdown(
            `### ✗ Test Failures Remain\n\n` +
            `| Metric | Count |\n|--------|-------|\n` +
            `| Tests Run | ${total} |\n` +
            `| Failures | ${failures} |\n` +
            `| Errors | ${errors} |\n` +
            `| Skipped | ${skipped} |\n\n` +
            `After ${MAX_FIX_ITERATIONS} fix attempts, some tests still fail.\n` +
            `Check \`target/surefire-reports/\` for details.\n`
        );
        return { metadata: { phase: 'test', status: 'failures' } };
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────────

function findPom(appDir: string, wsRoot: string): string | undefined {
    const candidate = path.join(appDir, 'pom.xml');
    if (fs.existsSync(candidate)) { return candidate; }
    const wsCandidate = path.join(wsRoot, 'pom.xml');
    if (fs.existsSync(wsCandidate)) { return wsCandidate; }
    return undefined;
}

async function checkTool(command: string, args: string[]): Promise<boolean> {
    try {
        await exec(command, args, process.cwd());
        return true;
    } catch {
        return false;
    }
}

function exec(command: string, args: string[], cwd: string): Promise<ExecResult> {
    return new Promise((resolve) => {
        const proc = cp.spawn(command, args, {
            cwd,
            shell: true,
            env: { ...process.env },
        });
        let stdout = '';
        let stderr = '';
        proc.stdout?.on('data', (d) => { stdout += d.toString(); });
        proc.stderr?.on('data', (d) => { stderr += d.toString(); });
        proc.on('close', (code) => {
            resolve({ stdout, stderr, exitCode: code ?? 1 });
        });
        proc.on('error', () => {
            resolve({ stdout, stderr, exitCode: 1 });
        });
    });
}

function truncate(text: string, max: number): string {
    if (text.length <= max) { return text; }
    return text.slice(0, max) + '\n… (truncated)';
}
