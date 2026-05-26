package com.cypress.diary.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.cypress.diary.github.GitHubConfig
import com.cypress.diary.ui.components.RefreshableScreen
import com.cypress.diary.ui.navigation.AppModule
import com.cypress.diary.ui.theme.ThemePalette
import java.time.LocalDate

private const val GitHubUnlockTapCount = 7
private const val GitHubUnlockTapWindowMillis = 1_000L

@Composable
fun ProfileScreen(
    currentModule: AppModule,
    onModuleSelected: (AppModule) -> Unit,
    selectedPalette: ThemePalette,
    onPaletteSelected: (ThemePalette) -> Unit,
    githubConfig: GitHubConfig?,
    connectionStatus: String,
    githubSettingsRevealSignal: Int,
    backgroundUri: String?,
    layoutOpacity: Float,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onGitHubConnect: (GitHubConfig) -> Unit,
    onGitHubDisconnect: () -> Unit,
    onExportDiary: (Uri) -> Unit,
    onImportDiary: (Uri) -> Unit,
    onExportAccounting: (Uri) -> Unit,
    onImportAccounting: (Uri) -> Unit,
    onBackgroundSelected: (String?) -> Unit,
    onLayoutOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val initialConfig = githubConfig ?: GitHubConfig(
        owner = "",
        repo = "",
        branch = "main",
        token = "",
    )

    var owner by rememberSaveable { mutableStateOf(initialConfig.owner) }
    var repo by rememberSaveable { mutableStateOf(initialConfig.repo) }
    var branch by rememberSaveable { mutableStateOf(initialConfig.branch) }
    var token by rememberSaveable { mutableStateOf(initialConfig.token) }
    var hiddenTapCount by rememberSaveable { mutableStateOf(0) }
    var lastHiddenTapAt by rememberSaveable { mutableStateOf(0L) }
    var showGitHubDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialConfig) {
        owner = initialConfig.owner
        repo = initialConfig.repo
        branch = initialConfig.branch
        token = initialConfig.token
    }
    LaunchedEffect(githubSettingsRevealSignal) {
        if (githubSettingsRevealSignal > 0) {
            showGitHubDialog = true
        }
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
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri != null) {
            onExportDiary(uri)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            onImportDiary(uri)
        }
    }
    val accountingExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri != null) {
            onExportAccounting(uri)
        }
    }
    val accountingImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            onImportAccounting(uri)
        }
    }

    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (githubConfig != null) {
                IconButton(
                    onClick = onGitHubDisconnect,
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "退出 GitHub 连接",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = "我的",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        AccountCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val now = System.currentTimeMillis()
                        hiddenTapCount = if (now - lastHiddenTapAt <= GitHubUnlockTapWindowMillis) {
                            hiddenTapCount + 1
                        } else {
                            1
                        }
                        lastHiddenTapAt = now
                        if (hiddenTapCount >= GitHubUnlockTapCount) {
                            hiddenTapCount = 0
                            showGitHubDialog = true
                        }
                    },
                )
                Column {
                    Text("私人日记", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "本地使用",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                }
            }
        }

        AccountCard {
            Text("模块", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppModule.values().forEach { module ->
                    FilterChip(
                        selected = currentModule == module,
                        onClick = { onModuleSelected(module) },
                        label = { Text(module.label) },
                    )
                }
            }
            Text(
                text = if (currentModule == AppModule.Diary) {
                    "当前使用日记模块"
                } else {
                    "当前使用记账模块"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
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
            Text("数据", fontWeight = FontWeight.SemiBold)
            Button(
                onClick = { exportLauncher.launch("diary-export-${LocalDate.now()}.diary") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Upload, contentDescription = null)
                Text("导出日记数据")
            }
            Button(
                onClick = {
                    importLauncher.launch(
                        arrayOf(
                            "application/octet-stream",
                            "application/zip",
                            "application/x-zip-compressed",
                            "*/*",
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Text("导入日记数据")
            }
            Text(
                text = "导出会生成 .diary 文件；导入会用文件内容替换本地缓存。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
            Button(
                onClick = { accountingExportLauncher.launch("accounting-export-${LocalDate.now()}.accounting") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Upload, contentDescription = null)
                Text("导出账单数据")
            }
            Button(
                onClick = {
                    accountingImportLauncher.launch(
                        arrayOf(
                            "application/octet-stream",
                            "application/zip",
                            "application/x-zip-compressed",
                            "*/*",
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Text("导入账单数据")
            }
            Text(
                text = "导出会生成 .accounting 文件；导入时可选择替换或合并本地账单。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
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
            Text(
                text = "布局透明度 ${(layoutOpacity * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = layoutOpacity,
                onValueChange = onLayoutOpacityChange,
                valueRange = 0.35f..1f,
                steps = 12,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showGitHubDialog) {
        AlertDialog(
            onDismissRequest = { showGitHubDialog = false },
            title = { Text("GitHub 同步") },
            text = {
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
                        visualTransformation = if (token.isBlank()) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    )
                    Text(
                        text = "公开仓库可直接读取；推送或私有仓库需要 Token。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    )
                    Text(
                        text = connectionStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = owner.isNotBlank() && repo.isNotBlank(),
                    onClick = {
                        onGitHubConnect(
                            GitHubConfig(
                                owner = owner,
                                repo = repo,
                                branch = branch,
                                token = token,
                            ),
                        )
                        showGitHubDialog = false
                    },
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        enabled = githubConfig != null,
                        onClick = {
                            onGitHubDisconnect()
                            showGitHubDialog = false
                        },
                    ) {
                        Text("退出连接")
                    }
                    TextButton(onClick = { showGitHubDialog = false }) {
                        Text("取消")
                    }
                }
            },
        )
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
