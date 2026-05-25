package com.cypress.diary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cypress.diary.accounting.formatAmountCents
import com.cypress.diary.accounting.parseAmountCents
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.model.accounting.AccountingRecordType
import com.cypress.diary.model.accounting.defaultAccountingCategories
import com.cypress.diary.ui.components.RefreshableScreen
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountingEditorScreen(
    record: AccountingRecord?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onSave: (AccountingRecord) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var typeName by rememberSaveable(record?.id) {
        mutableStateOf(record?.type?.name ?: AccountingRecordType.Expense.name)
    }
    val type = AccountingRecordType.valueOf(typeName)
    var amountText by rememberSaveable(record?.id) {
        mutableStateOf(record?.amountCents?.let(::formatAmountCents).orEmpty())
    }
    var category by rememberSaveable(record?.id) {
        mutableStateOf(record?.category ?: defaultAccountingCategories.first { it.type == type }.label)
    }
    var dateText by rememberSaveable(record?.id) {
        mutableStateOf(record?.date?.toString() ?: LocalDate.now().toString())
    }
    var note by rememberSaveable(record?.id) { mutableStateOf(record?.note.orEmpty()) }
    var showDeleteConfirm by rememberSaveable(record?.id) { mutableStateOf(false) }
    val amountCents = parseAmountCents(amountText)
    val parsedDate = runCatching { LocalDate.parse(dateText) }.getOrNull()
    val canSave = amountCents != null && parsedDate != null && category.isNotBlank()

    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = if (record == null) "记一笔" else "编辑账目",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AccountingRecordType.values().forEach { option ->
                FilterChip(
                    selected = type == option,
                    onClick = {
                        typeName = option.name
                        category = defaultAccountingCategories.first { it.type == option }.label
                    },
                    label = { Text(option.label) },
                )
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("金额") },
            singleLine = true,
            isError = amountText.isNotBlank() && amountCents == null,
            supportingText = {
                if (amountText.isNotBlank() && amountCents == null) {
                    Text("请输入大于 0 的金额，最多两位小数")
                }
            },
        )

        OutlinedTextField(
            value = dateText,
            onValueChange = { dateText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("日期 YYYY-MM-DD") },
            singleLine = true,
            isError = dateText.isNotBlank() && parsedDate == null,
            supportingText = {
                if (dateText.isNotBlank() && parsedDate == null) {
                    Text("日期格式示例：2026-05-25")
                }
            },
        )

        Text("分类", fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            defaultAccountingCategories.filter { it.type == type }.forEach { option ->
                FilterChip(
                    selected = category == option.label,
                    onClick = { category = option.label },
                    label = { Text(option.label) },
                )
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("备注") },
        )

        Button(
            enabled = canSave,
            onClick = {
                val now = System.currentTimeMillis()
                onSave(
                    AccountingRecord(
                        id = record?.id ?: UUID.randomUUID().toString(),
                        type = type,
                        amountCents = requireNotNull(amountCents),
                        category = category,
                        date = requireNotNull(parsedDate),
                        note = note.trim(),
                        createdAt = record?.createdAt ?: now,
                        updatedAt = now,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("保存")
        }

        if (record != null) {
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("删除这笔账目")
            }
        }
    }

    if (showDeleteConfirm && record != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除账目") },
            text = { Text("删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = { onDelete(record.id) }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }
}
