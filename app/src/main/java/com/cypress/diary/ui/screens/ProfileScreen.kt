package com.cypress.diary.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cypress.diary.github.GitHubConfig
import com.cypress.diary.ui.components.RefreshableScreen
import com.cypress.diary.ui.theme.ThemePalette

@Composable
fun ProfileScreen(
    selectedPalette: ThemePalette,
    onPaletteSelected: (ThemePalette) -> Unit,
    githubConfig: GitHubConfig?,
    connectionStatus: String,
    backgroundUri: String?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onGitHubConnect: (GitHubConfig) -> Unit,
    onBackgroundSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val initialConfig = githubConfig ?: GitHubConfig(
        owner = "cyprer",
        repo = "astroblog",
        branch = "main",
        token = "",
    )

    var owner by rememberSaveable { mutableStateOf(initialConfig.owner) }
    var repo by rememberSaveable { mutableStateOf(initialConfig.repo) }
    var branch by rememberSaveable { mutableStateOf(initialConfig.branch) }
    var token by rememberSaveable { mutableStateOf(initialConfig.token) }

    LaunchedEffect(initialConfig) {
        owner = initialConfig.owner
        repo = initialConfig.repo
        branch = initialConfig.branch
        token = initialConfig.token
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onBackgroundSelected(uri.toString())
        }
    }

    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "我的",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        AccountCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text("GitHub", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = connectionStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("仓库拥有者") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = repo,
                    onValueChange = { repo = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("仓库名称") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("分支") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Token") },
                    singleLine = true,
                    visualTransformation = if (token.isBlank()) VisualTransformation.None else PasswordVisualTransformation(),
                )
                Text(
                    text = "公开仓库可直接读取，推送仍需要 Token。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                )
                Button(
                    onClick = {
                        onGitHubConnect(
                            GitHubConfig(
                                owner = owner,
                                repo = repo,
                                branch = branch,
                                token = token,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("保存连接")
                }
            }
        }

        AccountCard {
            Text("主题色", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ThemePalette.values().forEach { palette ->
                    FilterChip(
                        selected = selectedPalette == palette,
                        onClick = { onPaletteSelected(palette) },
                        label = { Text(palette.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = palette.primary,
                            )
                        },
                    )
                }
            }
        }

        AccountCard {
            Text("背景图", fontWeight = FontWeight.SemiBold)
            Text(
                text = backgroundUri ?: "尚未选择背景图",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { picker.launch(arrayOf("image/*")) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                    Text("选择背景")
                }
                Button(
                    onClick = { onBackgroundSelected(null) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = null)
                    Text("清除")
                }
            }
            Text(
                text = "选择一张图片作为整个应用的背景。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }

        AccountCard {
            InfoLine(
                title = "仓库",
                value = githubConfig?.let { "${it.owner}/${it.repo}" } ?: "未连接",
            )
            InfoLine(title = "分支", value = githubConfig?.branch ?: "main")
            InfoLine(title = "草稿", value = "已开启", icon = Icons.Filled.Check)
            InfoLine(title = "同步", value = connectionStatus, icon = Icons.Filled.Info)
        }
    }
}

@Composable
private fun AccountCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun InfoLine(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(title, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}
