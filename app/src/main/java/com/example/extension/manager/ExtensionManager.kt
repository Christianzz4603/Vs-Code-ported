package com.example.extension.manager

import android.content.Context
import android.util.Log
import com.example.extension.api.*
import com.example.extension.loader.ExtensionPackage
import com.example.extension.loader.JarExtensionLoader
import com.example.extension.manifest.ExtensionPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class ExtensionManager(
    private val context: Context,
    private val editorAPI: EditorAPI,
    private val workspaceAPI: WorkspaceAPI,
    private val commandRegistry: CommandRegistry,
    val uiRegistry: UIContributionRegistry,
    val terminalAPI: TerminalAPI,
    val notificationAPI: NotificationAPI
) {
    private val TAG = "ExtensionManager"

    private val extensionsBaseDir = File(context.filesDir, "installed_extensions").apply {
        if (!exists()) mkdirs()
    }

    private val loader = JarExtensionLoader(context)

    private val _installedPackages = MutableStateFlow<List<ExtensionPackage>>(emptyList())
    val installedPackages: StateFlow<List<ExtensionPackage>> = _installedPackages.asStateFlow()

    private val _registeredExtensionCommands = MutableStateFlow<List<String>>(emptyList())
    val registeredExtensionCommands: StateFlow<List<String>> = _registeredExtensionCommands.asStateFlow()

    fun loadAllExtensions() {
        Log.i(TAG, "Scanning installed extensions in ${extensionsBaseDir.absolutePath}")

        val jarFiles = extensionsBaseDir.listFiles { _, name -> name.endsWith(".jar", ignoreCase = true) } ?: emptyArray()

        val packages = mutableListOf<ExtensionPackage>()

        for (jar in jarFiles) {
            try {
                val pkg = loader.loadExtensionJar(jar, extensionsBaseDir)
                packages.add(pkg)

                if (pkg.isLoaded && pkg.isEnabled && pkg.instance != null) {
                    activatePackage(pkg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed loading extension JAR ${jar.name}: ${e.message}", e)
            }
        }

        _installedPackages.value = packages
    }

    fun installExtensionFromJar(sourceJarFile: File): ExtensionPackage? {
        if (!sourceJarFile.exists()) {
            notificationAPI.showError("Source JAR file does not exist: ${sourceJarFile.name}")
            return null
        }

        try {
            val destJar = File(extensionsBaseDir, sourceJarFile.name)
            sourceJarFile.copyTo(destJar, overwrite = true)

            val pkg = loader.loadExtensionJar(destJar, extensionsBaseDir)

            val currentList = _installedPackages.value.filter { it.manifest.id != pkg.manifest.id }.toMutableList()
            currentList.add(pkg)
            _installedPackages.value = currentList

            if (pkg.isLoaded && pkg.instance != null) {
                activatePackage(pkg)
                notificationAPI.showInfo("Extension installed and activated: ${pkg.manifest.displayName}")
            } else {
                notificationAPI.showWarning("Extension installed (${pkg.manifest.displayName}), but load had warnings: ${pkg.loadError ?: "No active lifecycle class"}")
            }

            return pkg
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install extension from JAR ${sourceJarFile.name}: ${e.message}", e)
            notificationAPI.showError("Failed installing extension: ${e.message}")
            return null
        }
    }

    fun uninstallExtension(extensionId: String) {
        val pkg = _installedPackages.value.find { it.manifest.id == extensionId } ?: return

        try {
            if (pkg.instance != null) {
                pkg.instance.onDeactivate()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating extension $extensionId: ${e.message}")
        }

        if (pkg.jarFile.exists()) {
            pkg.jarFile.delete()
        }

        if (pkg.extensionDir.exists()) {
            pkg.extensionDir.deleteRecursively()
        }

        _installedPackages.value = _installedPackages.value.filter { it.manifest.id != extensionId }
        notificationAPI.showInfo("Extension uninstalled: ${pkg.manifest.displayName}")
    }

    fun toggleExtensionEnabled(extensionId: String) {
        val current = _installedPackages.value
        val updated = current.map { pkg ->
            if (pkg.manifest.id == extensionId) {
                val newEnabled = !pkg.isEnabled
                if (newEnabled) {
                    if (pkg.instance != null) activatePackage(pkg)
                } else {
                    if (pkg.instance != null) {
                        try { pkg.instance.onDeactivate() } catch (_: Exception) {}
                    }
                }
                pkg.copy(isEnabled = newEnabled)
            } else {
                pkg
            }
        }
        _installedPackages.value = updated
    }

    private fun activatePackage(pkg: ExtensionPackage) {
        try {
            val securityManager = SecurityManager(pkg.manifest.permissions)

            val storageDir = File(pkg.extensionDir, "storage").apply { if (!exists()) mkdirs() }

            val extContext = ExtensionContext(
                manifest = pkg.manifest,
                extensionDir = pkg.extensionDir,
                storageDir = storageDir,
                securityManager = securityManager,
                editorAPI = editorAPI,
                workspaceAPI = workspaceAPI,
                commandRegistry = commandRegistry,
                uiRegistry = uiRegistry,
                terminalAPI = terminalAPI,
                notificationAPI = notificationAPI
            )

            // Register commands declared in manifest
            for (cmd in pkg.manifest.commands) {
                commandRegistry.registerCommand(cmd.id) { args ->
                    notificationAPI.showInfo("Executed extension command: ${cmd.title}")
                }
            }

            pkg.instance?.onActivate(extContext)
            Log.i(TAG, "Extension ${pkg.manifest.displayName} activated!")
        } catch (e: Exception) {
            Log.e(TAG, "Crash during activation of extension ${pkg.manifest.displayName}: ${e.message}", e)
            notificationAPI.showError("Extension activation error (${pkg.manifest.name}): ${e.message}")
        }
    }
}
