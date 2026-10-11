package com.example.ui.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NoteEntity
import com.example.data.model.PocketType
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.BalanceCard
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.Formatters
import com.example.ui.components.TutorialDialog
import com.example.ui.components.openWhatsAppChannel
import com.example.ui.finance.AddEditTransactionDialog
import com.example.ui.finance.TransactionItemCard
import com.example.ui.navigation.AppScreen
import com.example.ui.notes.AddEditNoteDialog
import com.example.ui.notes.NoteCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.viewmodel.AppViewModel

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToTab: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.financeSummary.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val pockets by viewModel.pockets.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }

    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    val recentTransactions = remember(transactions) { transactions.take(3) }
    val recentNotes = remember(notes) { notes.take(2) }

    val todayFormatted = remember { Formatters.formatDate(System.currentTimeMillis()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Sleek Minimalist Header with Tutorial quick chip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayFormatted.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ringkasan Harian",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showTutorialDialog = true }
                        .testTag("dashboard_tutorial_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Panduan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Clean Balance Card with Today & Monthly Breakdown
        item {
            BalanceCard(
                balance = summary.balance,
                totalIncome = summary.totalIncome,
                totalExpense = summary.totalExpense,
                todayExpense = summary.todayExpense,
                monthlyExpense = summary.monthlyExpense,
                currency = currency
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sub-Alokasi Dana Quick Summary Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(AppScreen.POCKETS) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sub-Alokasi & Kantong Dana",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (pockets.isEmpty()) "Pisahkan uang belanja & tabungan" else "${pockets.size} kantong aktif",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Buka Kantong",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Modern Minimalist Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Catat Pengeluaran
                FilledTonalButton(
                    onClick = { showAddExpenseDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_expense_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ExpenseRedContainer.copy(alpha = 0.5f),
                        contentColor = ExpenseRed
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Keluar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Catat Pemasukan
                FilledTonalButton(
                    onClick = { showAddIncomeDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_income_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = IncomeGreenContainer.copy(alpha = 0.5f),
                        contentColor = IncomeGreen
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Masuk",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tulis Catatan
                FilledTonalButton(
                    onClick = { showAddNoteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_note_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Catat",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terbaru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(
                    onClick = { onNavigateToTab(AppScreen.FINANCE) },
                    modifier = Modifier.testTag("view_all_transactions_button")
                ) {
                    Text("Semua", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (recentTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Belum ada transaksi tercatat. Tekan 'Keluar' atau 'Masuk' untuk mulai mencatat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentTransactions, key = { "${it.id}_${it.dateMillis}" }) { tx ->
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

        item { Spacer(modifier = Modifier.height(18.dp)) }

        // Recent Notes Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catatan Harian Terbaru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(
                    onClick = { onNavigateToTab(AppScreen.NOTES) },
                    modifier = Modifier.testTag("view_all_notes_button")
                ) {
                    Text("Semua", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (recentNotes.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Belum ada catatan dibuat. Tekan 'Catat' untuk menulis ide atau diary harian.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentNotes, key = { "${it.id}_${it.updatedAtMillis}" }) { note ->
                NoteCard(
                    note = note,
                    onCardClick = { noteToEdit = note },
                    onTogglePin = { viewModel.togglePinNote(note) },
                    onToggleFavorite = { viewModel.toggleFavoriteNote(note) },
                    onEdit = { noteToEdit = note },
                    onDelete = { noteToDelete = note }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Creator & WhatsApp Community Banner at Bottom
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "Creator: zals",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = { showTutorialDialog = true }
                        ) {
                            Text("Lihat Tutorial 📖", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Gabung Saluran WhatsApp zals untuk update terbaru & tips penggunaan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { openWhatsAppChannel(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366) // WhatsApp Green
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_whatsapp_button")
                    ) {
                        Text(text = "💬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Saluran WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showTutorialDialog) {
        TutorialDialog(onDismiss = { showTutorialDialog = false })
    }

    if (showAddExpenseDialog) {
        AddEditTransactionDialog(
            defaultType = TransactionType.EXPENSE,
            pockets = pockets,
            currency = currency,
            onDismiss = { showAddExpenseDialog = false },
            onSave = {
                viewModel.addTransaction(it)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddIncomeDialog) {
        AddEditTransactionDialog(
            defaultType = TransactionType.INCOME,
            pockets = pockets,
            currency = currency,
            onDismiss = { showAddIncomeDialog = false },
            onSave = {
                viewModel.addTransaction(it)
                showAddIncomeDialog = false
            }
        )
    }

    if (showAddNoteDialog) {
        AddEditNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onSave = {
                viewModel.addNote(it)
                showAddNoteDialog = false
            }
        )
    }

    if (transactionToEdit != null) {
        AddEditTransactionDialog(
            initialTransaction = transactionToEdit,
            pockets = pockets,
            currency = currency,
            onDismiss = { transactionToEdit = null },
            onSave = {
                viewModel.updateTransaction(it)
                transactionToEdit = null
            }
        )
    }

    if (noteToEdit != null) {
        AddEditNoteDialog(
            initialNote = noteToEdit,
            onDismiss = { noteToEdit = null },
            onSave = {
                viewModel.updateNote(it)
                noteToEdit = null
            }
        )
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        ConfirmDeleteDialog(
            title = "Hapus Transaksi?",
            message = "Hapus catatan \"${tx.title}\"?",
            onConfirm = {
                viewModel.deleteTransaction(tx)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }

    if (noteToDelete != null) {
        val note = noteToDelete!!
        ConfirmDeleteDialog(
            title = "Hapus Catatan?",
            message = "Hapus catatan \"${note.title}\"?",
            onConfirm = {
                viewModel.deleteNote(note)
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }
}
