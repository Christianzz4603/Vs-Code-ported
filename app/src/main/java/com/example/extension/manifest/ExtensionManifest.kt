package com.example.extension.manifest

import org.json.JSONArray
import org.json.JSONObject

enum class ExtensionPermission(val id: String, val description: String) {
    FILE_SYSTEM("file_system", "Read and write workspace files"),
    NETWORK("network", "Access network and external web services"),
    TERMINAL("terminal", "Execute shell commands and manage terminal sessions"),
    COMMAND_REGISTRATION("commands", "Register custom commands in Command Palette"),
    UI_CONTRIBUTION("ui_contribution", "Add status bar items, panels, and menus"),
    NATIVE_CODE("native_code", "Load compiled native (.so) JNI libraries"),
    WORKSPACE_ACCESS("workspace_access", "Read workspace indexing and project state"),
    DIAGNOSTICS("diagnostics", "Access code diagnostics and problem lists");

    companion object {
        fun fromId(id: String): ExtensionPermission? {
            return entries.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
        }
    }
}

enum class ActivationEvent(val eventPattern: String) {
    ON_STARTUP("onStartup"),
    ON_COMMAND("onCommand:"),
    ON_LANGUAGE("onLanguage:"),
    ON_VIEW("onView:"),
    ALWAYS("*");

    companion object {
        fun matches(eventStr: String, currentEvent: String): Boolean {
            if (eventStr == "*" || eventStr == "onStartup") return true
            return currentEvent.startsWith(eventStr) || eventStr.startsWith(currentEvent)
        }
    }
}

data class ExtensionCommandSpec(
    val id: String,
    val title: String,
    val category: String = "Extension",
    val shortcut: String? = null,
    val icon: String? = null
)

data class ExtensionMenuSpec(
    val id: String,
    val label: String,
    val command: String,
    val group: String = "navigation"
)

data class ExtensionEntryPoints(
    val mainClass: String,
    val nativeLibrary: String? = null
)

data class ExtensionManifest(
    val id: String,
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val displayName: String,
    val version: String,
    val description: String,
    val author: String,
    val homepage: String = "",
    val repository: String = "",
    val license: String = "MIT",
    val category: String = "Utility",
    val keywords: List<String> = emptyList(),
    val minAppVersion: String = "1.0.0",
    val maxAppVersion: String = "99.0.0",
    val apiVersion: String = "1.0",
    val permissions: Set<ExtensionPermission> = emptySet(),
    val entryPoints: ExtensionEntryPoints,
    val nativeLibraries: List<String> = emptyList(),
    val commands: List<ExtensionCommandSpec> = emptyList(),
    val menus: List<ExtensionMenuSpec> = emptyList(),
    val settingsSchema: Map<String, Any> = emptyMap(),
    val dependencies: List<String> = emptyList(),
    val optionalDependencies: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val activationEvents: List<String> = listOf("onStartup")
) {
    companion object {
        fun parseFromJson(jsonStr: String): ExtensionManifest {
            val json = JSONObject(jsonStr)

            val permissionsList = mutableSetOf<ExtensionPermission>()
            if (json.has("permissions")) {
                val array = json.getJSONArray("permissions")
                for (i in 0 until array.length()) {
                    ExtensionPermission.fromId(array.getString(i))?.let { permissionsList.add(it) }
                }
            }

            val entryObj = json.getJSONObject("entryPoints")
            val entryPoints = ExtensionEntryPoints(
                mainClass = entryObj.getString("mainClass"),
                nativeLibrary = if (entryObj.has("nativeLibrary")) entryObj.getString("nativeLibrary") else null
            )

            val commandsList = mutableListOf<ExtensionCommandSpec>()
            if (json.has("commands")) {
                val array = json.getJSONArray("commands")
                for (i in 0 until array.length()) {
                    val cmdObj = array.getJSONObject(i)
                    commandsList.add(
                        ExtensionCommandSpec(
                            id = cmdObj.getString("id"),
                            title = cmdObj.getString("title"),
                            category = cmdObj.optString("category", "Extension"),
                            shortcut = cmdObj.optString("shortcut", null),
                            icon = cmdObj.optString("icon", null)
                        )
                    )
                }
            }

            val menusList = mutableListOf<ExtensionMenuSpec>()
            if (json.has("menus")) {
                val array = json.getJSONArray("menus")
                for (i in 0 until array.length()) {
                    val menuObj = array.getJSONObject(i)
                    menusList.add(
                        ExtensionMenuSpec(
                            id = menuObj.getString("id"),
                            label = menuObj.getString("label"),
                            command = menuObj.getString("command"),
                            group = menuObj.optString("group", "navigation")
                        )
                    )
                }
            }

            val nativeLibs = mutableListOf<String>()
            if (json.has("nativeLibraries")) {
                val array = json.getJSONArray("nativeLibraries")
                for (i in 0 until array.length()) {
                    nativeLibs.add(array.getString(i))
                }
            }

            val keywordsList = mutableListOf<String>()
            if (json.has("keywords")) {
                val array = json.getJSONArray("keywords")
                for (i in 0 until array.length()) {
                    keywordsList.add(array.getString(i))
                }
            }

            val activationList = mutableListOf<String>()
            if (json.has("activationEvents")) {
                val array = json.getJSONArray("activationEvents")
                for (i in 0 until array.length()) {
                    activationList.add(array.getString(i))
                }
            } else {
                activationList.add("onStartup")
            }

            return ExtensionManifest(
                id = json.getString("id"),
                uuid = json.optString("uuid", java.util.UUID.randomUUID().toString()),
                name = json.getString("name"),
                displayName = json.optString("displayName", json.getString("name")),
                version = json.getString("version"),
                description = json.optString("description", ""),
                author = json.optString("author", "Unknown"),
                homepage = json.optString("homepage", ""),
                repository = json.optString("repository", ""),
                license = json.optString("license", "MIT"),
                category = json.optString("category", "Utility"),
                keywords = keywordsList,
                minAppVersion = json.optString("minAppVersion", "1.0.0"),
                maxAppVersion = json.optString("maxAppVersion", "99.0.0"),
                apiVersion = json.optString("apiVersion", "1.0"),
                permissions = permissionsList,
                entryPoints = entryPoints,
                nativeLibraries = nativeLibs,
                commands = commandsList,
                menus = menusList,
                activationEvents = activationList
            )
        }
    }
}
