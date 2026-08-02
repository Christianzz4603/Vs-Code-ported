package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CodeFileEntity
import com.example.ui.theme.EditorColorScheme
import com.example.util.LanguageUtils

data class FileTemplate(
    val id: String,
    val name: String,
    val extension: String,
    val defaultName: String,
    val initialContent: String
)

val STARTER_TEMPLATES = listOf(
    FileTemplate("empty", "Empty File", "", "file", ""),
    FileTemplate("py", "Python Script", "py", "main.py", "#!/usr/bin/env python3\n\ndef main():\n    print(\"Hello from Python!\")\n\nif __name__ == '__main__':\n    main()\n"),
    FileTemplate("html", "HTML5 Document", "html", "index.html", "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>App</title>\n</head>\n<body>\n    <h1>Hello World</h1>\n</body>\n</html>\n"),
    FileTemplate("js", "JavaScript Module", "js", "script.js", "/**\n * Application Script\n */\nconsole.log('Script loaded successfully.');\n"),
    FileTemplate("ts", "TypeScript Module", "ts", "app.ts", "export interface User {\n    id: number;\n    name: string;\n}\n\nconst user: User = { id: 1, name: 'Alice' };\nconsole.log(user);\n"),
    FileTemplate("cpp", "C++ Program", "cpp", "main.cpp", "#include <iostream>\n\nint main() {\n    std::cout << \"Hello, World!\" << std::endl;\n    return 0;\n}\n"),
    FileTemplate("rs", "Rust Module", "rs", "main.rs", "fn main() {\n    println!(\"Hello, Rust!\");\n}\n"),
    FileTemplate("sh", "Shell Script", "sh", "run.sh", "#!/bin/bash\necho \"Executing script...\"\n"),
    FileTemplate("json", "JSON Config", "json", "config.json", "{\n  \"name\": \"my-app\",\n  \"version\": \"1.0.0\",\n  \"enabled\": true\n}\n"),
    FileTemplate("yaml", "YAML Config", "yaml", "config.yaml", "version: '3'\nservices:\n  app:\n    image: node:20\n    ports:\n      - '3000:3000'\n"),
    FileTemplate("md", "Markdown Doc", "md", "README.md", "# Project Title\n\nBrief description of project.\n\n## Getting Started\n"),
    FileTemplate("docker", "Dockerfile", "", "Dockerfile", "FROM node:20-alpine\nWORKDIR /app\nCOPY package*.json ./\nRUN npm install\nCOPY . .\nCMD [\"node\", \"index.js\"]\n"),
    FileTemplate("make", "Makefile", "", "Makefile", "all:\n\t@echo \"Building project...\"\n\nclean:\n\t@echo \"Cleaning build artifacts...\"\n"),
    FileTemplate("gitignore", "Git Ignore", "", ".gitignore", "node_modules/\nbuild/\n.DS_Store\n*.log\n.env\n"),
    FileTemplate("kt", "Kotlin Source", "kt", "Main.kt", "fun main() {\n    println(\"Hello Kotlin!\")\n}\n"),
    FileTemplate("java", "Java Class", "java", "Main.java", "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello Java!\");\n    }\n}\n")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFileDialog(
    existingFiles: List<CodeFileEntity>,
    colors: EditorColorScheme,
    onDismiss: () -> Unit,
    onCreateFile: (name: String, path: String, content: String, isDirectory: Boolean) -> Unit
) {
    var fileNameInput by remember { mutableStateOf("") }
    var manualExtension by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(STARTER_TEMPLATES.first()) }
    var isDirectory by remember { mutableStateOf(false) }

    // Folder choices
    val folderPaths = remember(existingFiles) {
        val folders = existingFiles.filter { it.isDirectory }.map { it.path }
        listOf("") + folders
    }
    var selectedFolder by remember { mutableStateOf("") }
    var showFolderDropdown by remember { mutableStateOf(false) }

    var favoriteExtensions by remember { mutableStateOf(setOf("py", "js", "json", "html", "md", "cpp", "rs", "kt")) }
    var recentExtensions by remember { mutableStateOf(listOf("py", "json", "js", "html", "md")) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isDirectory) "New Directory" else "New File (VS Code Studio)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.sidebarText
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // File vs Directory Switch Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Type:", fontSize = 12.sp, color = colors.sidebarText)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isDirectory,
                            onClick = { isDirectory = false },
                            label = { Text("File", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = isDirectory,
                            onClick = { isDirectory = true },
                            label = { Text("Folder", fontSize = 11.sp) }
                        )
                    }
                }

                // File Name Textfield
                OutlinedTextField(
                    value = fileNameInput,
                    onValueChange = { fileNameInput = it },
                    label = { Text(if (isDirectory) "Folder Name" else "Filename (e.g. README, script.py, .env)") },
                    placeholder = { Text(if (isDirectory) "e.g. components" else selectedTemplate.defaultName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isDirectory) {
                    // Custom Extension Textfield
                    OutlinedTextField(
                        value = manualExtension,
                        onValueChange = { manualExtension = it.removePrefix(".") },
                        label = { Text("Extension Override (Optional)") },
                        placeholder = { Text("e.g. py, tsx, cpp, yaml") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Favorite / Quick Extension Pills
                    Text("Favorite & Recent Extensions:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.sidebarText)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(favoriteExtensions.toList()) { ext ->
                            SuggestionChip(
                                onClick = {
                                    manualExtension = ext
                                    if (!fileNameInput.contains(".")) {
                                        fileNameInput = if (fileNameInput.isBlank()) "file.$ext" else "$fileNameInput.$ext"
                                    }
                                },
                                label = { Text(".$ext", fontSize = 11.sp) }
                            )
                        }
                    }

                    // Template Selector Carousel
                    Text("Starter Template:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.sidebarText)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        STARTER_TEMPLATES.take(8).forEach { tpl ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTemplate = tpl
                                        if (fileNameInput.isBlank()) {
                                            fileNameInput = tpl.defaultName
                                        }
                                        if (tpl.extension.isNotEmpty()) {
                                            manualExtension = tpl.extension
                                        }
                                    },
                                shape = RoundedCornerShape(6.dp),
                                color = if (selectedTemplate.id == tpl.id) colors.accentColor.copy(alpha = 0.2f) else colors.tabInactiveBackground
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedTemplate.id == tpl.id,
                                        onClick = {
                                            selectedTemplate = tpl
                                            if (fileNameInput.isBlank()) fileNameInput = tpl.defaultName
                                        }
                                    )
                                    Column {
                                        Text(tpl.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.sidebarText)
                                        Text(if (tpl.extension.isEmpty()) "No extension" else ".${tpl.extension}", fontSize = 10.sp, color = colors.editorLineNumber)
                                    }
                                }
                            }
                        }
                    }
                }

                // Parent Folder Selection Dropdown
                Text("Target Folder:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.sidebarText)
                Box {
                    OutlinedButton(
                        onClick = { showFolderDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (selectedFolder.isEmpty()) "📁 Root Workspace (/)" else "📁 $selectedFolder", fontSize = 12.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Folder")
                        }
                    }

                    DropdownMenu(
                        expanded = showFolderDropdown,
                        onDismissRequest = { showFolderDropdown = false }
                    ) {
                        folderPaths.forEach { path ->
                            DropdownMenuItem(
                                text = { Text(if (path.isEmpty()) "📁 Root Workspace (/)" else "📁 $path", fontSize = 12.sp) },
                                onClick = {
                                    selectedFolder = path
                                    showFolderDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var finalName = fileNameInput.trim()
                    if (finalName.isBlank()) {
                        finalName = if (isDirectory) "new_folder" else selectedTemplate.defaultName
                    }

                    if (!isDirectory && manualExtension.isNotBlank() && !finalName.endsWith(".$manualExtension") && !finalName.contains(".")) {
                        finalName = "$finalName.$manualExtension"
                    }

                    val fullPath = if (selectedFolder.isEmpty()) finalName else "$selectedFolder/$finalName"
                    val content = if (isDirectory) "" else if (selectedTemplate.id != "empty" && fileNameInput.isBlank()) selectedTemplate.initialContent else ""

                    onCreateFile(finalName, fullPath, content, isDirectory)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
