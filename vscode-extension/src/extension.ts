import * as vscode from 'vscode';
import { registerParticipant } from './participant';

export function activate(context: vscode.ExtensionContext): void {
    // Register the @design-to-code Copilot Chat participant
    registerParticipant(context);

    // Register the command to open the chat panel focused on this participant
    const openChatCmd = vscode.commands.registerCommand('design-to-code.openChat', () => {
        vscode.commands.executeCommand(
            'workbench.action.chat.open',
            { query: '@design-to-code /help' }
        );
    });

    context.subscriptions.push(openChatCmd);
}

export function deactivate(): void {
    // Nothing to clean up — participant subscriptions are disposed automatically
}
