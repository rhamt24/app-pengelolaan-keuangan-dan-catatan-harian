package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer

data class TutorialStep(
    val stepNumber: Int,
    val iconEmoji: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val tip: String
)

val tutorialSteps = listOf(
    TutorialStep(
        stepNumber = 1,
        iconEmoji = "💸",
        title = "Langkah 1: Catat Pengeluaran",
        subtitle = "Catat setiap uang yang keluar",
        description = "Tekan tombol 'Keluar' pada Beranda atau tombol (+) di menu Keuangan. Masukkan nominal uang yang dikeluarkan, pilih kategori pengeluaran (misal Makanan, Bensin, Belanja), pilih metode bayar (Tunai/QRIS), lalu tekan Simpan.",
        tip = "💡 Tips: Gunakan tombol cepat seperti +10rb, +20rb, atau +50rb agar penginputan lebih instan."
    ),
    TutorialStep(
        stepNumber = 2,
        iconEmoji = "💰",
        title = "Langkah 2: Catat Pemasukan",
        subtitle = "Hitung saldo dompet otomatis",
        description = "Tekan tombol 'Masuk' saat Anda menerima gaji, komisi, hasil jualan, atau uang saku. Sistem akan otomatis menghitung total saldo bersih Anda secara real-time tanpa perlu kalkulator manual.",
        tip = "💡 Tips: Pantau saldo di kartu Beranda untuk memastikan kondisi keuangan selalu aman."
    ),
    TutorialStep(
        stepNumber = 3,
        iconEmoji = "✍️",
        title = "Langkah 3: Tulis Catatan Harian",
        subtitle = "Jurnal, ide, dan rencana harian",
        description = "Buka menu 'Catatan' dan tekan 'Tulis Catatan'. Anda bisa menuangkan ide, daftar belanja, agenda kerja, atau refleksi malam. Pilih Mood hari ini (😊 Senang, 💼 Produktif, dll.) dan label kategori.",
        tip = "💡 Tips: Tekan ikon Pin 📌 pada catatan penting agar selalu tampil paling atas."
    ),
    TutorialStep(
        stepNumber = 4,
        iconEmoji = "📊",
        title = "Langkah 4: Pantau Statistik",
        subtitle = "Evaluasi rasio tabungan & gaya hidup",
        description = "Buka menu 'Statistik' untuk melihat persentase uang yang paling banyak terpakai berdasarkan kategori. Cek 'Rasio Tabungan' Anda untuk melihat apakah Anda sudah hemat atau over-budget.",
        tip = "💡 Tips: Targetkan rasio tabungan minimal 20% dari total pemasukan bulanan."
    ),
    TutorialStep(
        stepNumber = 5,
        iconEmoji = "💬",
        title = "Langkah 5: Saluran Komunitas & Creator",
        subtitle = "Dibuat oleh zals",
        description = "Aplikasi ini dikembangkan oleh zals untuk membantu pencatatan keuangan dan produktivitas harian yang rapi dan bebas repot. Bergabunglah dengan Saluran WhatsApp resmi untuk update, tutorial, dan tips bermanfaat.",
        tip = "🌟 Klik tombol Saluran WhatsApp di bawah untuk bergabung langsung!"
    )
)

const val WHATSAPP_CHANNEL_URL = "https://whatsapp.com/channel/0029VaYuIQT2v1IjZmqTNG3x"

fun openWhatsAppChannel(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_CHANNEL_URL))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback or ignore
    }
}

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = tutorialSteps[currentStepIndex]
    val totalSteps = tutorialSteps.size

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("tutorial_dialog"),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            tonalElevation = 6.dp
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
                    Column {
                        Text(
                            text = "PANDUAN APLIKASI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Langkah ${currentStepIndex + 1} dari $totalSteps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_tutorial_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step Progress Indicator Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 0 until totalSteps) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i <= currentStepIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Big Step Icon Emoji
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.iconEmoji,
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title & Subtitle
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = step.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = step.tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                // If on Step 5 (or any step), display WhatsApp Channel button
                if (currentStepIndex == totalSteps - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { openWhatsAppChannel(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366) // WhatsApp Green
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_channel_button")
                    ) {
                        Text(text = "💬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gabung Saluran WhatsApp zals",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Controls (Prev / Next)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("prev_tutorial_step_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kembali")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStepIndex < totalSteps - 1) {
                        Button(
                            onClick = { currentStepIndex++ },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("next_tutorial_step_button")
                        ) {
                            Text("Lanjut")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("finish_tutorial_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Selesai")
                        }
                    }
                }
            }
        }
    }
}
