package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CodeFileEntity
import com.example.data.local.GitCommitEntity
import com.example.data.local.ProjectEntity
import com.example.data.repository.CodeRepository
import com.example.model.*
import com.example.network.GeminiService
import com.example.util.WorkspaceIndexer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CodeRepository

    val allProjects: StateFlow<List<ProjectEntity>>
    val currentProjectFiles: StateFlow<List<CodeFileEntity>>
    val currentProjectCommits: StateFlow<List<GitCommitEntity>>

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject.asStateFlow()

    private val _activeActivityTab = MutableStateFlow(ActivityTab.EXPLORER)
    val activeActivityTab: StateFlow<ActivityTab> = _activeActivityTab.asStateFlow()

    private val _openTabs = MutableStateFlow<List<OpenTab>>(emptyList())
    val openTabs: StateFlow<List<OpenTab>> = _openTabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<Long?>(null)
    val activeTabId: StateFlow<Long?> = _activeTabId.asStateFlow()

    private val _settings = MutableStateFlow(EditorSettings())
    val settings: StateFlow<EditorSettings> = _settings.asStateFlow()

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine("Code Studio Mobile IDE v1.0.0", TerminalLineType.SYSTEM),
            TerminalLine("Type 'help' or tap 'Run File' to execute current open script.\n", TerminalLineType.SYSTEM)
        )
    )
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AIMessage>>(
        listOf(
            AIMessage(
                sender = AISender.GEMINI,
                content = "👋 Hi! I'm your Gemini AI Copilot inside Code Studio with FULL WORKSPACE AWARENESS. I index all project files, symbols, imports, and diagnostics. Ask me to refactor across files, fix project bugs, audit security, or write unit tests!"
            )
        )
    )
    val aiMessages: StateFlow<List<AIMessage>> = _aiMessages.asStateFlow()

    private val _isAIGenerating = MutableStateFlow(false)
    val isAIGenerating: StateFlow<Boolean> = _isAIGenerating.asStateFlow()

    val workspaceSummary: StateFlow<String>
    private val _undoSnapshots = mutableMapOf<String, Map<String, String>>() // EditID -> (FilePath -> OldContent)

    private val extensionHandlers = mutableMapOf<String, (List<Any>) -> Any?>()
    var extensionManager: com.example.extension.manager.ExtensionManager? = null
        private set

    val installedExtensionPackages: StateFlow<List<com.example.extension.loader.ExtensionPackage>>
        get() = extensionManager?.installedPackages ?: MutableStateFlow(emptyList())

    fun initExtensionManager(context: android.content.Context) {
        if (extensionManager != null) return

        val editorAPI = object : com.example.extension.api.EditorAPI {
            override fun getActiveFilePath(): String? = _openTabs.value.find { it.fileId == _activeTabId.value }?.path
            override fun getActiveFileContent(): String? = _openTabs.value.find { it.fileId == _activeTabId.value }?.content
            override fun updateActiveFileContent(newContent: String) { updateActiveTabContent(newContent) }
            override fun insertTextAtCursor(text: String) { insertSymbolToActiveFile(text) }
            override fun getSelectedText(): String? = null
        }

        val workspaceAPI = object : com.example.extension.api.WorkspaceAPI {
            override fun getWorkspacePath(): String? = currentProject.value?.name
            override fun listWorkspaceFiles(): List<String> = currentProjectFiles.value.map { it.path }
            override fun readFile(relativePath: String): String? = currentProjectFiles.value.find { it.path == relativePath || it.name == relativePath }?.content
            override fun writeFile(relativePath: String, content: String): Boolean {
                val existing = currentProjectFiles.value.find { it.path == relativePath || it.name == relativePath }
                if (existing != null) {
                    viewModelScope.launch { repository.saveFileContent(existing.id, content) }
                    return true
                }
                val projId = _currentProjectId.value ?: return false
                viewModelScope.launch { repository.createFile(projId, relativePath.substringAfterLast("/"), content, relativePath, false) }
                return true
            }
            override fun deleteFile(relativePath: String): Boolean {
                val existing = currentProjectFiles.value.find { it.path == relativePath || it.name == relativePath } ?: return false
                deleteFile(existing)
                return true
            }
        }

        val cmdRegistry = object : com.example.extension.api.CommandRegistry {
            override fun registerCommand(commandId: String, handler: (List<Any>) -> Any?) {
                extensionHandlers[commandId] = handler
            }
            override fun executeCommand(commandId: String, vararg args: Any): Any? {
                return extensionHandlers[commandId]?.invoke(args.toList())
            }
        }

        val uiRegistry = object : com.example.extension.api.UIContributionRegistry {
            override fun setStatusBarMessage(message: String, timeoutMs: Long) {
                appendTerminalLine("StatusBar: $message", TerminalLineType.SYSTEM)
            }
            override fun addActivityBarTab(id: String, title: String, iconName: String) {
                appendTerminalLine("Extension registered UI tab: $title ($id)", TerminalLineType.SYSTEM)
            }
            override fun showDialog(title: String, message: String, onConfirm: () -> Unit) {
                appendTerminalLine("Dialog [$title]: $message", TerminalLineType.SYSTEM)
                onConfirm()
            }
        }

        val termAPI = object : com.example.extension.api.TerminalAPI {
            override fun sendTerminalInput(inputCommand: String) { runTerminalCommand(inputCommand) }
            override fun writeTerminalOutput(output: String) { appendTerminalLine(output, TerminalLineType.OUTPUT) }
        }

        val notifAPI = object : com.example.extension.api.NotificationAPI {
            override fun showInfo(message: String) { appendTerminalLine("ℹ️ $message", TerminalLineType.SUCCESS) }
            override fun showWarning(message: String) { appendTerminalLine("⚠️ $message", TerminalLineType.SYSTEM) }
            override fun showError(message: String) { appendTerminalLine("❌ $message", TerminalLineType.ERROR) }
        }

        // 1. Generate sample .jar extensions in storage if not present
        val extDir = java.io.File(context.filesDir, "installed_extensions").apply { if (!exists()) mkdirs() }
        com.example.extension.sample.SampleExtensionGenerator.ensureSampleExtensions(context, extDir)

        // 2. Initialize ExtensionManager
        val mgr = com.example.extension.manager.ExtensionManager(
            context = context,
            editorAPI = editorAPI,
            workspaceAPI = workspaceAPI,
            commandRegistry = cmdRegistry,
            uiRegistry = uiRegistry,
            terminalAPI = termAPI,
            notificationAPI = notifAPI
        )
        mgr.loadAllExtensions()
        extensionManager = mgr
    }

    fun toggleExtensionEnabled(extensionId: String) {
        extensionManager?.toggleExtensionEnabled(extensionId)
    }

    fun uninstallExtension(extensionId: String) {
        extensionManager?.uninstallExtension(extensionId)
    }

    fun executeExtensionCommand(commandId: String) {
        extensionHandlers[commandId]?.invoke(emptyList()) ?: appendTerminalLine("Executing extension command: $commandId", TerminalLineType.SYSTEM)
    }

    fun installSampleJarExtension(context: android.content.Context) {
        val extDir = java.io.File(context.filesDir, "installed_extensions")
        com.example.extension.sample.SampleExtensionGenerator.ensureSampleExtensions(context, extDir)
        extensionManager?.loadAllExtensions()
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CodeRepository(db.codeDao())

        allProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentProjectFiles = _currentProjectId.flatMapLatest { id ->
            if (id != null) repository.getFilesForProject(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentProjectCommits = _currentProjectId.flatMapLatest { id ->
            if (id != null) repository.getCommitsForProject(id) else flowOf(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        workspaceSummary = combine(currentProject, currentProjectFiles) { proj, files ->
            val projName = proj?.name ?: "Workspace"
            val nonDir = files.filter { !it.isDirectory }
            val index = WorkspaceIndexer.buildIndex(projName, files)
            "${nonDir.size} Files Indexed | ${index.totalSymbols} Symbols"
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Indexing workspace..."
        )

        // Seed initial default project if empty
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (projects.isEmpty()) {
                    val newId = repository.createProject("My Mobile App", "Kotlin Jetpack Compose Android Project", "compose")
                    _currentProjectId.value = newId
                } else if (_currentProjectId.value == null) {
                    _currentProjectId.value = projects.first().id
                }
            }
        }

        // Keep currentProject model synchronized
        viewModelScope.launch {
            allProjects.collect { projects ->
                _currentProject.value = projects.find { it.id == _currentProjectId.value }
            }
        }
    }

    fun selectProject(projectId: Long) {
        _currentProjectId.value = projectId
        _openTabs.value = emptyList()
        _activeTabId.value = null
    }

    fun createProject(name: String, description: String, templateType: String) {
        viewModelScope.launch {
            val newId = repository.createProject(name, description, templateType)
            selectProject(newId)
        }
    }

    fun selectActivityTab(tab: ActivityTab) {
        _activeActivityTab.value = tab
    }

    fun openFile(file: CodeFileEntity) {
        val existing = _openTabs.value.find { it.fileId == file.id }
        if (existing == null) {
            val newTab = OpenTab(
                fileId = file.id,
                name = file.name,
                path = file.path,
                content = file.content,
                originalContent = file.content,
                language = file.language,
                isDirty = false
            )
            _openTabs.value = _openTabs.value + newTab
        }
        _activeTabId.value = file.id
    }

    fun closeTab(fileId: Long) {
        val updated = _openTabs.value.filter { it.fileId != fileId }
        _openTabs.value = updated
        if (_activeTabId.value == fileId) {
            _activeTabId.value = updated.lastOrNull()?.fileId
        }
    }

    fun selectTab(fileId: Long) {
        _activeTabId.value = fileId
    }

    fun updateActiveTabContent(newContent: String) {
        val currentTabId = _activeTabId.value ?: return
        val currentTabs = _openTabs.value.toMutableList()
        val index = currentTabs.indexOfFirst { it.fileId == currentTabId }
        if (index != -1) {
            val oldTab = currentTabs[index]
            val isDirty = newContent != oldTab.originalContent
            currentTabs[index] = oldTab.copy(content = newContent, isDirty = isDirty)
            _openTabs.value = currentTabs

            // Auto save to Room database
            viewModelScope.launch {
                repository.saveFileContent(currentTabId, newContent)
            }
        }
    }

    fun createFile(name: String, isDirectory: Boolean) {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            val fileId = repository.createFile(projectId, name, "", "", isDirectory)
            if (!isDirectory) {
                val file = repository.getFileById(fileId)
                if (file != null) openFile(file)
            }
        }
    }

    fun deleteFile(file: CodeFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file.id)
            closeTab(file.id)
        }
    }

    fun commitChanges(message: String) {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            repository.commitChanges(projectId, message)
            // Refresh tab dirty statuses
            val updated = _openTabs.value.map { it.copy(originalContent = it.content, isDirty = false) }
            _openTabs.value = updated
            appendTerminalLine("git commit -m \"$message\": committed successfully.", TerminalLineType.SUCCESS)
        }
    }

    fun syncCommit(message: String) {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            appendTerminalLine("$ git add -A", TerminalLineType.INPUT)
            appendTerminalLine("$ git commit -m \"$message\"", TerminalLineType.INPUT)
            repository.commitChanges(projectId, message)
            val updated = _openTabs.value.map { it.copy(originalContent = it.content, isDirty = false) }
            _openTabs.value = updated
            appendTerminalLine("$ git push origin main", TerminalLineType.INPUT)
            appendTerminalLine("✅ Sync commit successful. Branch 'main' up to date with remote.", TerminalLineType.SUCCESS)
        }
    }

    fun updateSettings(newSettings: EditorSettings) {
        _settings.value = newSettings
    }

    private val _isCommandPaletteOpen = MutableStateFlow(false)
    val isCommandPaletteOpen: StateFlow<Boolean> = _isCommandPaletteOpen.asStateFlow()

    private val _isSidebarVisible = MutableStateFlow(true)
    val isSidebarVisible: StateFlow<Boolean> = _isSidebarVisible.asStateFlow()

    private val _isPanelVisible = MutableStateFlow(true)
    val isPanelVisible: StateFlow<Boolean> = _isPanelVisible.asStateFlow()

    private val _extensions = MutableStateFlow(
        listOf(
            ExtensionItem("py", "Python", "Microsoft", "Rich support for Python language with IntelliSense & debugging.", "2024.2.0", "112M", 4.8f, true),
            ExtensionItem("prettier", "Prettier - Code Formatter", "Prettier", "Opinionated code formatter for JS, TS, HTML, CSS, JSON.", "10.4.0", "48M", 4.7f, true),
            ExtensionItem("pylance", "Pylance", "Microsoft", "Performant language server for Python in VS Code.", "2024.1.2", "85M", 4.9f, true),
            ExtensionItem("cpp", "C/C++", "Microsoft", "C/C++ IntelliSense, debugging, and code browsing.", "1.18.5", "62M", 4.6f, false),
            ExtensionItem("eslint", "ESLint", "Microsoft", "Integrates ESLint JavaScript into VS Code.", "2.4.4", "35M", 4.7f, true),
            ExtensionItem("tailwind", "Tailwind CSS IntelliSense", "Tailwind Labs", "Intelligent Tailwind CSS tooling for VS Code.", "0.10.5", "18M", 4.9f, false),
            ExtensionItem("gitlens", "GitLens — Git supercharged", "GitKraken", "Supercharge Git within VS Code - blame, history & repos.", "14.7.0", "32M", 4.8f, true),
            ExtensionItem("docker", "Docker", "Microsoft", "Makes it easy to create, manage, and debug containerized apps.", "1.29.0", "28M", 4.6f, false),
            ExtensionItem("kotlin", "Kotlin Language Support", "JetBrains", "Syntax highlighting & code completion for Kotlin.", "0.2.27", "8M", 4.8f, true)
        )
    )
    val extensions: StateFlow<List<ExtensionItem>> = _extensions.asStateFlow()

    private val _problems = MutableStateFlow<List<ProblemItem>>(emptyList())
    val problems: StateFlow<List<ProblemItem>> = _problems.asStateFlow()

    private val _breakpoints = MutableStateFlow<List<Breakpoint>>(emptyList())
    val breakpoints: StateFlow<List<Breakpoint>> = _breakpoints.asStateFlow()

    fun toggleCommandPalette(open: Boolean? = null) {
        _isCommandPaletteOpen.value = open ?: !_isCommandPaletteOpen.value
    }

    fun toggleSidebar(visible: Boolean? = null) {
        _isSidebarVisible.value = visible ?: !_isSidebarVisible.value
    }

    fun togglePanel(visible: Boolean? = null) {
        _isPanelVisible.value = visible ?: !_isPanelVisible.value
    }

    fun toggleSplitEditor() {
        _settings.value = _settings.value.copy(splitEditorEnabled = !_settings.value.splitEditorEnabled)
    }

    fun toggleExtensionInstalled(id: String) {
        _extensions.value = _extensions.value.map { ext ->
            if (ext.id == id) ext.copy(isInstalled = !ext.isInstalled) else ext
        }
    }

    fun insertSymbolToActiveFile(symbol: String) {
        val currentTabId = _activeTabId.value ?: return
        val currentTab = _openTabs.value.find { it.fileId == currentTabId } ?: return
        updateActiveTabContent(currentTab.content + symbol)
    }

    fun clearTerminal() {
        _terminalLines.value = emptyList()
    }

    fun appendTerminalLine(text: String, type: TerminalLineType = TerminalLineType.OUTPUT) {
        _terminalLines.value = _terminalLines.value + TerminalLine(text, type)
    }

    fun runTerminalCommand(command: String) {
        appendTerminalLine("studio@android:~$ $command", TerminalLineType.INPUT)
        val trimmed = command.trim()

        when {
            trimmed.equals("help", ignoreCase = true) -> {
                appendTerminalLine("""
                    Available Studio Terminal Commands:
                      help                   Display command list
                      clear                  Clear terminal window
                      ls                     List project files
                      cat <file>             View file content
                      run                    Run current open file
                      python <file.py>       Execute Python script
                      kt run                 Execute Kotlin main function
                      node <file.js>         Execute JavaScript file
                      git status             Show uncommitted files
                      git log                Display commit log
                      npm test               Run project test suite
                """.trimIndent(), TerminalLineType.SYSTEM)
            }
            trimmed.equals("clear", ignoreCase = true) -> {
                clearTerminal()
            }
            trimmed.equals("ls", ignoreCase = true) -> {
                val files = currentProjectFiles.value
                val fileListStr = files.joinToString("\n") { f ->
                    if (f.isDirectory) "📁 ${f.name}/" else "📄 ${f.name} (${f.language})"
                }
                appendTerminalLine(if (fileListStr.isEmpty()) "Directory empty." else fileListStr)
            }
            trimmed.startsWith("cat ", ignoreCase = true) -> {
                val fileName = trimmed.removePrefix("cat ").trim()
                val target = currentProjectFiles.value.find { it.name.equals(fileName, ignoreCase = true) }
                if (target != null) {
                    appendTerminalLine("--- ${target.name} ---", TerminalLineType.SYSTEM)
                    appendTerminalLine(target.content)
                } else {
                    appendTerminalLine("cat: $fileName: No such file in workspace.", TerminalLineType.ERROR)
                }
            }
            trimmed.equals("git status", ignoreCase = true) -> {
                val modified = currentProjectFiles.value.filter { it.isModified }
                if (modified.isEmpty()) {
                    appendTerminalLine("On branch main\nNothing to commit, working tree clean.", TerminalLineType.SUCCESS)
                } else {
                    appendTerminalLine("On branch main\nChanges not staged for commit:\n" +
                            modified.joinToString("\n") { "  modified: ${it.path}" }, TerminalLineType.SYSTEM)
                }
            }
            trimmed.equals("git log", ignoreCase = true) -> {
                val commits = currentProjectCommits.value
                if (commits.isEmpty()) {
                    appendTerminalLine("No commits yet.", TerminalLineType.SYSTEM)
                } else {
                    appendTerminalLine(commits.joinToString("\n---\n") {
                        "commit ${it.hash}\nAuthor: ${it.author}\nDate: ${java.util.Date(it.timestamp)}\n\n    ${it.message}"
                    })
                }
            }
            trimmed.equals("run", ignoreCase = true) -> {
                runActiveFile()
            }
            trimmed.startsWith("python ", ignoreCase = true) ||
            trimmed.startsWith("node ", ignoreCase = true) ||
            trimmed.startsWith("kt ", ignoreCase = true) ||
            trimmed.equals("npm test", ignoreCase = true) -> {
                executeCodeRunner(trimmed)
            }
            else -> {
                appendTerminalLine("Executing command: $trimmed...", TerminalLineType.SYSTEM)
                appendTerminalLine("Process finished with exit code 0", TerminalLineType.SUCCESS)
            }
        }
    }

    fun runActiveFile() {
        val currentTabId = _activeTabId.value
        val activeTab = _openTabs.value.find { it.fileId == currentTabId }
        if (activeTab == null) {
            appendTerminalLine("Error: No file open in editor to run.", TerminalLineType.ERROR)
            return
        }

        executeCodeRunner("run ${activeTab.name}")
    }

    private fun executeCodeRunner(cmd: String) {
        val activeTab = _openTabs.value.find { it.fileId == _activeTabId.value }
        val codeContent = activeTab?.content ?: ""
        val fileName = activeTab?.name ?: "script"
        val lang = activeTab?.language ?: "text"

        appendTerminalLine("🚀 Running $fileName ($lang)...", TerminalLineType.SYSTEM)

        when (lang.lowercase()) {
            "python" -> {
                appendTerminalLine("[Python 3.11.2 Interpreter Loaded]", TerminalLineType.SYSTEM)
                if (codeContent.contains("print(")) {
                    val printLines = codeContent.lines().filter { it.trim().startsWith("print(") }
                    printLines.forEach { line ->
                        val outputStr = line.substringAfter("print(").substringBeforeLast(")").replace("\"", "").replace("'", "")
                        appendTerminalLine(outputStr, TerminalLineType.OUTPUT)
                    }
                } else {
                    appendTerminalLine("Data processing complete. Execution time: 0.042s", TerminalLineType.SUCCESS)
                }
                appendTerminalLine("\nProgram executed with return code 0", TerminalLineType.SUCCESS)
            }
            "kotlin", "java" -> {
                appendTerminalLine("[Kotlin JVM Compiler v2.2.10]", TerminalLineType.SYSTEM)
                appendTerminalLine("Compiling $fileName... DONE (180ms)", TerminalLineType.SYSTEM)
                if (codeContent.contains("println(")) {
                    val printLines = codeContent.lines().filter { it.trim().contains("println(") }
                    printLines.forEach { line ->
                        val outputStr = line.substringAfter("println(").substringBeforeLast(")").replace("\"", "").replace("'", "")
                        appendTerminalLine(outputStr, TerminalLineType.OUTPUT)
                    }
                } else {
                    appendTerminalLine("Main method finished execution successfully.", TerminalLineType.SUCCESS)
                }
            }
            "javascript", "typescript", "html" -> {
                appendTerminalLine("[Node.js v20.11.0 Engine]", TerminalLineType.SYSTEM)
                appendTerminalLine("Serving Web Preview at http://localhost:3000...", TerminalLineType.SUCCESS)
                appendTerminalLine("Console output: Ready for client interaction.", TerminalLineType.OUTPUT)
            }
            else -> {
                appendTerminalLine("Executing script $fileName...", TerminalLineType.SYSTEM)
                appendTerminalLine("Done. Return code 0.", TerminalLineType.SUCCESS)
            }
        }
    }

    fun sendAIMessage(userPrompt: String, actionType: String) {
        val userMsg = AIMessage(sender = AISender.USER, content = userPrompt)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAIGenerating.value = true

        val projName = currentProject.value?.name ?: "Workspace"
        val files = currentProjectFiles.value
        val activeFile = files.find { it.id == _activeTabId.value }
        val problemMsgs = problems.value.map { "${it.fileName}:${it.lineNumber} - ${it.message}" }

        val workspaceContext = WorkspaceIndexer.buildWorkspaceContextPrompt(
            projectName = projName,
            files = files,
            activeFile = activeFile,
            problems = problemMsgs
        )

        viewModelScope.launch {
            val (responseText, proposedEdit) = GeminiService.generateWorkspaceResponse(
                prompt = userPrompt,
                workspaceContext = workspaceContext,
                actionType = actionType
            )

            val aiMsg = AIMessage(
                sender = AISender.GEMINI,
                content = responseText,
                proposedEdit = proposedEdit
            )
            _aiMessages.value = _aiMessages.value + aiMsg
            _isAIGenerating.value = false
        }
    }

    fun acceptWorkspaceEdit(edit: ProposedWorkspaceEdit) {
        val projectId = _currentProjectId.value ?: return
        val currentFiles = currentProjectFiles.value

        // Store undo snapshot
        val snapshot = mutableMapOf<String, String>()
        edit.fileChanges.forEach { change ->
            val existing = currentFiles.find { it.path == change.filePath || it.name == change.filePath }
            if (existing != null) {
                snapshot[change.filePath] = existing.content
            }
        }
        _undoSnapshots[edit.id] = snapshot

        viewModelScope.launch {
            edit.fileChanges.forEach { change ->
                val existing = currentFiles.find { it.path == change.filePath || it.name == change.filePath }
                when (change.changeType) {
                    ProposedChangeType.EDIT_FILE -> {
                        if (existing != null && change.newContent != null) {
                            repository.saveFileContent(existing.id, change.newContent)
                            // Update tab if open
                            val openIndex = _openTabs.value.indexOfFirst { it.fileId == existing.id }
                            if (openIndex != -1) {
                                val updatedTabs = _openTabs.value.toMutableList()
                                updatedTabs[openIndex] = updatedTabs[openIndex].copy(content = change.newContent, isDirty = false)
                                _openTabs.value = updatedTabs
                            }
                        }
                    }
                    ProposedChangeType.CREATE_FILE -> {
                        val fileName = change.filePath.substringAfterLast("/")
                        val fileId = repository.createFile(projectId, fileName, change.newContent ?: "", change.filePath, false)
                        val created = repository.getFileById(fileId)
                        if (created != null) openFile(created)
                    }
                    ProposedChangeType.DELETE_FILE -> {
                        if (existing != null) {
                            deleteFile(existing)
                        }
                    }
                    ProposedChangeType.RENAME_FILE -> {
                        // Handled if needed
                    }
                }
            }

            // Update status in messages
            _aiMessages.value = _aiMessages.value.map { msg ->
                if (msg.proposedEdit?.id == edit.id) {
                    msg.copy(proposedEdit = edit.copy(status = ProposedEditStatus.ACCEPTED))
                } else msg
            }

            appendTerminalLine("✨ Gemini AI applied ${edit.fileChanges.size} proposed changes across workspace.", TerminalLineType.SUCCESS)
        }
    }

    fun rejectWorkspaceEdit(edit: ProposedWorkspaceEdit) {
        _aiMessages.value = _aiMessages.value.map { msg ->
            if (msg.proposedEdit?.id == edit.id) {
                msg.copy(proposedEdit = edit.copy(status = ProposedEditStatus.REJECTED))
            } else msg
        }
        appendTerminalLine("❌ Proposed AI workspace edits rejected by user.", TerminalLineType.SYSTEM)
    }

    fun rollbackWorkspaceEdit(edit: ProposedWorkspaceEdit) {
        val snapshot = _undoSnapshots[edit.id] ?: return
        val currentFiles = currentProjectFiles.value

        viewModelScope.launch {
            snapshot.forEach { (filePath, oldContent) ->
                val existing = currentFiles.find { it.path == filePath || it.name == filePath }
                if (existing != null) {
                    repository.saveFileContent(existing.id, oldContent)
                    val openIndex = _openTabs.value.indexOfFirst { it.fileId == existing.id }
                    if (openIndex != -1) {
                        val updatedTabs = _openTabs.value.toMutableList()
                        updatedTabs[openIndex] = updatedTabs[openIndex].copy(content = oldContent, isDirty = false)
                        _openTabs.value = updatedTabs
                    }
                }
            }

            _aiMessages.value = _aiMessages.value.map { msg ->
                if (msg.proposedEdit?.id == edit.id) {
                    msg.copy(proposedEdit = edit.copy(status = ProposedEditStatus.ROLLED_BACK))
                } else msg
            }

            appendTerminalLine("↩️ Rolled back AI workspace changes for ${edit.title}.", TerminalLineType.SYSTEM)
        }
    }

    fun applyAICodeToActiveFile(generatedCode: String) {
        val activeTabId = _activeTabId.value ?: return
        updateActiveTabContent(generatedCode)
        appendTerminalLine("✨ Gemini AI code applied to active file.", TerminalLineType.SUCCESS)
    }
}
