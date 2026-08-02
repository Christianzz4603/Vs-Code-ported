package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CodeFileEntity
import com.example.model.SearchResult
import com.example.ui.theme.EditorColorScheme

@Composable
fun GlobalSearch(
    files: List<CodeFileEntity>,
    colors: EditorColorScheme,
    onFileSelected: (CodeFileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val results = remember(searchQuery, files) {
        if (searchQuery.isBlank() || searchQuery.length < 2) emptyList()
        else {
            val list = mutableListOf<SearchResult>()
            files.filter { !it.isDirectory }.forEach { file ->
                val lines = file.content.lines()
                lines.forEachIndexed { index, line ->
                    if (line.contains(searchQuery, ignoreCase = true)) {
                        val startIdx = line.indexOf(searchQuery, ignoreCase = true)
                        list.add(
                            SearchResult(
                                fileId = file.id,
                                fileName = file.name,
                                filePath = file.path,
                                lineNumber = index + 1,
                                lineContent = line.trim(),
                                matchStartIndex = startIdx,
                                matchEndIndex = startIdx + searchQuery.length
                            )
                        )
                    }
                }
            }
            list
        }
    }

    Surface(
        modifier = modifier.width(260.dp).fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SEARCH WORKSPACE",
                color = colors.sidebarText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search text...", fontSize = 12.sp, color = colors.editorLineNumber) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.sidebarText,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )

            Text(
                text = "${results.size} results found",
                color = colors.editorLineNumber,
                fontSize = 11.sp
            )

            HorizontalDivider(color = colors.activityBarBackground)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { res ->
                    val matchingFile = files.find { it.id == res.fileId }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { matchingFile?.let { onFileSelected(it) } }
                            .background(colors.tabInactiveBackground)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = res.fileName,
                                color = colors.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Line ${res.lineNumber}",
                                color = colors.editorLineNumber,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = res.lineContent,
                            color = colors.sidebarText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
