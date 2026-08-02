package com.example.model

enum class ActivityTab {
    EXPLORER,
    SEARCH,
    SOURCE_CONTROL,
    DEBUG,
    EXTENSIONS,
    AI_COPILOT,
    TERMINAL,
    SETTINGS
}

enum class ThemeMode(val displayName: String) {
    VS_CODE_DARK("VS Code Dark Modern"),
    MONOKAI_PRO("Monokai Pro"),
    ONE_DARK_PRO("One Dark Pro"),
    DRACULA("Dracula Dark"),
    SOLARIZED_DARK("Solarized Dark"),
    GITHUB_LIGHT("GitHub Light")
}

data class OpenTab(
    val fileId: Long,
    val name: String,
    val path: String,
    var content: String,
    val originalContent: String,
    val language: String,
    val isDirty: Boolean = false
)

data class FileTreeNode(
    val id: Long,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val language: String,
    val children: List<FileTreeNode> = emptyList(),
    val isExpanded: Boolean = false
)

enum class TerminalLineType {
    INPUT,
    OUTPUT,
    ERROR,
    SUCCESS,
    SYSTEM
}

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT,
    val timestamp: Long = System.currentTimeMillis()
)

data class SearchResult(
    val fileId: Long,
    val fileName: String,
    val filePath: String,
    val lineNumber: Int,
    val lineContent: String,
    val matchStartIndex: Int,
    val matchEndIndex: Int
)

enum class ProposedChangeType {
    EDIT_FILE,
    CREATE_FILE,
    DELETE_FILE,
    RENAME_FILE
}

enum class ProposedEditStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    ROLLED_BACK
}

data class ProposedFileChange(
    val fileId: Long? = null,
    val filePath: String,
    val changeType: ProposedChangeType,
    val oldContent: String? = null,
    val newContent: String? = null,
    val explanation: String = ""
)

data class ProposedWorkspaceEdit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val confidenceScore: Int = 95,
    val explanation: String,
    val fileChanges: List<ProposedFileChange>,
    var status: ProposedEditStatus = ProposedEditStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

data class WorkspaceIndex(
    val projectName: String,
    val totalFiles: Int,
    val totalSymbols: Int,
    val fileSummaryList: List<String>,
    val symbolsMap: Map<String, List<String>>, // Symbol -> list of file paths where found
    val dependencyGraph: Map<String, List<String>> // File -> imported files
)

data class AIMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: AISender,
    val content: String,
    val codeSnippet: String? = null,
    val proposedEdit: ProposedWorkspaceEdit? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AISender {
    USER,
    GEMINI
}

data class EditorSettings(
    val themeMode: ThemeMode = ThemeMode.VS_CODE_DARK,
    val fontSizeSp: Int = 14,
    val tabSizeSpaces: Int = 4,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val autoCloseBrackets: Boolean = true,
    val minimapEnabled: Boolean = true,
    val showBreadcrumbs: Boolean = true,
    val splitEditorEnabled: Boolean = false
)

data class ExtensionItem(
    val id: String,
    val name: String,
    val publisher: String,
    val description: String,
    val version: String,
    val downloads: String,
    val rating: Float,
    val isInstalled: Boolean = false,
    val iconCategory: String = "code"
)

data class CommandPaletteItem(
    val id: String,
    val title: String,
    val category: String = "Command",
    val shortcut: String? = null,
    val action: () -> Unit
)

enum class ProblemSeverity {
    ERROR,
    WARNING,
    INFO
}

data class ProblemItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileId: Long,
    val fileName: String,
    val lineNumber: Int,
    val columnNumber: Int,
    val message: String,
    val severity: ProblemSeverity,
    val source: String = "compiler"
)

enum class PanelTab {
    TERMINAL,
    PROBLEMS,
    OUTPUT,
    DEBUG_CONSOLE,
    GEMINI_COPILOT
}

data class Breakpoint(
    val fileId: Long,
    val lineNumber: Int,
    val isEnabled: Boolean = true
)

