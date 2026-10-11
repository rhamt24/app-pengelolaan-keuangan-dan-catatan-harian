package com.example.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CurrencyType
import com.example.data.model.ExpenseCategories
import com.example.data.model.IncomeCategories
import com.example.data.model.PaymentMethods
import com.example.data.model.PocketEntity
import com.example.data.model.SubItemParser
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionSubItem
import com.example.data.model.TransactionType
import com.example.ui.components.Formatters
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    defaultType: TransactionType = TransactionType.EXPENSE,
    pockets: List<PocketEntity> = emptyList(),
    currency: CurrencyType = CurrencyType.IDR,
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
    var selectedPocketId by remember {
        mutableStateOf(initialTransaction?.pocketId ?: 0L)
    }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Multi-Item / Grouped Expense Breakdown
    var subItems by remember {
        mutableStateOf(SubItemParser.parse(initialTransaction?.subItemsJson ?: ""))
    }
    var showSubItemSection by remember {
        mutableStateOf(subItems.isNotEmpty())
    }
    var newItemName by remember { mutableStateOf("") }
    var newItemAmount by remember { mutableStateOf("") }

    val currentCategories = if (type == TransactionType.EXPENSE) ExpenseCategories else IncomeCategories

    val numericAmount = remember(amountText) {
        try {
            amountText.take(12).toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    val subItemsTotal = remember(subItems) {
        subItems.sumOf { it.amount }
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
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("close_transaction_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Type Selector (Pengeluaran vs Pemasukan)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Expense Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable {
                                    type = TransactionType.EXPENSE
                                    if (IncomeCategories.any { it.first == category }) {
                                        category = ExpenseCategories.first().first
                                    }
                                }
                                .testTag("type_expense_tab"),
                            shape = RoundedCornerShape(9.dp),
                            color = if (type == TransactionType.EXPENSE) ExpenseRed else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pengeluaran",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Income Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable {
                                    type = TransactionType.INCOME
                                    if (ExpenseCategories.any { it.first == category }) {
                                        category = IncomeCategories.first().first
                                    }
                                }
                                .testTag("type_income_tab"),
                            shape = RoundedCornerShape(9.dp),
                            color = if (type == TransactionType.INCOME) IncomeGreen else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pemasukan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sub-Alokasi / Kantong Dana Selector
                Text(
                    text = "Alokasi Dompet / Kantong",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Dompet Utama
                    val isMainWallet = selectedPocketId == 0L
                    FilterChip(
                        selected = isMainWallet,
                        onClick = { selectedPocketId = 0L },
                        label = { Text("👛 Dompet Utama", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isMainWallet,
                            borderColor = if (isMainWallet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("pocket_chip_main")
                    )

                    // Pockets list
                    pockets.forEach { pocket ->
                        val isSelected = selectedPocketId == pocket.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPocketId = pocket.id },
                            label = {
                                Text(
                                    "${pocket.emoji} ${pocket.name}",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("pocket_chip_${pocket.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Nominal Utama
                Text(
                    text = "Nominal Total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }.take(12)
                        amountText = filtered
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input_field"),
                    placeholder = { Text("0", fontSize = 20.sp) },
                    leadingIcon = {
                        Text(
                            text = "${currency.symbol} ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                if (numericAmount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatCurrency(numericAmount, currency),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick Increment Chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val increments = listOf(10_000L, 20_000L, 50_000L, 100_000L)
                    increments.forEach { valChip ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val current = amountText.toLongOrNull() ?: 0L
                                    amountText = (current + valChip).coerceAtMost(999999999999L).toString()
                                }
                        ) {
                            Text(
                                text = "+${valChip / 1000}k",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Judul / Keterangan
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
                        Text(if (type == TransactionType.EXPENSE) "Contoh: Belanja Bulanan" else "Contoh: Gaji bulanan")
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Multi-Item / Grouped Expense Section (Rincian Item Belanja/Pengeluaran)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatListBulleted,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Kelompokkan Item Pengeluaran",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            TextButton(
                                onClick = { showSubItemSection = !showSubItemSection },
                                modifier = Modifier.testTag("toggle_sub_items_section")
                            ) {
                                Text(if (showSubItemSection) "Sembunyikan" else "Buka Rincian (${subItems.size})")
                            }
                        }

                        if (showSubItemSection) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tambahkan rincian barang dalam pengeluaran ini (misal: Telur 20rb, Beras 50rb).",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Current sub-items list
                            if (subItems.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    subItems.forEachIndexed { index, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${index + 1}. ${item.name}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = Formatters.formatCurrency(item.amount, currency),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    color = ExpenseRed
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = {
                                                        subItems = subItems.filterIndexed { i, _ -> i != index }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Hapus item",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Total Rincian & Tombol Gunakan Total
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total Rincian: ${Formatters.formatCurrency(subItemsTotal, currency)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        TextButton(
                                            onClick = {
                                                amountText = subItemsTotal.toString()
                                            }
                                        ) {
                                            Text("Gunakan Sebagai Nominal", fontSize = 11.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Input form for new sub-item
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newItemName,
                                    onValueChange = { newItemName = it },
                                    modifier = Modifier.weight(1.2f),
                                    placeholder = { Text("Nama item...", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = newItemAmount,
                                    onValueChange = { input ->
                                        newItemAmount = input.filter { it.isDigit() }.take(10)
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Harga...", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                IconButton(
                                    onClick = {
                                        val amt = newItemAmount.toLongOrNull() ?: 0L
                                        val nm = newItemName.trim()
                                        if (nm.isNotEmpty() && amt > 0) {
                                            subItems = subItems + TransactionSubItem(nm, amt)
                                            newItemName = ""
                                            newItemAmount = ""
                                            // Optionally auto-update total if amountText is 0
                                            if (numericAmount == 0L) {
                                                amountText = (subItemsTotal + amt).toString()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah item",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

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
                            val subJson = SubItemParser.toJson(subItems)
                            val newTx = TransactionEntity(
                                id = initialTransaction?.id ?: 0L,
                                title = finalTitle,
                                amount = parsedAmount,
                                type = type,
                                category = category,
                                paymentMethod = paymentMethod,
                                note = note.trim(),
                                pocketId = selectedPocketId,
                                subItemsJson = subJson,
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
