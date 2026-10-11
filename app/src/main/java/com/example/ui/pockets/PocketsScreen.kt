package com.example.ui.pockets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PocketEntity
import com.example.data.model.PocketType
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.Formatters
import com.example.ui.viewmodel.AppViewModel

@Composable
fun PocketsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val pockets by viewModel.pockets.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var pocketToEdit by remember { mutableStateOf<PocketEntity?>(null) }
    var pocketToDelete by remember { mutableStateOf<PocketEntity?>(null) }
    var pocketToAdjustFunds by remember { mutableStateOf<PocketEntity?>(null) }

    var selectedTabType by remember { mutableStateOf<PocketType?>(null) } // null = all

    val filteredPockets = remember(pockets, selectedTabType) {
        if (selectedTabType == null) pockets else pockets.filter { it.type == selectedTabType }
    }

    val totalSpendingAllocated = remember(pockets) {
        pockets.filter { it.type == PocketType.SPENDING }.sumOf { it.allocatedBalance }
    }
    val totalSavingsAccumulated = remember(pockets) {
        pockets.filter { it.type == PocketType.SAVINGS }.sumOf { it.allocatedBalance }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sub-Alokasi & Kantong Dana",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Pisahkan uang belanja dan tabungan impian dari dompet utama.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Summary Card (Spending vs Savings)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Belanja Terpisah
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🛒", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Uang Belanja",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatCurrency(totalSpendingAllocated, currency),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        )

                        // Tabungan Impian
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎯", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Total Tabungan",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatCurrency(totalSavingsAccumulated, currency),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF10B981),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedTabType == null,
                            onClick = { selectedTabType = null },
                            label = { Text("Semua (${pockets.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTabType == PocketType.SPENDING,
                            onClick = { selectedTabType = PocketType.SPENDING },
                            label = { Text("🛒 Belanja & Keluar", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTabType == PocketType.SAVINGS,
                            onClick = { selectedTabType = PocketType.SAVINGS },
                            label = { Text("🎯 Tabungan Impian", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Pockets List
            if (filteredPockets.isEmpty()) {
                item {
                    EmptyStateCard(
                        emoji = "👛",
                        title = "Belum Ada Sub-Alokasi Dana",
                        message = "Buat kantong terpisah untuk uang belanja bulanan (misal 200rb) atau alokasi tabungan masa depan."
                    )
                }
            } else {
                items(filteredPockets, key = { "${it.id}_${it.createdAtMillis}" }) { pocket ->
                    PocketItemCard(
                        pocket = pocket,
                        currency = currency,
                        onAddFunds = { pocketToAdjustFunds = pocket },
                        onEdit = { pocketToEdit = pocket },
                        onDelete = { pocketToDelete = pocket }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_pocket_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah") },
            text = { Text("Buat Kantong", fontWeight = FontWeight.Bold) }
        )
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditPocketDialog(
            currency = currency,
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.addPocket(it)
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    if (pocketToEdit != null) {
        AddEditPocketDialog(
            initialPocket = pocketToEdit,
            currency = currency,
            onDismiss = { pocketToEdit = null },
            onSave = {
                viewModel.updatePocket(it)
                pocketToEdit = null
            }
        )
    }

    // Adjust Funds Dialog
    if (pocketToAdjustFunds != null) {
        val pocket = pocketToAdjustFunds!!
        AdjustFundsDialog(
            pocket = pocket,
            currency = currency,
            onDismiss = { pocketToAdjustFunds = null },
            onConfirm = { amount ->
                viewModel.allocateFundsToPocket(pocket.id, amount)
                pocketToAdjustFunds = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (pocketToDelete != null) {
        val pocket = pocketToDelete!!
        ConfirmDeleteDialog(
            title = "Hapus Kantong?",
            message = "Apakah Anda yakin ingin menghapus alokasi \"${pocket.name}\"?",
            onConfirm = {
                viewModel.deletePocket(pocket)
                pocketToDelete = null
            },
            onDismiss = { pocketToDelete = null }
        )
    }
}

@Composable
fun PocketItemCard(
    pocket: PocketEntity,
    currency: com.example.data.model.CurrencyType,
    onAddFunds: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isSavings = pocket.type == PocketType.SAVINGS

    val progress = remember(pocket) {
        if (isSavings && pocket.targetAmount > 0) {
            (pocket.allocatedBalance.toFloat() / pocket.targetAmount.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAddFunds() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = pocket.emoji, fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pocket.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSavings) "🎯 Tabungan Impian" else "🛒 Alokasi Belanja",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatCurrency(pocket.allocatedBalance, currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSavings) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    )
                    if (isSavings && pocket.targetAmount > 0) {
                        Text(
                            text = "Target: ${Formatters.formatCurrency(pocket.targetAmount, currency)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Options Menu
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opsi", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Isi / Sesuaikan Saldo") },
                            onClick = {
                                showMenu = false
                                onAddFunds()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Kantong") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Savings Progress Bar
            if (isSavings && pocket.targetAmount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF10B981),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            // Quick Top Up / Adjust Button
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAddFunds() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSavings) "Tambah Tabungan" else "Isi Saldo Kantong",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AdjustFundsDialog(
    pocket: PocketEntity,
    currency: com.example.data.model.CurrencyType,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${pocket.emoji} ${pocket.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Saldo saat ini: ${Formatters.formatCurrency(pocket.allocatedBalance, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Tambah vs Kurang
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAdding) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isAdding = true }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Tambah Dana",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isAdding) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isAdding) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isAdding = false }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "- Tarik Dana",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (!isAdding) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() }.take(12) },
                    label = { Text("Nominal") },
                    prefix = { Text("${currency.symbol} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount > 0) {
                        onConfirm(if (isAdding) amount else -amount)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
