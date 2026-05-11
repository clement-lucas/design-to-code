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
exports.runExtractPhase = runExtractPhase;
const vscode = __importStar(require("vscode"));
const path = __importStar(require("path"));
const cp = __importStar(require("child_process"));
const fs = __importStar(require("fs"));
const SCRIPTS_DIR_REL = '../.github/skills/extract-design-docs/scripts';
async function runExtractPhase(stream, workspaceRoot, token) {
    stream.progress('Scanning workspace for design documents…');
    // ── 1. Discover design documents ─────────────────────────────────────────
    const docs = await findDesignDocs(workspaceRoot);
    if (docs.length === 0) {
        stream.markdown('**No design documents found.**\n\n' +
            'Place `.xlsx` or `.docx` files anywhere in the workspace and run `/extract` again.\n\n' +
            'Expected location: `samples/design-docs/` or any subdirectory.');
        return {};
    }
    stream.markdown(`Found **${docs.length} design document(s)**:\n`);
    for (const d of docs.slice(0, 20)) {
        stream.markdown(`- \`${d}\`\n`);
    }
    if (docs.length > 20) {
        stream.markdown(`- *(and ${docs.length - 20} more…)*\n`);
    }
    // ── 2. Resolve paths ──────────────────────────────────────────────────────
    const wsPath = workspaceRoot.fsPath;
    const scriptsDir = resolveScriptsDir(wsPath);
    if (!scriptsDir) {
        stream.markdown('**Extraction scripts not found.**\n\n' +
            'Expected Python scripts in `.github/skills/extract-design-docs/scripts/`. ' +
            'Clone the full `design-to-code` repository or copy the scripts folder into place.');
        return {};
    }
    const outputDir = path.join(wsPath, 'extracted');
    fs.mkdirSync(outputDir, { recursive: true });
    // ── 3. Verify Python ──────────────────────────────────────────────────────
    const python = await findPython();
    if (!python) {
        stream.markdown('**Python 3 not found.**\n\n' +
            'Install Python 3.x and make sure it is on your `PATH`, then run `/extract` again.');
        return {};
    }
    if (token.isCancellationRequested) {
        return {};
    }
    // ── 4. Detect the best input directory (common ancestor of the docs) ──────
    const docsRoot = findCommonRoot(docs, wsPath);
    // ── 5. Run extract_text.py ────────────────────────────────────────────────
    const textOut = path.join(outputDir, 'extracted_text.txt');
    stream.progress('Running text extraction (extract_text.py)…');
    const textScript = path.join(scriptsDir, 'extract_text.py');
    const textResult = await runPython(python, textScript, [docsRoot, textOut]);
    if (textResult.exitCode !== 0) {
        stream.markdown(`**Text extraction failed** (exit ${textResult.exitCode}):\n\`\`\`\n${textResult.stderr}\n\`\`\``);
        return {};
    }
    stream.markdown(`Text extraction complete → \`extracted/extracted_text.txt\`\n`);
    if (token.isCancellationRequested) {
        return {};
    }
    // ── 6. Run extract_images.py ──────────────────────────────────────────────
    const imagesOut = path.join(outputDir, 'images');
    fs.mkdirSync(imagesOut, { recursive: true });
    stream.progress('Running image extraction (extract_images.py)…');
    const imgScript = path.join(scriptsDir, 'extract_images.py');
    const imgResult = await runPython(python, imgScript, [docsRoot, imagesOut]);
    if (imgResult.exitCode !== 0) {
        stream.markdown(`**Image extraction failed** (exit ${imgResult.exitCode}):\n\`\`\`\n${imgResult.stderr}\n\`\`\``);
        // Non-fatal — continue
    }
    else {
        const imageCount = countFiles(imagesOut);
        stream.markdown(`Image extraction complete → \`extracted/images/\` (${imageCount} image(s))\n`);
    }
    if (token.isCancellationRequested) {
        return {};
    }
    // ── 7. Run extract_drawingml.py ───────────────────────────────────────────
    const drawingmlOut = path.join(outputDir, 'extracted_drawingml.txt');
    stream.progress('Running DrawingML extraction (extract_drawingml.py)…');
    const drawScript = path.join(scriptsDir, 'extract_drawingml.py');
    const drawResult = await runPython(python, drawScript, [docsRoot, drawingmlOut]);
    if (drawResult.exitCode !== 0) {
        stream.markdown(`**DrawingML extraction failed** (exit ${drawResult.exitCode}):\n\`\`\`\n${drawResult.stderr}\n\`\`\``);
        // Non-fatal — continue
    }
    else {
        stream.markdown(`DrawingML extraction complete → \`extracted/extracted_drawingml.txt\`\n`);
    }
    // ── 8. Summary ────────────────────────────────────────────────────────────
    const textLines = countLines(textOut);
    stream.markdown(`\n---\n` +
        `### Phase 1 Complete ✓\n\n` +
        `| Output | Location |\n` +
        `|--------|----------|\n` +
        `| Text/tables | \`extracted/extracted_text.txt\` (${textLines} lines) |\n` +
        `| Images | \`extracted/images/\` |\n` +
        `| DrawingML diagrams | \`extracted/extracted_drawingml.txt\` |\n\n` +
        `Run \`@design-to-code /generate\` to produce the Spring Boot application.`);
    return { metadata: { phase: 'extract', outputDir } };
}
// ── Helpers ───────────────────────────────────────────────────────────────────
async function findDesignDocs(root) {
    const pattern = new vscode.RelativePattern(root, '**/*.{xlsx,docx,XLSX,DOCX}');
    const uris = await vscode.workspace.findFiles(pattern, '**/node_modules/**');
    return uris.map(u => vscode.workspace.asRelativePath(u));
}
function resolveScriptsDir(wsPath) {
    // Try the path relative to the workspace root (standard repo layout)
    const candidate = path.resolve(wsPath, SCRIPTS_DIR_REL);
    if (fs.existsSync(path.join(candidate, 'extract_text.py'))) {
        return candidate;
    }
    // Try directly inside the workspace (if the user opened the extension folder)
    const candidate2 = path.join(wsPath, '.github', 'skills', 'extract-design-docs', 'scripts');
    if (fs.existsSync(path.join(candidate2, 'extract_text.py'))) {
        return candidate2;
    }
    return undefined;
}
async function findPython() {
    for (const cmd of ['python3', 'python', 'py']) {
        try {
            const result = await execAsync(cmd, ['--version']);
            if (result.exitCode === 0 && result.stdout.toLowerCase().includes('python 3')) {
                return cmd;
            }
        }
        catch {
            // try next
        }
    }
    return undefined;
}
function findCommonRoot(relativePaths, wsPath) {
    // Find the deepest common directory of all documents
    const parts = relativePaths.map(p => path.dirname(p).split(path.sep));
    const common = parts[0].slice();
    for (const p of parts.slice(1)) {
        let i = 0;
        while (i < common.length && i < p.length && common[i] === p[i]) {
            i++;
        }
        common.splice(i);
    }
    const rel = common.join(path.sep);
    return rel ? path.join(wsPath, rel) : wsPath;
}
function countLines(filePath) {
    if (!fs.existsSync(filePath)) {
        return 0;
    }
    const content = fs.readFileSync(filePath, 'utf-8');
    return content.split('\n').length;
}
function countFiles(dir) {
    if (!fs.existsSync(dir)) {
        return 0;
    }
    return fs.readdirSync(dir).filter(f => !f.startsWith('.')).length;
}
function execAsync(cmd, args) {
    return new Promise(resolve => {
        const proc = cp.spawn(cmd, args, { shell: true });
        let stdout = '';
        let stderr = '';
        proc.stdout?.on('data', (d) => { stdout += d.toString(); });
        proc.stderr?.on('data', (d) => { stderr += d.toString(); });
        proc.on('close', code => resolve({ exitCode: code ?? 1, stdout, stderr }));
        proc.on('error', err => resolve({ exitCode: 1, stdout: '', stderr: err.message }));
    });
}
function runPython(python, scriptPath, args) {
    return execAsync(python, [scriptPath, ...args]);
}
//# sourceMappingURL=extract.js.map