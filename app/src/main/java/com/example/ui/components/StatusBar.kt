package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OpenTab
import com.example.ui.theme.EditorColorScheme

@Composable
fun StatusBar(
    activeTab: OpenTab?,
    colors: EditorColorScheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(24.dp),
        color = colors.statusBarBackground
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = colors.statusBarText,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "main*",
                        color = colors.statusBarText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.statusBarText,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "0 ⊗ 0 △",
                        color = colors.statusBarText,
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ln 1, Col 1",
                    color = colors.statusBarText,
                    fontSize = 10.sp
                )

                Text(
                    text = "Spaces: 4",
                    color = colors.statusBarText,
                    fontSize = 10.sp
                )

                Text(
                    text = "UTF-8",
                    color = colors.statusBarText,
                    fontSize = 10.sp
                )

                Text(
                    text = activeTab?.language?.uppercase() ?: "PLAIN TEXT",
                    color = colors.statusBarText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.statusBarText,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Gemini",
                        color = colors.statusBarText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
