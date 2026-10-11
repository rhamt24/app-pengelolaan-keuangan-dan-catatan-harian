package com.example.ui.pockets

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
import com.example.data.model.CurrencyType
import com.example.data.model.PocketEntity
import com.example.data.model.PocketType
import com.example.ui.components.Formatters

val PocketPresetEmojis = listOf("👛", "🛒", "🍔", "☕", "🚗", "🏠", "🎁", "🏖️", "🏍️", "📱", "🛡️", "💰", "🎓")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditPocketDialog(
    initialPocket: PocketEntity? = null,
    currency: CurrencyType = CurrencyType.IDR,
    onDismiss: () -> Unit,
    onSave: (PocketEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialPocket?.name ?: "") }
    var type by remember { mutableStateOf(initialPocket?.type ?: PocketType.SPENDING) }
    var balanceText by remember { mutableStateOf(if (initialPocket != null) initialPocket.allocatedBalance.toString() else "") }
    var targetText by remember { mutableStateOf(if (initialPocket != null && initialPocket.targetAmount > 0) initialPocket.targetAmount.toString() else "") }
    var selectedEmoji by remember { mutableStateOf(initialPocket?.emoji ?: "👛") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_pocket_dialog"),
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
                        text = if (initialPocket == null) "Tambah Kantong Dana" else "Edit Kantong Dana",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_pocket_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tipe Kantong: Belanja vs Tabungan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp)
                ) {
                    val isSpending = type == PocketType.SPENDING
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSpending) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { type = PocketType.SPENDING }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belanja / Keluar",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSpending) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSpending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val isSavings = type == PocketType.SAVINGS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSavings) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { type = PocketType.SAVINGS }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tabungan Impian",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSavings) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSavings) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nama Kantong
                Text(
                    text = "Nama Sub Alokasi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth().testTag("pocket_name_input"),
                    placeholder = {
                        Text(if (type == PocketType.SPENDING) "Contoh: Uang Belanja 200rb" else "Contoh: Beli Laptop Baru")
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Saldo / Alokasi Awal
                Text(
                    text = if (type == PocketType.SPENDING) "Dana Dialokasikan Saat Ini" else "Jumlah Tabungan Terkumpul",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        balanceText = input.filter { it.isDigit() }.take(12)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("pocket_balance_input"),
                    placeholder = { Text("0") },
                    prefix = { Text("${currency.symbol} ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                // Jika Tabungan, tambahkan Input Target
                if (type == PocketType.SAVINGS) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Target Total Tabungan (Opsional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { input ->
                            targetText = input.filter { it.isDigit() }.take(12)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("pocket_target_input"),
                        placeholder = { Text("Contoh: 5000000") },
                        prefix = { Text("${currency.symbol} ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pilihan Emoji Ikon
                Text(
                    text = "Pilih Ikon",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PocketPresetEmojis.forEach { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Harap masukkan nama kantong"
                                return@Button
                            }
                            val parsedBalance = balanceText.toLongOrNull() ?: 0L
                            val parsedTarget = targetText.toLongOrNull() ?: 0L

                            val pocket = PocketEntity(
                                id = initialPocket?.id ?: 0L,
                                name = name.trim(),
                                type = type,
                                allocatedBalance = parsedBalance,
                                targetAmount = parsedTarget,
                                emoji = selectedEmoji,
                                createdAtMillis = initialPocket?.createdAtMillis ?: System.currentTimeMillis()
                            )
                            onSave(pocket)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
