package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExtensionItem
import com.example.ui.theme.EditorColorScheme

@Composable
fun ExtensionsPanel(
    extensions: List<ExtensionItem>,
    colors: EditorColorScheme,
    onToggleInstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredExtensions = remember(searchQuery, extensions) {
        if (searchQuery.isBlank()) extensions
        else extensions.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.publisher.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "EXTENSIONS: MARKETPLACE",
                color = colors.sidebarText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Search input box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Extensions in Marketplace", fontSize = 11.sp, color = colors.editorLineNumber) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.sidebarText,
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )

            HorizontalDivider(color = colors.activityBarBackground)

            Text(
                text = "POPULAR EXTENSIONS (${filteredExtensions.size})",
                color = colors.editorLineNumber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredExtensions, key = { it.id }) { ext ->
                    ExtensionCard(
                        item = ext,
                        colors = colors,
                        onToggleInstall = { onToggleInstall(ext.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtensionCard(
    item: ExtensionItem,
    colors: EditorColorScheme,
    onToggleInstall: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = colors.tabInactiveBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = colors.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.name,
                        color = colors.sidebarText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${item.rating}",
                        color = colors.sidebarText,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = "${item.publisher} • v${item.version} • ${item.downloads}",
                color = colors.editorLineNumber,
                fontSize = 10.sp
            )

            Text(
                text = item.description,
                color = colors.sidebarText,
                fontSize = 11.sp,
                maxLines = 2
            )

            Button(
                onClick = onToggleInstall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isInstalled) colors.activityBarBackground else colors.accentColor
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier
                    .height(28.dp)
                    .align(Alignment.End)
            ) {
                Text(
                    text = if (item.isInstalled) "Installed ✓" else "Install",
                    fontSize = 10.sp,
                    color = if (item.isInstalled) colors.sidebarText else Color.White
                )
            }
        }
    }
}
