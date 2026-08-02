package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorSettings
import com.example.model.ThemeMode
import com.example.ui.theme.EditorColorScheme

@Composable
fun SettingsSheet(
    settings: EditorSettings,
    colors: EditorColorScheme,
    onSettingsChanged: (EditorSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(280.dp).fillMaxHeight(),
        color = colors.sidebarBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = colors.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "EDITOR PREFERENCES",
                    color = colors.sidebarText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            HorizontalDivider(color = colors.activityBarBackground)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme Selection
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = colors.sidebarText, modifier = Modifier.size(16.dp))
                            Text("Color Theme", color = colors.sidebarText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        ThemeMode.entries.forEach { mode ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSettingsChanged(settings.copy(themeMode = mode)) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (settings.themeMode == mode) colors.tabActiveBackground else colors.tabInactiveBackground
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mode.displayName, color = colors.sidebarText, fontSize = 12.sp)
                                    if (settings.themeMode == mode) {
                                        Text("Active", color = colors.accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Font Size Slider
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Font Size: ${settings.fontSizeSp} sp", color = colors.sidebarText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = settings.fontSizeSp.toFloat(),
                            onValueChange = { onSettingsChanged(settings.copy(fontSizeSp = it.toInt())) },
                            valueRange = 10f..24f,
                            steps = 14,
                            colors = SliderDefaults.colors(thumbColor = colors.accentColor, activeTrackColor = colors.accentColor)
                        )
                    }
                }

                // Show Line Numbers Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Show Line Numbers", color = colors.sidebarText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Displays line count in editor gutter", color = colors.editorLineNumber, fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.showLineNumbers,
                            onCheckedChange = { onSettingsChanged(settings.copy(showLineNumbers = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.accentColor)
                        )
                    }
                }

                // Tab Size
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tab Size", color = colors.sidebarText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onSettingsChanged(settings.copy(tabSizeSpaces = 2)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (settings.tabSizeSpaces == 2) colors.accentColor else colors.tabInactiveBackground
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) { Text("2 Spaces", fontSize = 11.sp) }

                            Button(
                                onClick = { onSettingsChanged(settings.copy(tabSizeSpaces = 4)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (settings.tabSizeSpaces == 4) colors.accentColor else colors.tabInactiveBackground
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) { Text("4 Spaces", fontSize = 11.sp) }
                        }
                    }
                }
            }
        }
    }
}
