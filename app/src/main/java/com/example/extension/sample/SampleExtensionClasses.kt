package com.example.extension.sample

import android.util.Log
import com.example.extension.api.ExtensionContext
import com.example.extension.api.ExtensionLifecycle
import com.example.extension.manifest.ExtensionPermission

class GitSuperchargeExtension : ExtensionLifecycle {
    private val TAG = "GitSuperchargeExtension"

    override fun onActivate(context: ExtensionContext) {
        Log.i(TAG, "Activating Git Supercharge Extension (v${context.manifest.version})")

        // Verify security permissions before calling APIs
        context.securityManager.checkPermission(ExtensionPermission.UI_CONTRIBUTION)
        context.securityManager.checkPermission(ExtensionPermission.COMMAND_REGISTRATION)

        context.uiRegistry.setStatusBarMessage("⚡ Git Supercharge Extension Active", 4000)

        context.commandRegistry.registerCommand("extension.gitSupercharge.sync") {
            context.notificationAPI.showInfo("Git Supercharge: Synchronized workspace branch with origin/main")
            context.terminalAPI.writeTerminalOutput("⚡ [Git Supercharge] Branch synchronized successfully.\n")
        }

        context.commandRegistry.registerCommand("extension.gitSupercharge.blame") {
            val activeFile = context.editorAPI.getActiveFilePath() ?: "No file open"
            context.notificationAPI.showInfo("Git Blame for $activeFile: Committed by Lead Developer (2 hours ago)")
        }
    }

    override fun onDeactivate() {
        Log.i(TAG, "Git Supercharge Extension deactivated.")
    }
}

class DraculaThemeExtension : ExtensionLifecycle {
    private val TAG = "DraculaThemeExtension"

    override fun onActivate(context: ExtensionContext) {
        Log.i(TAG, "Activating Dracula Theme Pack Extension (v${context.manifest.version})")

        context.securityManager.checkPermission(ExtensionPermission.UI_CONTRIBUTION)

        context.commandRegistry.registerCommand("extension.theme.applyDracula") {
            context.uiRegistry.setStatusBarMessage("🎨 Applied Dracula Pro Dark Theme", 3000)
            context.notificationAPI.showInfo("Applied Dracula Pro Dark Theme Palette!")
        }
    }

    override fun onDeactivate() {
        Log.i(TAG, "Dracula Theme Pack deactivated.")
    }
}
