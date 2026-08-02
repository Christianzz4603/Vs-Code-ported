package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Breakpoint
import com.example.model.EditorSettings
import com.example.model.OpenTab
import com.example.ui.theme.EditorColorScheme

@Composable
fun CodeEditor(
    activeTab: OpenTab?,
    settings: EditorSettings,
    colors: EditorColorScheme,
    onContentChanged: (String) -> Unit,
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
                Text(
                    text = "Visual Studio Code (Port)",
                    color = colors.sidebarText,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(letterSpacing = 1.sp)
                )
                Text(
                    text = "Press Ctrl+P to search files, or open a file from Explorer.",
                    color = colors.editorLineNumber,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    var textValue by remember(activeTab.fileId) { mutableStateOf(activeTab.content) }
    var showSearchReplace by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var breakpoints by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val lines = remember(textValue) { textValue.lines() }
    val lineCount = maxOf(1, lines.size)

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.editorBackground)
    ) {
        // Search & Replace Banner
        if (showSearchReplace) {
            SearchReplaceBanner(
                searchQuery = searchQuery,
                replaceQuery = replaceQuery,
                colors = colors,
                onSearchChange = { searchQuery = it },
                onReplaceChange = { replaceQuery = it },
                onReplaceAll = {
                    if (searchQuery.isNotEmpty()) {
                        val newText = textValue.replace(searchQuery, replaceQuery)
                        textValue = newText
                        onContentChanged(newText)
                    }
                },
                onClose = { showSearchReplace = false }
            )
        }

        // Main Editor Body (Optionally Split View)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Main Editor Pane
            SingleEditorPane(
                textValue = textValue,
                lineCount = lineCount,
                breakpoints = breakpoints,
                settings = settings,
                colors = colors,
                verticalScrollState = verticalScrollState,
                horizontalScrollState = horizontalScrollState,
                onToggleBreakpoint = { lineNum ->
                    breakpoints = if (breakpoints.contains(lineNum)) breakpoints - lineNum else breakpoints + lineNum
                },
                onTextChanged = { newText ->
                    textValue = newText
                    onContentChanged(newText)
                },
                modifier = Modifier.weight(1f)
            )

            // Split View Second Pane
            if (settings.splitEditorEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(colors.activityBarBackground)
                )

                SingleEditorPane(
                    textValue = textValue,
                    lineCount = lineCount,
                    breakpoints = breakpoints,
                    settings = settings,
                    colors = colors,
                    verticalScrollState = verticalScrollState,
                    horizontalScrollState = horizontalScrollState,
                    onToggleBreakpoint = { lineNum ->
                        breakpoints = if (breakpoints.contains(lineNum)) breakpoints - lineNum else breakpoints + lineNum
                    },
                    onTextChanged = { newText ->
                        textValue = newText
                        onContentChanged(newText)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // VS Code Minimap Column
            if (settings.minimapEnabled) {
                MinimapBar(
                    lines = lines,
                    colors = colors,
                    modifier = Modifier.width(60.dp)
                )
            }
        }
    }
}

@Composable
private fun SingleEditorPane(
    textValue: String,
    lineCount: Int,
    breakpoints: Set<Int>,
    settings: EditorSettings,
    colors: EditorColorScheme,
    verticalScrollState: androidx.compose.foundation.ScrollState,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    onToggleBreakpoint: (Int) -> Unit,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxHeight()) {
        // Line numbers & Breakpoints Gutter
        if (settings.showLineNumbers) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(colors.editorGutterBackground)
                    .padding(horizontal = 6.dp, vertical = 8.dp)
                    .verticalScroll(verticalScrollState),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    val hasBp = breakpoints.contains(i)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { onToggleBreakpoint(i) }
                    ) {
                        if (hasBp) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFE53935), shape = CircleShape)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = i.toString(),
                            color = if (hasBp) Color(0xFFE53935) else colors.editorLineNumber,
                            fontSize = settings.fontSizeSp.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = (settings.fontSizeSp * 1.4).sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(colors.activityBarBackground)
            )
        }

        // Code Editor Text Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = onTextChanged,
                textStyle = TextStyle(
                    color = colors.editorTextColor,
                    fontSize = settings.fontSizeSp.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = (settings.fontSizeSp * 1.4).sp
                ),
                cursorBrush = SolidColor(colors.accentColor),
                modifier = Modifier.fillMaxWidth()
            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
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

@Composable
private fun SearchReplaceBanner(
    searchQuery: String,
    replaceQuery: String,
    colors: EditorColorScheme,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.sidebarBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Find text...", fontSize = 11.sp, color = colors.editorLineNumber) },
                textStyle = TextStyle(color = colors.sidebarText, fontSize = 11.sp),
                modifier = Modifier.weight(1f).height(40.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = replaceQuery,
                onValueChange = onReplaceChange,
                placeholder = { Text("Replace text...", fontSize = 11.sp, color = colors.editorLineNumber) },
                textStyle = TextStyle(color = colors.sidebarText, fontSize = 11.sp),
                modifier = Modifier.weight(1f).height(40.dp),
                singleLine = true
            )

            Button(
                onClick = onReplaceAll,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Replace All", fontSize = 11.sp)
            }

            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.sidebarText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
