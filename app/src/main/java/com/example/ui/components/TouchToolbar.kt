package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EditorColorScheme

@Composable
fun TouchToolbar(
    colors: EditorColorScheme,
    onInsertSymbol: (String) -> Unit,
    onOpenCommandPalette: () -> Unit,
    onRunFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickSymbols = listOf("Tab", "//", "{ }", "( )", "[ ]", "\"", "'", ";", ":", "=", "->", "==", "!=", "&&", "||", "<", ">")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp),
        color = colors.activityBarBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Quick Command Palette & Run Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier.clickable { onOpenCommandPalette() },
                    shape = RoundedCornerShape(4.dp),
                    color = colors.accentColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("Ctrl+P", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    modifier = Modifier.clickable { onRunFile() },
                    shape = RoundedCornerShape(4.dp),
                    color = colors.functionColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(12.dp))
                        Text("Run", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Touch Symbol Scroll Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                items(quickSymbols) { sym ->
                    Surface(
                        modifier = Modifier.clickable {
                            val textToInsert = when (sym) {
                                "Tab" -> "    "
                                "{ }" -> "{\n    \n}"
                                "( )" -> "()"
                                "[ ]" -> "[]"
                                else -> sym
                            }
                            onInsertSymbol(textToInsert)
                        },
                        shape = RoundedCornerShape(4.dp),
                        color = colors.tabInactiveBackground
                    ) {
                        Text(
                            text = sym,
                            color = colors.sidebarText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
