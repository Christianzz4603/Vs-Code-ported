package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.extension.loader.ExtensionPackage
import com.example.model.ExtensionItem
import com.example.ui.theme.EditorColorScheme

@Composable
fun ExtensionsPanel(
    extensions: List<ExtensionItem>,
    installedPackages: List<ExtensionPackage>,
    colors: EditorColorScheme,
    onToggleInstallMarketplace: (String) -> Unit,
    onInstallJarFile: () -> Unit,
    onToggleExtensionEnabled: (String) -> Unit,
    onUninstallExtension: (String) -> Unit,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Installed .JAR, 1 = Marketplace

    val filteredInstalled = remember(searchQuery, installedPackages) {
        if (searchQuery.isBlank()) installedPackages
        else installedPackages.filter {
            it.manifest.name.contains(searchQuery, ignoreCase = true) ||
            it.manifest.displayName.contains(searchQuery, ignoreCase = true) ||
            it.manifest.author.contains(searchQuery, ignoreCase = true) ||
            it.manifest.description.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredMarketplace = remember(searchQuery, extensions) {
        if (searchQuery.isBlank()) extensions
        else extensions.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.publisher.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXTENSIONS ECOSYSTEM",
                    color = colors.sidebarText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                IconButton(
                    onClick = onInstallJarFile,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Install .JAR Extension",
                        tint = colors.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Tabs for Installed .JAR vs Marketplace
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.sidebarBackground,
                contentColor = colors.accentColor,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Installed (${installedPackages.size})", fontSize = 11.sp, color = if (selectedTab == 0) colors.accentColor else colors.sidebarText) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Marketplace", fontSize = 11.sp, color = if (selectedTab == 1) colors.accentColor else colors.sidebarText) }
                )
            }

            // Search input box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter extensions...", fontSize = 11.sp, color = colors.editorLineNumber) },
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

            if (selectedTab == 0) {
                // Installed .JAR Packages Tab
                if (filteredInstalled.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No .JAR extensions loaded.\nClick '+' above to install sample extension.",
                            color = colors.editorLineNumber,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredInstalled, key = { it.manifest.id }) { pkg ->
                            InstalledPackageCard(
                                pkg = pkg,
                                colors = colors,
                                onToggleEnabled = { onToggleExtensionEnabled(pkg.manifest.id) },
                                onUninstall = { onUninstallExtension(pkg.manifest.id) },
                                onExecuteCommand = onExecuteCommand
                            )
                        }
                    }
                }
            } else {
                // Popular Marketplace Tab
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMarketplace, key = { it.id }) { ext ->
                        ExtensionCard(
                            item = ext,
                            colors = colors,
                            onToggleInstall = { onToggleInstallMarketplace(ext.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledPackageCard(
    pkg: ExtensionPackage,
    colors: EditorColorScheme,
    onToggleEnabled: () -> Unit,
    onUninstall: () -> Unit,
    onExecuteCommand: (String) -> Unit
) {
    var isDetailsExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = colors.tabInactiveBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isDetailsExpanded = !isDetailsExpanded }
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = if (pkg.isEnabled) colors.accentColor else colors.editorLineNumber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = pkg.manifest.displayName,
                        color = colors.sidebarText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (pkg.isLoaded) Color(0xFF2E7D32) else Color(0xFFC62828)
                ) {
                    Text(
                        text = if (pkg.isLoaded) ".JAR Active" else "Pending",
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "v${pkg.manifest.version} • ${pkg.manifest.author} • Format: .JAR",
                color = colors.editorLineNumber,
                fontSize = 10.sp
            )

            Text(
                text = pkg.manifest.description,
                color = colors.sidebarText,
                fontSize = 11.sp,
                maxLines = if (isDetailsExpanded) 6 else 2
            )

            if (isDetailsExpanded) {
                HorizontalDivider(color = colors.activityBarBackground, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Permissions: ${pkg.manifest.permissions.joinToString { it.id }}",
                    fontSize = 10.sp,
                    color = colors.editorLineNumber
                )

                Text(
                    text = "Main Entry: ${pkg.manifest.entryPoints.mainClass}",
                    fontSize = 10.sp,
                    color = colors.editorLineNumber
                )

                if (pkg.manifest.commands.isNotEmpty()) {
                    Text(
                        text = "Registered Commands (${pkg.manifest.commands.size}):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.sidebarText
                    )
                    pkg.manifest.commands.forEach { cmd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExecuteCommand(cmd.id) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "▶ ${cmd.title}", fontSize = 10.sp, color = colors.accentColor)
                            cmd.shortcut?.let {
                                Text(text = it, fontSize = 9.sp, color = colors.editorLineNumber)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onToggleEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pkg.isEnabled) colors.activityBarBackground else colors.accentColor
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(
                            text = if (pkg.isEnabled) "Disable" else "Enable",
                            fontSize = 10.sp,
                            color = colors.sidebarText
                        )
                    }

                    IconButton(
                        onClick = onUninstall,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Uninstall .JAR",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = if (isDetailsExpanded) "Less ▲" else "More ▼",
                    fontSize = 10.sp,
                    color = colors.editorLineNumber
                )
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
