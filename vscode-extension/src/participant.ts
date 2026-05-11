import * as vscode from 'vscode';
import { runExtractPhase } from './phases/extract';
import { runGeneratePhase } from './phases/generate';
import { runBuildPhase } from './phases/build';
import { runTestPhase } from './phases/test';

const PARTICIPANT_ID = 'design-to-code.agent';

const HELP_TEXT = `
## Design to Code — Workflow Overview

Transform Japanese design documents (設計書) into a Spring Boot application in two phases:

**Phase 1 — Extract** \`/extract\`
Parses all \`.xlsx\` and \`.docx\` files found in your workspace.
Extracts text, tables, images, and DrawingML diagrams using Python scripts.
Produces \`extracted/\` output files for the generate phase.

**Phase 2 — Generate** \`/generate\`
Reads the extracted specifications and uses Copilot to generate a complete Spring Boot application:
- JPA entities, repositories, services, controllers
- Thymeleaf templates from screen designs
- Spring Security configuration
- Spring Batch jobs from job-flow designs
- \`schema.sql\`, \`data.sql\`, \`messages.properties\`
- Maven \`pom.xml\` with all required dependencies

**Phase 3 — Build & Run** \`/build\`
Compiles the generated application and launches it:
- Checks Java 17+ and Maven 3.9+ are available
- Runs \`mvn clean compile\` and uses Copilot to auto-fix compilation errors (up to 5 attempts)
- On success, opens a terminal and starts the app with \`mvn spring-boot:run\`
- App available at [http://localhost:8080](http://localhost:8080)

**Phase 4 — Test** \`/test\`
Creates test specifications, generates test code, and executes tests:
- Analyzes source code and design documents to create テスト仕様書
- Generates JUnit 5 + MockMvc + Mockito test code (単体/結合/システムテスト)
- Runs tests with Maven and auto-fixes failures (up to 5 attempts)
- Produces test result reports in Japanese

### Prerequisites
- Python 3.x (for \`/extract\`)
- JDK 17+ and Maven 3.9+ (for \`/build\`)

### Typical session
\`\`\`
@design-to-code /extract
@design-to-code /generate output directory: samples/generated-app
@design-to-code /build app directory: samples/generated-app
@design-to-code /test app directory: samples/generated-app
\`\`\`
`;

export function registerParticipant(context: vscode.ExtensionContext): void {
    const participant = vscode.chat.createChatParticipant(PARTICIPANT_ID, handler);
    participant.followupProvider = {
        provideFollowups(result: vscode.ChatResult, _ctx: vscode.ChatContext, _token: vscode.CancellationToken) {
            const followups: vscode.ChatFollowup[] = [];
            if (result.metadata?.phase === 'extract') {
                followups.push({
                    prompt: '/generate',
                    label: 'Proceed to Phase 2: Generate Spring Boot code',
                    command: 'generate',
                });
            }
            if (result.metadata?.phase === 'generate') {
                followups.push({
                    prompt: '/build',
                    label: 'Proceed to Phase 3: Build & Run',
                    command: 'build',
                });
            }
            if (result.metadata?.phase === 'build') {
                followups.push({
                    prompt: '/test',
                    label: 'Proceed to Phase 4: Test',
                    command: 'test',
                });
            }
            if (result.metadata?.phase === 'help') {
                followups.push(
                    { prompt: '/extract', label: 'Start Phase 1: Extract design documents', command: 'extract' },
                    { prompt: '/generate', label: 'Start Phase 2: Generate code', command: 'generate' },
                    { prompt: '/build', label: 'Start Phase 3: Build & Run', command: 'build' },
                    { prompt: '/test', label: 'Start Phase 4: Test', command: 'test' },
                );
            }
            return followups;
        },
    };
    context.subscriptions.push(participant);
}

async function handler(
    request: vscode.ChatRequest,
    _context: vscode.ChatContext,
    stream: vscode.ChatResponseStream,
    token: vscode.CancellationToken
): Promise<vscode.ChatResult> {

    const workspaceRoot = vscode.workspace.workspaceFolders?.[0]?.uri;
    if (!workspaceRoot) {
        stream.markdown('**No workspace folder open.** Please open a folder that contains your design documents.');
        return {};
    }

    // Route by slash command or detect intent from free-text
    const command = request.command ?? inferCommand(request.prompt);

    switch (command) {
        case 'extract':
            return runExtractPhase(stream, workspaceRoot, token);

        case 'generate': {
            const outputDir = parseOutputDir(request.prompt) ?? 'samples/generated-app';
            return runGeneratePhase(stream, workspaceRoot, outputDir, token);
        }

        case 'build': {
            const appDir = parseAppDir(request.prompt) ?? 'samples/generated-app';
            return runBuildPhase(stream, workspaceRoot, appDir, token);
        }

        case 'test': {
            const appDir = parseAppDir(request.prompt) ?? 'samples/generated-app';
            return runTestPhase(stream, workspaceRoot, appDir, token);
        }

        case 'help':
        default:
            stream.markdown(HELP_TEXT);
            return { metadata: { phase: 'help' } };
    }
}

/** Infer the intended command from free-form text when no slash command is used. */
function inferCommand(prompt: string): string {
    const lower = prompt.toLowerCase();
    if (lower.includes('extract') || lower.includes('parse') || lower.includes('read doc')) {
        return 'extract';
    }
    if (lower.includes('generat') || lower.includes('scaffold') || lower.includes('create app')) {
        return 'generate';
    }
    if (lower.includes('build') || lower.includes('compile') || lower.includes('run') || lower.includes('start')) {
        return 'build';
    }
    if (lower.includes('test') || lower.includes('テスト') || lower.includes('junit') || lower.includes('単体') || lower.includes('結合')) {
        return 'test';
    }
    return 'help';
}

/** Parse an optional "output directory: <path>" argument from the user prompt. */
function parseOutputDir(prompt: string): string | undefined {
    const match = prompt.match(/output\s+(?:dir(?:ectory)?|folder)\s*[:\s]+([^\s]+)/i);
    return match?.[1];
}

/** Parse an optional "app directory: <path>" argument from the user prompt. */
function parseAppDir(prompt: string): string | undefined {
    const match = prompt.match(/app(?:lication)?\s+(?:dir(?:ectory)?|folder)\s*[:\s]+([^\s]+)/i);
    return match?.[1];
}
