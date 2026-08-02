package com.example.data.repository

import com.example.data.local.CodeDao
import com.example.data.local.CodeFileEntity
import com.example.data.local.GitCommitEntity
import com.example.data.local.ProjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CodeRepository(private val codeDao: CodeDao) {

    val allProjects: Flow<List<ProjectEntity>> = codeDao.getAllProjects()

    fun getFilesForProject(projectId: Long): Flow<List<CodeFileEntity>> =
        codeDao.getFilesForProject(projectId)

    fun getCommitsForProject(projectId: Long): Flow<List<GitCommitEntity>> =
        codeDao.getCommitsForProject(projectId)

    suspend fun getFileById(fileId: Long): CodeFileEntity? = codeDao.getFileById(fileId)

    suspend fun createProject(name: String, description: String, templateType: String): Long {
        val project = ProjectEntity(
            name = name,
            description = description,
            templateType = templateType
        )
        val projectId = codeDao.insertProject(project)
        seedProjectFiles(projectId, templateType)
        return projectId
    }

    suspend fun createFile(
        projectId: Long,
        name: String,
        parentPath: String,
        content: String = "",
        isDirectory: Boolean = false
    ): Long {
        val path = if (parentPath.isEmpty()) name else "$parentPath/$name"
        val extension = name.substringAfterLast('.', "")
        val language = detectLanguage(extension, isDirectory)

        val fileEntity = CodeFileEntity(
            projectId = projectId,
            name = name,
            path = path,
            content = content,
            language = language,
            isDirectory = isDirectory,
            parentPath = parentPath
        )
        return codeDao.insertFile(fileEntity)
    }

    suspend fun saveFileContent(fileId: Long, newContent: String) {
        val file = codeDao.getFileById(fileId) ?: return
        val updated = file.copy(
            content = newContent,
            isModified = true,
            lastModified = System.currentTimeMillis()
        )
        codeDao.updateFile(updated)
    }

    suspend fun deleteFile(fileId: Long) {
        val file = codeDao.getFileById(fileId) ?: return
        if (file.isDirectory) {
            codeDao.deleteFilesByPathPrefix(file.projectId, file.path)
        }
        codeDao.deleteFile(file)
    }

    suspend fun commitChanges(projectId: Long, message: String) {
        val files = codeDao.getFilesForProject(projectId).first()
        files.filter { it.isModified }.forEach {
            codeDao.updateFile(it.copy(isModified = false))
        }

        val commitHash = java.util.UUID.randomUUID().toString().take(7)
        val commit = GitCommitEntity(
            projectId = projectId,
            message = message,
            hash = commitHash
        )
        codeDao.insertCommit(commit)
    }

    private fun detectLanguage(extension: String, isDirectory: Boolean): String {
        if (isDirectory) return "folder"
        return when (extension.lowercase()) {
            "kt", "kts" -> "kotlin"
            "java" -> "java"
            "py" -> "python"
            "js", "jsx" -> "javascript"
            "ts", "tsx" -> "typescript"
            "html" -> "html"
            "css" -> "css"
            "json" -> "json"
            "md" -> "markdown"
            "sh", "bash" -> "shell"
            "cpp", "c", "h" -> "cpp"
            "sql" -> "sql"
            "rs" -> "rust"
            "go" -> "go"
            else -> "text"
        }
    }

    suspend fun seedProjectFiles(projectId: Long, templateType: String) {
        when (templateType) {
            "compose" -> {
                createFile(projectId, "src", "", isDirectory = true)
                createFile(projectId, "main", "src", isDirectory = true)
                createFile(projectId, "MainActivity.kt", "src/main", content = """
                    package com.example.app

                    import androidx.compose.foundation.layout.*
                    import androidx.compose.material3.*
                    import androidx.compose.runtime.*
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.unit.dp

                    @Composable
                    fun MainScreen() {
                        var counter by remember { mutableStateOf(0) }
                        
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        ) {
                            Text(
                                text = "Welcome to Code Studio!",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Current count: ${'$'}counter")
                            Button(onClick = { counter++ }) {
                                Text("Increment")
                            }
                        }
                    }
                """.trimIndent())
                createFile(projectId, "build.gradle.kts", "", content = """
                    plugins {
                        id("com.android.application")
                        kotlin("android")
                    }

                    android {
                        namespace = "com.example.app"
                        compileSdk = 35
                    }
                """.trimIndent())
                createFile(projectId, "README.md", "", content = "# Android Jetpack Compose App\n\nBuilt with Kotlin and Jetpack Compose inside Code Studio Mobile IDE.")
            }
            "python" -> {
                createFile(projectId, "main.py", "", content = """
                    # Python AI & Data Processing Script
                    import math
                    import time

                    def analyze_data(values):
                        print("Analyzing dataset of size:", len(values))
                        total = sum(values)
                        avg = total / len(values) if values else 0
                        variance = sum((x - avg) ** 2 for x in values) / len(values) if values else 0
                        std_dev = math.sqrt(variance)
                        return {"mean": avg, "std_dev": std_dev}

                    if __name__ == "__main__":
                        data = [12.5, 45.2, 88.0, 32.1, 99.4, 61.2, 73.8]
                        result = analyze_data(data)
                        print("Analysis result:", result)
                """.trimIndent())
                createFile(projectId, "requirements.txt", "", content = "numpy>=1.21.0\npandas>=1.3.0\nrequests>=2.25.0")
                createFile(projectId, "README.md", "", content = "# Python Data Pipeline\n\nRun `python main.py` in the Code Studio Terminal to execute.")
            }
            "web" -> {
                createFile(projectId, "index.html", "", content = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Code Studio App</title>
                        <link rel="stylesheet" href="style.css">
                    </head>
                    <body>
                        <div class="container">
                            <h1>Hello from Code Studio!</h1>
                            <p>Mobile web application developed on Android.</p>
                            <button id="actionBtn">Click Me</button>
                        </div>
                        <script src="script.js"></script>
                    </body>
                    </html>
                """.trimIndent())
                createFile(projectId, "style.css", "", content = """
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #0f172a;
                        color: #f8fafc;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                    }
                    .container {
                        text-align: center;
                        padding: 2rem;
                        background: #1e293b;
                        border-radius: 12px;
                        box-shadow: 0 4px 20px rgba(0,0,0,0.5);
                    }
                    button {
                        background-color: #3b82f6;
                        color: white;
                        border: none;
                        padding: 10px 20px;
                        border-radius: 6px;
                        font-size: 1rem;
                        cursor: pointer;
                    }
                """.trimIndent())
                createFile(projectId, "script.js", "", content = """
                    document.addEventListener('DOMContentLoaded', () => {
                        const btn = document.getElementById('actionBtn');
                        btn.addEventListener('click', () => {
                            alert('Button clicked in Code Studio Web Runner!');
                        });
                    });
                """.trimIndent())
            }
            else -> {
                // Default Node / Kotlin console
                createFile(projectId, "App.kt", "", content = """
                    fun main() {
                        println("Code Studio Portable IDE v1.0")
                        val items = listOf("Kotlin", "Java", "Python", "JavaScript", "Rust")
                        println("Supported Languages: ${'$'}{items.joinToString()}")
                    }
                """.trimIndent())
                createFile(projectId, "README.md", "", content = "# Code Studio Mobile Project\n\nStart editing files or run terminal commands.")
            }
        }

        // Add initial Git Commit
        commitChanges(projectId, "Initial commit from template ($templateType)")
    }
}
