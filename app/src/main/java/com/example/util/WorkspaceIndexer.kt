package com.example.util

import com.example.data.local.CodeFileEntity
import com.example.model.WorkspaceIndex

object WorkspaceIndexer {

    fun buildIndex(projectName: String, files: List<CodeFileEntity>): WorkspaceIndex {
        val nonDirFiles = files.filter { !it.isDirectory }
        val fileSummaryList = mutableListOf<String>()
        val symbolsMap = mutableMapOf<String, MutableList<String>>()
        val dependencyGraph = mutableMapOf<String, MutableList<String>>()
        var totalSymbolsCount = 0

        val classRegex = "(?:class|interface|object|enum class)\\s+([A-Za-z0-9_]+)".toRegex()
        val functionRegex = "(?:fun|def|function|void|val|var|const)\\s+([A-Za-z0-9_]+)".toRegex()
        val importRegex = "(?:import|require|from|include)\\s+([A-Za-z0-9_.]+)".toRegex()

        for (file in nonDirFiles) {
            val content = file.content
            val path = file.path.ifBlank { file.name }
            val linesCount = content.lines().size
            fileSummaryList.add("• $path (${file.language}, $linesCount lines)")

            // Symbol extraction
            val classes = classRegex.findAll(content).map { it.groupValues[1] }.toList()
            val funcs = functionRegex.findAll(content).map { it.groupValues[1] }.toList()
            val imports = importRegex.findAll(content).map { it.groupValues[1] }.toList()

            val allSymbols = (classes + funcs).distinct()
            totalSymbolsCount += allSymbols.size

            for (symbol in allSymbols) {
                symbolsMap.getOrPut(symbol) { mutableListOf() }.add(path)
            }

            if (imports.isNotEmpty()) {
                dependencyGraph[path] = imports.toMutableList()
            }
        }

        return WorkspaceIndex(
            projectName = projectName,
            totalFiles = nonDirFiles.size,
            totalSymbols = totalSymbolsCount,
            fileSummaryList = fileSummaryList,
            symbolsMap = symbolsMap,
            dependencyGraph = dependencyGraph
        )
    }

    fun buildWorkspaceContextPrompt(
        projectName: String,
        files: List<CodeFileEntity>,
        activeFile: CodeFileEntity?,
        problems: List<String> = emptyList()
    ): String {
        val index = buildIndex(projectName, files)
        val sb = StringBuilder()

        sb.append("=== WORKSPACE OVERVIEW ===\n")
        sb.append("Project Name: ").append(projectName).append("\n")
        sb.append("Total Files: ").append(index.totalFiles).append(", Symbols Indexed: ").append(index.totalSymbols).append("\n")
        sb.append("File Structure:\n")
        index.fileSummaryList.forEach { sb.append(it).append("\n") }
        sb.append("\n")

        if (problems.isNotEmpty()) {
            sb.append("=== COMPILER / WORKSPACE DIAGNOSTICS & PROBLEMS ===\n")
            problems.forEach { sb.append("⚠️ ").append(it).append("\n") }
            sb.append("\n")
        }

        sb.append("=== COMPLETE WORKSPACE CODE CONTENTS ===\n")
        files.filter { !it.isDirectory }.forEach { file ->
            val isCurrent = activeFile?.id == file.id
            val header = if (isCurrent) "[ACTIVE FILE] ${file.path} (${file.language})" else "${file.path} (${file.language})"
            sb.append("--- ").append(header).append(" ---\n")
            sb.append(file.content).append("\n\n")
        }

        sb.append("=== INSTRUCTIONS FOR GEMINI AI COPILOT ===\n")
        sb.append("You are an expert AI Copilot integrated into VS Code for Android with full workspace awareness.\n")
        sb.append("If the user asks for multi-file edits, refactoring, fixing bugs, or creating/deleting files, include proposed structured workspace changes using XML tags in your response so the editor can preview and apply them safely:\n")
        sb.append("<workspace_edit title=\"Short Title\" confidence=\"95\" explanation=\"Reason for edit\">\n")
        sb.append("  <file_edit path=\"exact/file/path\" type=\"EDIT|CREATE|DELETE|RENAME\">\n")
        sb.append("```language\n")
        sb.append("// Entire new content or replacement code\n")
        sb.append("```\n")
        sb.append("  </file_edit>\n")
        sb.append("</workspace_edit>\n")

        return sb.toString()
    }
}
