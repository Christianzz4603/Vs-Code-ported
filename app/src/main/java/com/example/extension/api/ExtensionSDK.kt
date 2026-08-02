package com.example.extension.api

import com.example.extension.manifest.ExtensionManifest
import com.example.extension.manifest.ExtensionPermission
import java.io.File

interface ExtensionLifecycle {
    fun onActivate(context: ExtensionContext)
    fun onDeactivate() {}
}

interface EditorAPI {
    fun getActiveFilePath(): String?
    fun getActiveFileContent(): String?
    fun updateActiveFileContent(newContent: String)
    fun insertTextAtCursor(text: String)
    fun getSelectedText(): String?
}

interface WorkspaceAPI {
    fun getWorkspacePath(): String?
    fun listWorkspaceFiles(): List<String>
    fun readFile(relativePath: String): String?
    fun writeFile(relativePath: String, content: String): Boolean
    fun deleteFile(relativePath: String): Boolean
}

interface CommandRegistry {
    fun registerCommand(commandId: String, handler: (args: List<Any>) -> Any?)
    fun executeCommand(commandId: String, vararg args: Any): Any?
}

interface UIContributionRegistry {
    fun setStatusBarMessage(message: String, timeoutMs: Long = 3000)
    fun addActivityBarTab(id: String, title: String, iconName: String)
    fun showDialog(title: String, message: String, onConfirm: () -> Unit)
}

interface TerminalAPI {
    fun sendTerminalInput(inputCommand: String)
    fun writeTerminalOutput(output: String)
}

interface NotificationAPI {
    fun showInfo(message: String)
    fun showWarning(message: String)
    fun showError(message: String)
}

class SecurityManager(private val grantedPermissions: Set<ExtensionPermission>) {
    fun checkPermission(permission: ExtensionPermission) {
        if (!grantedPermissions.contains(permission)) {
            throw SecurityException(
                "Extension Permission Denied: Extension does not have permission '${permission.id}' (${permission.description})"
            )
        }
    }

    fun hasPermission(permission: ExtensionPermission): Boolean {
        return grantedPermissions.contains(permission)
    }
}

class ExtensionContext(
    val manifest: ExtensionManifest,
    val extensionDir: File,
    val storageDir: File,
    val securityManager: SecurityManager,
    val editorAPI: EditorAPI,
    val workspaceAPI: WorkspaceAPI,
    val commandRegistry: CommandRegistry,
    val uiRegistry: UIContributionRegistry,
    val terminalAPI: TerminalAPI,
    val notificationAPI: NotificationAPI
)
