package com.example.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExpenseCategories
import com.example.data.model.IncomeCategories
import com.example.data.model.PaymentMethods
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.Formatters
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    defaultType: TransactionType = TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: defaultType) }
    var amountText by remember {
        mutableStateOf(if (initialTransaction != null) initialTransaction.amount.toString() else "")
    }
    var title by remember { mutableStateOf(initialTransaction?.title ?: "") }
    var category by remember {
        mutableStateOf(
            initialTransaction?.category ?: if (type == TransactionType.EXPENSE) {
                ExpenseCategories.first().first
            } else {
                IncomeCategories.first().first
            }
        )
    }
    var paymentMethod by remember {
        mutableStateOf(initialTransaction?.paymentMethod ?: PaymentMethods.first().first)
    }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentCategories = if (type == TransactionType.EXPENSE) ExpenseCategories else IncomeCategories

    val numericAmount = remember(amountText) {
        try {
            amountText.take(12).toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_transaction_dialog"),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTransaction == null) "Tambah Transaksi" else "Edit Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_transaction_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp)
                ) {
                    val isExp = type == TransactionType.EXPENSE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExp) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable {
                                type = TransactionType.EXPENSE
                                if (currentCategories.none { it.first == category }) {
                                    category = ExpenseCategories.first().first
                                }
                            }
                            .padding(vertical = 8.dp)
                            .testTag("type_expense_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pengeluaran",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isExp) FontWeight.Bold else FontWeight.Normal,
                            color = if (isExp) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val isInc = type == TransactionType.INCOME
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isInc) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable {
                                type = TransactionType.INCOME
                                if (currentCategories.none { it.first == category }) {
                                    category = IncomeCategories.first().first
                                }
                            }
                            .padding(vertical = 8.dp)
                            .testTag("type_income_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pemasukan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isInc) FontWeight.Bold else FontWeight.Normal,
                            color = if (isInc) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nominal Input
                Text(
                    text = "Nominal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(12)
                        amountText = digits
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input_field"),
                    placeholder = { Text("0") },
                    prefix = {
                        Text(
                            "Rp ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    supportingText = {
                        if (numericAmount > 0) {
                            Text(
                                text = Formatters.formatRupiah(numericAmount),
                                color = if (type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    isError = errorMessage != null && amountText.isBlank(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                // Quick Amount Chips
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickAddList = listOf(10000L, 20000L, 50000L, 100000L, 200000L, 500000L)
                    quickAddList.forEach { valChip ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val current = amountText.toLongOrNull() ?: 0L
                                    amountText = (current + valChip).coerceAtMost(999999999999L).toString()
                                }
                        ) {
                            Text(
                                text = "+${valChip / 1000}rb",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Judul
                Text(
                    text = "Keterangan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("title_input_field"),
                    placeholder = {
                        Text(if (type == TransactionType.EXPENSE) "Contoh: Makan siang" else "Contoh: Gaji bulanan")
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Kategori Selector
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentCategories.forEach { (catName, emoji) ->
                        val isSelected = category == catName
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = catName },
                            label = { Text("$emoji $catName", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("category_chip_$catName")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Metode Pembayaran
                Text(
                    text = "Metode Pembayaran",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethods.forEach { (payName, emoji) ->
                        val isSelected = paymentMethod == payName
                        FilterChip(
                            selected = isSelected,
                            onClick = { paymentMethod = payName },
                            label = { Text("$emoji $payName", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("payment_chip_$payName")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Catatan Tambahan (Opsional)
                Text(
                    text = "Catatan Tambahan (Opsional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_input_field"),
                    placeholder = { Text("Detail catatan...") },
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_transaction_button")
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = try {
                                amountText.take(12).toLongOrNull()
                            } catch (e: Exception) {
                                null
                            }
                            if (parsedAmount == null || parsedAmount <= 0) {
                                errorMessage = "Harap masukkan nominal yang valid (> 0)"
                                return@Button
                            }
                            val finalTitle = title.trim().ifEmpty { category }
                            val newTx = TransactionEntity(
                                id = initialTransaction?.id ?: 0L,
                                title = finalTitle,
                                amount = parsedAmount,
                                type = type,
                                category = category,
                                paymentMethod = paymentMethod,
                                note = note.trim(),
                                dateMillis = initialTransaction?.dateMillis ?: System.currentTimeMillis()
                            )
                            onSave(newTx)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_transaction_button")
                    ) {
                        Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
