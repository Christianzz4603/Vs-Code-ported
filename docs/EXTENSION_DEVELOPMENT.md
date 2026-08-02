# Extension Development Guide for Code Studio (.JAR Native Platform)

This guide details how to build, package, compile, and distribute native `.jar` extensions for the Android application platform.

---

## 1. Extension Package Format (.jar)

Every extension is distributed as a single, compiled `.jar` package containing metadata, compiled bytecode, resources, and optional native JNI C/C++/Rust/Go libraries.

```
ExampleExtension.jar
├── META-INF/
│   └── MANIFEST.MF
├── manifest.json
├── classes/
│   └── com/example/extension/MyExtension.class
├── lib/
│   ├── arm64-v8a/
│   │   └── libextension.so
│   ├── armeabi-v7a/
│   │   └── libextension.so
│   ├── x86/
│   └── x86_64/
├── assets/
├── icons/
├── themes/
├── snippets/
├── README.md
└── LICENSE
```

---

## 2. manifest.json Specification

The `manifest.json` file placed in the root of the `.jar` defines extension capabilities, entry points, and requested permissions.

```json
{
  "id": "com.developer.myextension",
  "uuid": "4f183921-992a-431b-b23d-01293881270f",
  "name": "My Custom Extension",
  "displayName": "My Native Custom Extension",
  "version": "1.0.0",
  "description": "An extension providing advanced file analytics and C++ JNI calculations.",
  "author": "Dev Team",
  "category": "Utilities",
  "keywords": ["extension", "analysis", "cpp", "native"],
  "minAppVersion": "1.0.0",
  "maxAppVersion": "99.0.0",
  "apiVersion": "1.0",
  "permissions": [
    "file_system",
    "commands",
    "ui_contribution",
    "terminal",
    "native_code"
  ],
  "entryPoints": {
    "mainClass": "com.developer.myextension.MyExtensionLifecycle",
    "nativeLibrary": "libextension.so"
  },
  "commands": [
    {
      "id": "extension.myextension.runAnalysis",
      "title": "MyExtension: Analyze Current Active File",
      "category": "Analysis",
      "shortcut": "Ctrl+Shift+A"
    }
  ],
  "activationEvents": [
    "onStartup",
    "onCommand:extension.myextension.runAnalysis"
  ]
}
```

---

## 3. Extension API Lifecycle (Kotlin / Java)

Implement the `ExtensionLifecycle` interface:

```kotlin
package com.developer.myextension

import com.example.extension.api.ExtensionContext
import com.example.extension.api.ExtensionLifecycle
import com.example.extension.manifest.ExtensionPermission

class MyExtensionLifecycle : ExtensionLifecycle {

    override fun onActivate(context: ExtensionContext) {
        // 1. Verify permissions
        context.securityManager.checkPermission(ExtensionPermission.UI_CONTRIBUTION)
        context.securityManager.checkPermission(ExtensionPermission.COMMAND_REGISTRATION)

        // 2. Set Status Bar Info
        context.uiRegistry.setStatusBarMessage("🚀 My Native Extension Active", 5000)

        // 3. Register Command
        context.commandRegistry.registerCommand("extension.myextension.runAnalysis") { args ->
            val activeContent = context.editorAPI.getActiveFileContent() ?: ""
            val lineCount = activeContent.lines().size
            context.notificationAPI.showInfo("Analysis Complete: $lineCount lines processed.")
            context.terminalAPI.writeTerminalOutput("⚡ Processed $lineCount lines via native JAR extension.\n")
        }
    }

    override fun onDeactivate() {
        // Cleanup resources
    }
}
```

---

## 4. Native JNI Library Support (.so)

To include compiled C/C++, Rust, or Go JNI libraries:
1. Place compiled `.so` files in `lib/<abi>/` (e.g. `lib/arm64-v8a/libextension.so`).
2. Include `"native_code"` in your `manifest.json` permissions array.
3. Specify `"nativeLibrary": "libextension.so"` under `entryPoints`.
4. The runtime will automatically extract the appropriate ABI `.so` into isolated app storage and invoke `System.load()`.

---

## 5. Gradle Extension Build Template

```kotlin
plugins {
    id("java-library")
    id("kotlin")
}

dependencies {
    implementation(files("libs/code-studio-extension-sdk.jar"))
}

tasks.jar {
    from("src/main/resources") {
        include("manifest.json")
    }
    archiveFileName.set("MyCustomExtension.jar")
}
```

---

## 6. Security & Permission Sandbox

Extensions run within an isolated permission model. If an extension attempts an API call without declaring the corresponding permission in `manifest.json`, a `SecurityException` is thrown:

| Permission | API Access Granted |
| :--- | :--- |
| `file_system` | Read / write files in workspace |
| `commands` | Register and execute commands in Command Palette |
| `ui_contribution` | Status bar, activity bar tabs, dialogs |
| `terminal` | Read / write terminal input and output |
| `native_code` | Load `.so` C/C++/Rust/Go libraries |
| `workspace_access` | Workspace indexer and project state |
