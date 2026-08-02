package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ActivityTab
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.getThemePalette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val themeColors = remember(settings.themeMode) { getThemePalette(settings.themeMode) }

            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    color = themeColors.editorBackground
                ) {
                    CodeStudioAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CodeStudioAppScreen(viewModel: MainViewModel) {
    val activeTab by viewModel.activeActivityTab.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val projectFiles by viewModel.currentProjectFiles.collectAsStateWithLifecycle()
    val projectCommits by viewModel.currentProjectCommits.collectAsStateWithLifecycle()
    val openTabs by viewModel.openTabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val terminalLines by viewModel.terminalLines.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isAIGenerating by viewModel.isAIGenerating.collectAsStateWithLifecycle()
    val workspaceSummary by viewModel.workspaceSummary.collectAsStateWithLifecycle()

    val isCommandPaletteOpen by viewModel.isCommandPaletteOpen.collectAsStateWithLifecycle()
    val isSidebarVisible by viewModel.isSidebarVisible.collectAsStateWithLifecycle()
    val isPanelVisible by viewModel.isPanelVisible.collectAsStateWithLifecycle()
    val extensions by viewModel.extensions.collectAsStateWithLifecycle()
    val problems by viewModel.problems.collectAsStateWithLifecycle()
    val breakpoints by viewModel.breakpoints.collectAsStateWithLifecycle()

    val colors = remember(settings.themeMode) { getThemePalette(settings.themeMode) }
    val activeOpenTab = remember(openTabs, activeTabId) { openTabs.find { it.fileId == activeTabId } }
    val modifiedFiles = remember(projectFiles) { projectFiles.filter { it.isModified } }

    Scaffold(
        topBar = {
            TitleBar(
                activeTab = activeOpenTab,
                workspaceName = currentProject?.name ?: "Workspace",
                colors = colors,
                onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                onToggleSidebar = { viewModel.toggleSidebar() },
                onTogglePanel = { viewModel.togglePanel() },
                onToggleSplit = { viewModel.toggleSplitEditor() },
                onNewFile = { viewModel.createFile("Untitled.kt", false) },
                onNewProject = { viewModel.createProject("New Project", "Native Android App", "compose") },
                onRunFile = { viewModel.runActiveFile() }
            )
        },
        bottomBar = {
            StatusBar(
                activeTab = activeOpenTab,
                colors = colors
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.editorBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // 1. Left Vertical Activity Bar
                    ActivityBar(
                        activeTab = activeTab,
                        colors = colors,
                        modifiedFilesCount = modifiedFiles.size,
                        onTabSelected = { tab ->
                            if (activeTab == tab && isSidebarVisible) {
                                viewModel.toggleSidebar(false)
                            } else {
                                viewModel.selectActivityTab(tab)
                                viewModel.toggleSidebar(true)
                            }
                        }
                    )

                    // 2. Collapsible Sidebar Panel according to Active ActivityTab
                    if (isSidebarVisible) {
                        when (activeTab) {
                            ActivityTab.EXPLORER -> {
                                FileExplorer(
                                    currentProject = currentProject,
                                    allProjects = allProjects,
                                    files = projectFiles,
                                    colors = colors,
                                    onProjectSelected = { viewModel.selectProject(it) },
                                    onCreateProject = { name, desc, tpl -> viewModel.createProject(name, desc, tpl) },
                                    onFileSelected = { viewModel.openFile(it) },
                                    onCreateFile = { name, isDir -> viewModel.createFile(name, isDir) },
                                    onDeleteFile = { viewModel.deleteFile(it) }
                                )
                            }
                            ActivityTab.SEARCH -> {
                                GlobalSearch(
                                    files = projectFiles,
                                    colors = colors,
                                    onFileSelected = { viewModel.openFile(it) }
                                )
                            }
                            ActivityTab.SOURCE_CONTROL -> {
                                SourceControl(
                                    modifiedFiles = modifiedFiles,
                                    commits = projectCommits,
                                    colors = colors,
                                    onCommit = { viewModel.commitChanges(it) }
                                )
                            }
                            ActivityTab.DEBUG -> {
                                RunAndDebugPanel(
                                    activeTab = activeOpenTab,
                                    breakpoints = breakpoints,
                                    colors = colors,
                                    onStartDebug = { viewModel.runActiveFile() }
                                )
                            }
                            ActivityTab.EXTENSIONS -> {
                                ExtensionsPanel(
                                    extensions = extensions,
                                    colors = colors,
                                    onToggleInstall = { viewModel.toggleExtensionInstalled(it) }
                                )
                            }
                            ActivityTab.AI_COPILOT -> {
                                AIBotPanel(
                                    messages = aiMessages,
                                    activeTab = activeOpenTab,
                                    isGenerating = isAIGenerating,
                                    workspaceSummary = workspaceSummary,
                                    colors = colors,
                                    onSendMessage = { prompt, type -> viewModel.sendAIMessage(prompt, type) },
                                    onApplyCodeToActiveFile = { code -> viewModel.applyAICodeToActiveFile(code) },
                                    onAcceptWorkspaceEdit = { edit -> viewModel.acceptWorkspaceEdit(edit) },
                                    onRejectWorkspaceEdit = { edit -> viewModel.rejectWorkspaceEdit(edit) },
                                    onRollbackWorkspaceEdit = { edit -> viewModel.rollbackWorkspaceEdit(edit) }
                                )
                            }
                            ActivityTab.TERMINAL -> {}
                            ActivityTab.SETTINGS -> {
                                SettingsSheet(
                                    settings = settings,
                                    colors = colors,
                                    onSettingsChanged = { viewModel.updateSettings(it) }
                                )
                            }
                        }
                    }

                    // 3. Main Center Code Editor Workspace Space
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Breadcrumbs Line
                        if (settings.showBreadcrumbs) {
                            BreadcrumbsBar(
                                activeTab = activeOpenTab,
                                workspaceName = currentProject?.name ?: "Workspace",
                                colors = colors
                            )
                        }

                        // Open Editor Tabs Bar
                        if (openTabs.isNotEmpty()) {
                            EditorTabBar(
                                openTabs = openTabs,
                                activeTabId = activeTabId,
                                colors = colors,
                                onTabSelected = { viewModel.selectTab(it) },
                                onTabClosed = { viewModel.closeTab(it) }
                            )
                        }

                        // Touch Toolbar for landscape quick symbol typing
                        TouchToolbar(
                            colors = colors,
                            onInsertSymbol = { symbol -> viewModel.insertSymbolToActiveFile(symbol) },
                            onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                            onRunFile = { viewModel.runActiveFile() }
                        )

                        // Main Code Editor Component
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            CodeEditor(
                                activeTab = activeOpenTab,
                                settings = settings,
                                colors = colors,
                                onContentChanged = { newContent -> viewModel.updateActiveTabContent(newContent) }
                            )
                        }

                        // Bottom Integrated Panel (Terminal, Problems, Output, Debug Console)
                        if (isPanelVisible) {
                            IntegratedPanel(
                                lines = terminalLines,
                                problems = problems,
                                activeTab = activeOpenTab,
                                colors = colors,
                                onRunCommand = { viewModel.runTerminalCommand(it) },
                                onRunActiveFile = { viewModel.runActiveFile() },
                                onClearTerminal = { viewModel.clearTerminal() },
                                onClosePanel = { viewModel.togglePanel(false) }
                            )
                        }
                    }
                }
            }

            // Command Palette Modal Overlay (Ctrl+P / Ctrl+Shift+P)
            CommandPalette(
                isOpen = isCommandPaletteOpen,
                files = projectFiles,
                colors = colors,
                onDismiss = { viewModel.toggleCommandPalette(false) },
                onFileSelected = { file -> viewModel.openFile(file) },
                onExecuteCommand = { cmd ->
                    when (cmd) {
                        "toggle_terminal" -> viewModel.togglePanel()
                        "open_settings" -> {
                            viewModel.selectActivityTab(ActivityTab.SETTINGS)
                            viewModel.toggleSidebar(true)
                        }
                        "ai_explain" -> {
                            viewModel.selectActivityTab(ActivityTab.AI_COPILOT)
                            viewModel.toggleSidebar(true)
                            viewModel.sendAIMessage("Explain this file code", "explain")
                        }
                        "ai_fix" -> {
                            viewModel.selectActivityTab(ActivityTab.AI_COPILOT)
                            viewModel.toggleSidebar(true)
                            viewModel.sendAIMessage("Fix errors and refactor", "fix")
                        }
                        "run_file" -> viewModel.runActiveFile()
                        "git_commit" -> {
                            viewModel.selectActivityTab(ActivityTab.SOURCE_CONTROL)
                            viewModel.toggleSidebar(true)
                        }
                        "new_file" -> viewModel.createFile("Untitled.kt", false)
                        "toggle_split" -> viewModel.toggleSplitEditor()
                        "open_extensions" -> {
                            viewModel.selectActivityTab(ActivityTab.EXTENSIONS)
                            viewModel.toggleSidebar(true)
                        }
                    }
                }
            )
        }
    }
}
