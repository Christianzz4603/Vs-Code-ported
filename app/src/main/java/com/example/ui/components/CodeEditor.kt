package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorSettings
import com.example.model.OpenTab
import com.example.model.ProblemItem
import com.example.model.ProblemSeverity
import com.example.ui.theme.EditorColorScheme
import com.example.util.LanguageUtils
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

data class CompletionItem(
    val label: String,
    val kind: String, // Keyword, Function, Variable, Class, Import
    val insertText: String,
    val detail: String = ""
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CodeEditor(
    activeTab: OpenTab?,
    settings: EditorSettings,
    colors: EditorColorScheme,
    symbolInsertFlow: SharedFlow<String>? = null,
    onContentChanged: (String) -> Unit,
    onReportProblems: (List<ProblemItem>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (activeTab == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.editorBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = colors.editorLineNumber, modifier = Modifier.size(48.dp))
                Text(
                    text = "Visual Studio Code (Android Port)",
                    color = colors.sidebarText,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Supports SAF files, IntelliSense, Diagnostics, Multi-cursor & formatting.",
                    color = colors.editorLineNumber,
                    fontSize = 12.sp
                )
            }
        }
        return
    }

    var textFieldValue by remember(activeTab.fileId) {
        mutableStateOf(TextFieldValue(text = activeTab.content, selection = TextRange(activeTab.content.length)))
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Listen to symbol insertions from TouchToolbar
    LaunchedEffect(symbolInsertFlow, activeTab.fileId) {
        symbolInsertFlow?.collect { symbol ->
            val currentText = textFieldValue.text
            val selection = textFieldValue.selection
            val start = minOf(selection.start, selection.end)
            val end = maxOf(selection.start, selection.end)
            val newText = currentText.substring(0, start) + symbol + currentText.substring(end)
            val newCursor = start + symbol.length
            textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor))
            onContentChanged(newText)
            focusRequester.requestFocus()
        }
    }

    // Diagnostics State
    var problems by remember(activeTab.fileId, textFieldValue.text) {
        mutableStateOf(analyzeCodeDiagnostics(activeTab.fileId, activeTab.name, textFieldValue.text))
    }

    LaunchedEffect(problems) {
        onReportProblems(problems)
    }

    // IntelliSense Popup State
    var showIntelliSense by remember { mutableStateOf(false) }
    var completions by remember { mutableStateOf<List<CompletionItem>>(emptyList()) }

    // Hover / Signature Help State
    var hoverInfo by remember { mutableStateOf<String?>(null) }
    var quickFixItem by remember { mutableStateOf<ProblemItem?>(null) }

    // Search & Replace
    var showSearchReplace by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var isRegex by remember { mutableStateOf(false) }
    var isCaseSensitive by remember { mutableStateOf(false) }

    // Outline / Symbol Drawer State
    var showOutlineView by remember { mutableStateOf(false) }

    // Relative Line Numbers option
    var useRelativeLineNumbers by remember { mutableStateOf(false) }

    var breakpoints by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val lines = remember(textFieldValue.text) { textFieldValue.text.lines() }
    val lineCount = maxOf(1, lines.size)

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // Calculate current cursor line & column
    val cursorOffset = textFieldValue.selection.start
    val (cursorLine, cursorCol) = remember(textFieldValue.text, cursorOffset) {
        var currentLine = 1
        var currentCol = 1
        var count = 0
        for (char in textFieldValue.text) {
            if (count >= cursorOffset) break
            if (char == '\n') {
                currentLine++
                currentCol = 1
            } else {
                currentCol++
            }
            count++
        }
        Pair(currentLine, currentCol)
    }

    // Soft Keyboard Inset handling & auto-scroll
    val isImeVisible = WindowInsets.isImeVisible

    // Auto-scroll effect to keep cursor line visible above keyboard
    LaunchedEffect(cursorLine, isImeVisible, textFieldValue.selection) {
        if (verticalScrollState.maxValue > 0) {
            val fontSizeSp = settings.fontSizeSp
            val lineHeightPx = with(density) { (fontSizeSp * 1.45).sp.toPx() }
            val targetY = ((cursorLine - 1) * lineHeightPx).toInt()
            val visibleHeight = verticalScrollState.viewportSize
            if (visibleHeight > 0) {
                val currentScroll = verticalScrollState.value
                if (targetY < currentScroll || targetY + lineHeightPx > currentScroll + visibleHeight) {
                    val scrollGoal = (targetY - visibleHeight / 3).coerceIn(0, verticalScrollState.maxValue)
                    verticalScrollState.animateScrollTo(scrollGoal)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(colors.editorBackground)
    ) {
        // Sticky Breadcrumb Bar
        if (settings.showBreadcrumbs) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(26.dp),
                color = colors.sidebarBackground
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = colors.accentColor, modifier = Modifier.size(12.dp))
                        Text(activeTab.path, fontSize = 11.sp, color = colors.sidebarText, fontFamily = FontFamily.Monospace)
                        Text(">", fontSize = 11.sp, color = colors.editorLineNumber)
                        Text("Line $cursorLine, Col $cursorCol", fontSize = 11.sp, color = colors.accentColor, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Quick Action Buttons
                        Text(
                            text = "Format",
                            fontSize = 11.sp,
                            color = colors.accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                val formatted = LanguageUtils.formatCode(textFieldValue.text, activeTab.language, settings.tabSizeSpaces)
                                textFieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                                onContentChanged(formatted)
                                focusRequester.requestFocus()
                            }
                        )
                        Text(
                            text = "Find",
                            fontSize = 11.sp,
                            color = colors.sidebarText,
                            modifier = Modifier.clickable { showSearchReplace = !showSearchReplace }
                        )
                        Text(
                            text = "Outline",
                            fontSize = 11.sp,
                            color = colors.sidebarText,
                            modifier = Modifier.clickable { showOutlineView = !showOutlineView }
                        )
                    }
                }
            }
            HorizontalDivider(color = colors.activityBarBackground)
        }

        // Search & Replace Bar
        if (showSearchReplace) {
            SearchReplaceBar(
                searchQuery = searchQuery,
                replaceQuery = replaceQuery,
                isRegex = isRegex,
                isCaseSensitive = isCaseSensitive,
                colors = colors,
                onSearchChange = { searchQuery = it },
                onReplaceChange = { replaceQuery = it },
                onToggleRegex = { isRegex = !isRegex },
                onToggleCase = { isCaseSensitive = !isCaseSensitive },
                onReplaceAll = {
                    if (searchQuery.isNotEmpty()) {
                        val newText = if (isRegex) {
                            textFieldValue.text.replace(Regex(searchQuery), replaceQuery)
                        } else if (isCaseSensitive) {
                            textFieldValue.text.replace(searchQuery, replaceQuery)
                        } else {
                            textFieldValue.text.replace(searchQuery, replaceQuery, ignoreCase = true)
                        }
                        textFieldValue = TextFieldValue(text = newText, selection = TextRange(newText.length))
                        onContentChanged(newText)
                        focusRequester.requestFocus()
                    }
                },
                onClose = { showSearchReplace = false }
            )
        }

        // Outline Drawer Overlay
        if (showOutlineView) {
            OutlineViewPanel(
                lines = lines,
                colors = colors,
                onSelectLine = { lineIdx ->
                    showOutlineView = false
                    val fontSizeSp = settings.fontSizeSp
                    val lineHeightPx = (fontSizeSp * 1.45) * density.density
                    val targetY = (lineIdx * lineHeightPx).toInt()
                    coroutineScope.launch {
                        verticalScrollState.animateScrollTo(targetY.coerceIn(0, verticalScrollState.maxValue))
                    }
                }
            )
        }

        // Main Editor Surface
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Line Numbers Gutter
                if (settings.showLineNumbers) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(colors.editorGutterBackground)
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .verticalScroll(verticalScrollState),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount) {
                            val hasBp = breakpoints.contains(i)
                            val hasProblem = problems.any { it.lineNumber == i }
                            val displayNum = if (useRelativeLineNumbers && i != cursorLine) kotlin.math.abs(i - cursorLine).toString() else i.toString()

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable {
                                    breakpoints = if (hasBp) breakpoints - i else breakpoints + i
                                }
                            ) {
                                if (hasBp) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFE53935), shape = CircleShape))
                                } else if (hasProblem) {
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF9800), shape = CircleShape))
                                } else {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Text(
                                    text = displayNum,
                                    color = if (i == cursorLine) colors.accentColor else colors.editorLineNumber,
                                    fontSize = settings.fontSizeSp.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (i == cursorLine) FontWeight.Bold else FontWeight.Normal,
                                    lineHeight = (settings.fontSizeSp * 1.45).sp
                                )
                            }
                        }
                        // Bottom scroll margin space
                        Spacer(modifier = Modifier.height(280.dp))
                    }

                    Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(colors.activityBarBackground))
                }

                // Code Input Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Column {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                val isComposing = newValue.composition != null
                                val updatedText = handleSmartAutoClosing(newValue.text, textFieldValue.text, newValue.selection, isComposing)
                                val newSelection = if (updatedText.length != newValue.text.length) {
                                    TextRange(newValue.selection.start)
                                } else newValue.selection

                                textFieldValue = TextFieldValue(text = updatedText, selection = newSelection, composition = newValue.composition)
                                onContentChanged(updatedText)

                                // Trigger IntelliSense completions only when not composing IME text
                                if (!isComposing) {
                                    val currentWord = getCurrentWord(updatedText, newSelection.start)
                                    if (currentWord.length >= 1 && settings.autoCloseBrackets) {
                                        completions = generateIntelliSenseCompletions(currentWord, activeTab.language, updatedText)
                                        showIntelliSense = completions.isNotEmpty()
                                    } else {
                                        showIntelliSense = false
                                    }
                                } else {
                                    showIntelliSense = false
                                }
                            },
                            textStyle = TextStyle(
                                color = colors.editorTextColor,
                                fontSize = settings.fontSizeSp.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = (settings.fontSizeSp * 1.45).sp
                            ),
                            cursorBrush = SolidColor(colors.accentColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )

                        // Bottom scroll margin space to allow any line to scroll comfortably above soft keyboard
                        Spacer(modifier = Modifier.height(280.dp))
                    }
                }

                // VS Code Minimap Column
                if (settings.minimapEnabled) {
                    MinimapBar(lines = lines, colors = colors, modifier = Modifier.width(55.dp))
                }
            }

            // Floating IntelliSense Suggestions Popup (Rendered above soft keyboard)
            if (showIntelliSense && completions.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 8.dp, start = 48.dp)
                        .widthIn(max = 280.dp)
                        .heightIn(max = 180.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.sidebarBackground,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, colors.accentColor)
                ) {
                    LazyColumn(modifier = Modifier.padding(4.dp)) {
                        items(completions) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val word = getCurrentWord(textFieldValue.text, textFieldValue.selection.start)
                                        val start = textFieldValue.selection.start - word.length
                                        val newText = textFieldValue.text.replaceRange(start, textFieldValue.selection.start, item.insertText)
                                        val newCursor = start + item.insertText.length
                                        textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor))
                                        onContentChanged(newText)
                                        showIntelliSense = false
                                        focusRequester.requestFocus()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(item.kind.take(1).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.accentColor)
                                    Text(item.label, fontSize = 12.sp, color = colors.sidebarText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Text(item.detail, fontSize = 10.sp, color = colors.editorLineNumber)
                            }
                        }
                    }
                }
            }

            // Quick Fix / Problem Diagnostic Alert
            val problemOnCurrentLine = problems.find { it.lineNumber == cursorLine }
            if (problemOnCurrentLine != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF332211),
                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                        Text(problemOnCurrentLine.message, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun handleSmartAutoClosing(newText: String, oldText: String, selection: TextRange, isComposing: Boolean): String {
    if (isComposing) return newText
    if (newText.length == oldText.length + 1 && selection.start > 0) {
        val insertedChar = newText.getOrNull(selection.start - 1) ?: return newText
        val pairMap = mapOf('(' to ")", '[' to "]", '{' to "}", '"' to "\"", '\'' to "'")
        if (pairMap.containsKey(insertedChar)) {
            val closeChar = pairMap[insertedChar]!!
            return newText.substring(0, selection.start) + closeChar + newText.substring(selection.start)
        }
    }
    return newText
}

private fun getCurrentWord(text: String, cursorIndex: Int): String {
    if (cursorIndex <= 0 || cursorIndex > text.length) return ""
    var start = cursorIndex - 1
    while (start >= 0 && (text[start].isLetterOrDigit() || text[start] == '_' || text[start] == '$')) {
        start--
    }
    return text.substring(start + 1, cursorIndex)
}

private fun generateIntelliSenseCompletions(prefix: String, language: String, fullText: String): List<CompletionItem> {
    val keywords = when (language.lowercase()) {
        "python" -> listOf("import", "from", "def", "class", "return", "if", "else", "for", "while", "print", "self")
        "kotlin" -> listOf("fun", "val", "var", "class", "data", "override", "import", "package", "return", "println")
        "javascript", "typescript" -> listOf("function", "const", "let", "import", "export", "return", "console", "async", "await")
        else -> listOf("if", "else", "return", "function", "var", "import", "class")
    }

    val wordsInDocument = fullText.split(Regex("[^a-zA-Z0-9_$]")).filter { it.length > 2 }.distinct()

    val result = mutableListOf<CompletionItem>()
    keywords.filter { it.startsWith(prefix, ignoreCase = true) }.forEach { kw ->
        result.add(CompletionItem(label = kw, kind = "Keyword", insertText = kw, detail = "Keyword"))
    }
    wordsInDocument.filter { it.startsWith(prefix, ignoreCase = true) && !keywords.contains(it) }.forEach { sym ->
        result.add(CompletionItem(label = sym, kind = "Symbol", insertText = sym, detail = "Workspace Symbol"))
    }
    return result
}

private fun analyzeCodeDiagnostics(fileId: Long, fileName: String, code: String): List<ProblemItem> {
    val result = mutableListOf<ProblemItem>()
    val lines = code.lines()

    var openBraces = 0
    var openParens = 0

    lines.forEachIndexed { index, line ->
        openBraces += line.count { it == '{' } - line.count { it == '}' }
        openParens += line.count { it == '(' } - line.count { it == ')' }

        if (line.contains("TODO") || line.contains("FIXME")) {
            result.add(
                ProblemItem(
                    fileId = fileId,
                    fileName = fileName,
                    lineNumber = index + 1,
                    columnNumber = line.indexOf("TODO") + 1,
                    message = "Task annotation found: ${line.trim()}",
                    severity = ProblemSeverity.INFO
                )
            )
        }
    }

    if (openBraces != 0) {
        result.add(
            ProblemItem(
                fileId = fileId,
                fileName = fileName,
                lineNumber = lines.size,
                columnNumber = 1,
                message = "Syntax Error: Unmatched curly braces '{ }' count mismatch.",
                severity = ProblemSeverity.ERROR
            )
        )
    }

    if (openParens != 0) {
        result.add(
            ProblemItem(
                fileId = fileId,
                fileName = fileName,
                lineNumber = lines.size,
                columnNumber = 1,
                message = "Syntax Warning: Parentheses '( )' balance mismatch.",
                severity = ProblemSeverity.WARNING
            )
        )
    }

    return result
}

@Composable
private fun SearchReplaceBar(
    searchQuery: String,
    replaceQuery: String,
    isRegex: Boolean,
    isCaseSensitive: Boolean,
    colors: EditorColorScheme,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onToggleRegex: () -> Unit,
    onToggleCase: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.sidebarBackground
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search regex or text...", fontSize = 11.sp) },
                textStyle = TextStyle(color = colors.sidebarText, fontSize = 11.sp),
                modifier = Modifier.weight(1f).height(38.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = replaceQuery,
                onValueChange = onReplaceChange,
                placeholder = { Text("Replace with...", fontSize = 11.sp) },
                textStyle = TextStyle(color = colors.sidebarText, fontSize = 11.sp),
                modifier = Modifier.weight(1f).height(38.dp),
                singleLine = true
            )

            FilterChip(selected = isRegex, onClick = onToggleRegex, label = { Text(".*", fontSize = 11.sp) })
            FilterChip(selected = isCaseSensitive, onClick = onToggleCase, label = { Text("Aa", fontSize = 11.sp) })

            Button(
                onClick = onReplaceAll,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Replace All", fontSize = 10.sp)
            }

            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.sidebarText, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun OutlineViewPanel(
    lines: List<String>,
    colors: EditorColorScheme,
    onSelectLine: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        color = colors.tabInactiveBackground
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text("DOCUMENT OUTLINE / SYMBOLS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.sidebarText)
            LazyColumn {
                items(lines.mapIndexedNotNull { idx, line -> if (line.contains("fun ") || line.contains("def ") || line.contains("class ") || line.contains("function ")) Pair(idx, line.trim()) else null }) { (idx, symbolStr) ->
                    Text(
                        text = "Line ${idx + 1}: $symbolStr",
                        fontSize = 11.sp,
                        color = colors.accentColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLine(idx) }
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimapBar(
    lines: List<String>,
    colors: EditorColorScheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = colors.tabInactiveBackground
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 4.dp, horizontal = 2.dp)) {
            lines.take(60).forEach { line ->
                val lineLengthFraction = minOf(1f, line.length / 50f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(lineLengthFraction)
                        .height(2.dp)
                        .padding(vertical = 0.5.dp)
                        .background(colors.editorLineNumber.copy(alpha = 0.4f))
                )
            }
        }
    }
}
