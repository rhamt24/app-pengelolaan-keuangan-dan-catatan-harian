package com.example.ui.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CurrencyType
import com.example.data.model.ExpenseCategories
import com.example.data.model.IncomeCategories
import com.example.data.model.PocketEntity
import com.example.data.model.SubItemParser
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.AppSearchBar
import com.example.ui.components.BalanceCard
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.Formatters
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.viewmodel.AppViewModel

@Composable
fun FinanceScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val summary by viewModel.financeSummary.collectAsStateWithLifecycle()
    val searchQuery by viewModel.financeSearchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val pockets by viewModel.pockets.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Balance Card with Today & Monthly Breakdown
            item {
                BalanceCard(
                    balance = summary.balance,
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    todayExpense = summary.todayExpense,
                    monthlyExpense = summary.monthlyExpense,
                    currency = currency
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Search Bar
            item {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setFinanceSearchQuery(it) },
                    placeholder = "Cari pengeluaran atau pemasukan..."
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Minimalist Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { viewModel.setTypeFilter(null) },
                            label = { Text("Semua", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedType == null,
                                borderColor = if (selectedType == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("filter_all_transactions")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedType == TransactionType.EXPENSE,
                            onClick = { viewModel.setTypeFilter(TransactionType.EXPENSE) },
                            label = { Text("Pengeluaran", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExpenseRedContainer,
                                selectedLabelColor = ExpenseRed
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedType == TransactionType.EXPENSE,
                                borderColor = if (selectedType == TransactionType.EXPENSE) ExpenseRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("filter_expense_transactions")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedType == TransactionType.INCOME,
                            onClick = { viewModel.setTypeFilter(TransactionType.INCOME) },
                            label = { Text("Pemasukan", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IncomeGreenContainer,
                                selectedLabelColor = IncomeGreen
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedType == TransactionType.INCOME,
                                borderColor = if (selectedType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("filter_income_transactions")
                        )
                    }

                    // Category filters
                    val categoriesToDisplay = if (selectedType == TransactionType.INCOME) IncomeCategories else ExpenseCategories
                    items(categoriesToDisplay) { (cat, emoji) ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setCategoryFilter(if (isSelected) null else cat)
                            },
                            label = { Text("$emoji $cat", fontSize = 12.sp) },
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
                            modifier = Modifier.testTag("filter_cat_$cat")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Transactions Header Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${transactions.size} transaksi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Transaction Cards List
            if (transactions.isEmpty()) {
                item {
                    EmptyStateCard(
                        emoji = if (searchQuery.isNotEmpty()) "🔍" else "💸",
                        title = if (searchQuery.isNotEmpty()) "Tidak ada hasil" else "Belum ada transaksi",
                        message = if (searchQuery.isNotEmpty()) {
                            "Tidak ditemukan transaksi dengan kata kunci \"$searchQuery\"."
                        } else {
                            "Tekan tombol (+) untuk mencatat pengeluaran atau pemasukan."
                        }
                    )
                }
            } else {
                items(transactions, key = { "${it.id}_${it.dateMillis}" }) { tx ->
                    val pocket = pockets.find { it.id == tx.pocketId }
                    TransactionItemCard(
                        transaction = tx,
                        currency = currency,
                        pocketName = pocket?.name,
                        pocketEmoji = pocket?.emoji ?: "👛",
                        onEdit = { transactionToEdit = tx },
                        onDelete = { transactionToDelete = tx }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Minimalist FAB
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_transaction_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah") },
            text = { Text("Catat Uang", fontWeight = FontWeight.Bold) }
        )
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditTransactionDialog(
            defaultType = TransactionType.EXPENSE,
            pockets = pockets,
            currency = currency,
            onDismiss = { showAddDialog = false },
            onSave = { newTx ->
                viewModel.addTransaction(newTx)
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    if (transactionToEdit != null) {
        AddEditTransactionDialog(
            initialTransaction = transactionToEdit,
            pockets = pockets,
            currency = currency,
            onDismiss = { transactionToEdit = null },
            onSave = { updatedTx ->
                viewModel.updateTransaction(updatedTx)
                transactionToEdit = null
            }
        )
    }

    // Delete Confirmation
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        ConfirmDeleteDialog(
            title = "Hapus Transaksi?",
            message = "Apakah Anda yakin ingin menghapus \"${tx.title}\" sebesar ${Formatters.formatCurrency(tx.amount, currency)}?",
            onConfirm = {
                viewModel.deleteTransaction(tx)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    currency: CurrencyType = CurrencyType.IDR,
    pocketName: String? = null,
    pocketEmoji: String = "👛",
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    var showMenu by remember { mutableStateOf(false) }
    var expandedSubItems by remember { mutableStateOf(false) }

    val emoji = remember(transaction.category, transaction.type) {
        val list = if (isExpense) ExpenseCategories else IncomeCategories
        list.find { it.first == transaction.category }?.second ?: if (isExpense) "💸" else "💰"
    }

    val subItems = remember(transaction.subItemsJson) {
        SubItemParser.parse(transaction.subItemsJson)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main Row: Icon + Title/Category + Amount + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon Badge Minimalist
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Category + Payment Method
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.category} • ${transaction.paymentMethod.split(" ").first()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Amount
                Text(
                    text = Formatters.formatSignedCurrency(transaction.amount, isExpense, currency),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = if (isExpense) ExpenseRed else IncomeGreen,
                    maxLines = 1
                )

                // Menu button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("transaction_menu_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Sub-Alokasi Badge (if assigned to a pocket)
            if (!pocketName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "$pocketEmoji $pocketName",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Multi-Item Grouped Expense Breakdown Preview
            if (subItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedSubItems = !expandedSubItems }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📦 ${subItems.size} Rincian Item Pengeluaran",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (expandedSubItems) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = expandedSubItems) {
                            Column(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                subItems.forEachIndexed { i, sub ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• ${sub.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = Formatters.formatCurrency(sub.amount, currency),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Row: Detail Tanggal, Hari Kejadian, & Jam Lengkap
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Formatters.formatDetailedDateTime(transaction.dateMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
