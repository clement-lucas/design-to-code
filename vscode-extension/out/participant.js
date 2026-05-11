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
exports.registerParticipant = registerParticipant;
const vscode = __importStar(require("vscode"));
const extract_1 = require("./phases/extract");
const generate_1 = require("./phases/generate");
const build_1 = require("./phases/build");
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

### Prerequisites
- Python 3.x (for \`/extract\`)
- JDK 17+ and Maven 3.9+ (for \`/build\`)

### Typical session
\`\`\`
@design-to-code /extract
@design-to-code /generate output directory: samples/generated-app
@design-to-code /build app directory: samples/generated-app
\`\`\`
`;
function registerParticipant(context) {
    const participant = vscode.chat.createChatParticipant(PARTICIPANT_ID, handler);
    participant.followupProvider = {
        provideFollowups(result, _ctx, _token) {
            const followups = [];
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
            if (result.metadata?.phase === 'help') {
                followups.push({ prompt: '/extract', label: 'Start Phase 1: Extract design documents', command: 'extract' }, { prompt: '/generate', label: 'Start Phase 2: Generate code', command: 'generate' }, { prompt: '/build', label: 'Start Phase 3: Build & Run', command: 'build' });
            }
            return followups;
        },
    };
    context.subscriptions.push(participant);
}
async function handler(request, _context, stream, token) {
    const workspaceRoot = vscode.workspace.workspaceFolders?.[0]?.uri;
    if (!workspaceRoot) {
        stream.markdown('**No workspace folder open.** Please open a folder that contains your design documents.');
        return {};
    }
    // Route by slash command or detect intent from free-text
    const command = request.command ?? inferCommand(request.prompt);
    switch (command) {
        case 'extract':
            return (0, extract_1.runExtractPhase)(stream, workspaceRoot, token);
        case 'generate': {
            const outputDir = parseOutputDir(request.prompt) ?? 'samples/generated-app';
            return (0, generate_1.runGeneratePhase)(stream, workspaceRoot, outputDir, token);
        }
        case 'build': {
            const appDir = parseAppDir(request.prompt) ?? 'samples/generated-app';
            return (0, build_1.runBuildPhase)(stream, workspaceRoot, appDir, token);
        }
        case 'help':
        default:
            stream.markdown(HELP_TEXT);
            return { metadata: { phase: 'help' } };
    }
}
/** Infer the intended command from free-form text when no slash command is used. */
function inferCommand(prompt) {
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
    return 'help';
}
/** Parse an optional "output directory: <path>" argument from the user prompt. */
function parseOutputDir(prompt) {
    const match = prompt.match(/output\s+(?:dir(?:ectory)?|folder)\s*[:\s]+([^\s]+)/i);
    return match?.[1];
}
/** Parse an optional "app directory: <path>" argument from the user prompt. */
function parseAppDir(prompt) {
    const match = prompt.match(/app(?:lication)?\s+(?:dir(?:ectory)?|folder)\s*[:\s]+([^\s]+)/i);
    return match?.[1];
}
//# sourceMappingURL=participant.js.map