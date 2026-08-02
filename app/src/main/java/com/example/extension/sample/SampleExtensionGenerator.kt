package com.example.extension.sample

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SampleExtensionGenerator {

    private const val TAG = "SampleExtensionGen"

    fun ensureSampleExtensions(context: Context, destinationDir: File) {
        if (!destinationDir.exists()) destinationDir.mkdirs()

        val sampleGitJar = File(destinationDir, "git_supercharge.jar")
        if (!sampleGitJar.exists()) {
            generateGitSuperchargeExtensionJar(sampleGitJar)
        }

        val sampleThemeJar = File(destinationDir, "dracula_pro_theme.jar")
        if (!sampleThemeJar.exists()) {
            generateDraculaThemeExtensionJar(sampleThemeJar)
        }
    }

    private fun generateGitSuperchargeExtensionJar(outFile: File) {
        val manifestJson = """
        {
          "id": "com.code.studio.git.supercharge",
          "uuid": "8f88a912-421b-419b-a312-70b13f173111",
          "name": "Git Supercharge Tools",
          "displayName": "Git Supercharge & History Visualizer",
          "version": "1.2.0",
          "description": "Adds advanced Git status bar indicators, automated commit sync, branch graphs, and diff tools.",
          "author": "Code Studio Extension Team",
          "category": "Source Control",
          "keywords": ["git", "source-control", "commit", "sync", "diff"],
          "minAppVersion": "1.0.0",
          "apiVersion": "1.0",
          "permissions": [
            "file_system",
            "terminal",
            "commands",
            "ui_contribution",
            "workspace_access"
          ],
          "entryPoints": {
            "mainClass": "com.example.extension.sample.GitSuperchargeExtension",
            "nativeLibrary": "libgit_helper.so"
          },
          "commands": [
            {
              "id": "extension.gitSupercharge.sync",
              "title": "Git: Supercharge Fast Sync",
              "category": "Git Supercharge",
              "shortcut": "Ctrl+Alt+G"
            },
            {
              "id": "extension.gitSupercharge.blame",
              "title": "Git: Annotate File Blame Lines",
              "category": "Git Supercharge"
            }
          ],
          "activationEvents": [
            "onStartup",
            "onCommand:extension.gitSupercharge.sync"
          ]
        }
        """.trimIndent()

        createJarArchive(
            outFile = outFile,
            manifestJson = manifestJson,
            additionalFiles = mapOf(
                "README.md" to "# Git Supercharge Extension for Code Studio\nAdds status bar git info and one-click sync.",
                "snippets/git_snippets.json" to "{\"commit\": {\"prefix\": \"gcm\", \"body\": [\"git commit -m \"\$1\"\"], \"description\": \"Git commit snippet\"}}"
            )
        )
        Log.i(TAG, "Generated sample extension JAR: ${outFile.name}")
    }

    private fun generateDraculaThemeExtensionJar(outFile: File) {
        val manifestJson = """
        {
          "id": "com.dracula.theme.extension",
          "uuid": "11aa22bb-33cc-44dd-55ee-66ff77aa88bb",
          "name": "Dracula Official Theme Pack",
          "displayName": "Dracula Pro Theme Pack",
          "version": "2.1.0",
          "description": "Official Dracula dark theme palette for Code Studio Android editor.",
          "author": "Dracula Theme Org",
          "category": "Themes",
          "keywords": ["dracula", "theme", "dark-mode", "syntax"],
          "minAppVersion": "1.0.0",
          "apiVersion": "1.0",
          "permissions": [
            "ui_contribution"
          ],
          "entryPoints": {
            "mainClass": "com.example.extension.sample.DraculaThemeExtension"
          },
          "commands": [
            {
              "id": "extension.theme.applyDracula",
              "title": "Theme: Apply Dracula Pro Dark",
              "category": "Preferences"
            }
          ],
          "activationEvents": [
            "onStartup"
          ]
        }
        """.trimIndent()

        createJarArchive(
            outFile = outFile,
            manifestJson = manifestJson,
            additionalFiles = mapOf(
                "themes/dracula.json" to "{\"name\": \"Dracula Pro\", \"type\": \"dark\", \"colors\": {\"background\": \"#282a36\", \"foreground\": \"#f8f8f2\"}}",
                "LICENSE" to "MIT License\nCopyright (c) 2026 Dracula Theme"
            )
        )
        Log.i(TAG, "Generated sample theme extension JAR: ${outFile.name}")
    }

    private fun createJarArchive(
        outFile: File,
        manifestJson: String,
        additionalFiles: Map<String, String>
    ) {
        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            // 1. MANIFEST.MF
            zos.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
            val manifestMf = "Manifest-Version: 1.0\nCreated-By: Code Studio Extension Builder 1.0\n\n"
            zos.write(manifestMf.toByteArray())
            zos.closeEntry()

            // 2. manifest.json
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestJson.toByteArray())
            zos.closeEntry()

            // 3. Additional files
            for ((path, content) in additionalFiles) {
                zos.putNextEntry(ZipEntry(path))
                zos.write(content.toByteArray())
                zos.closeEntry()
            }
        }
    }
}
