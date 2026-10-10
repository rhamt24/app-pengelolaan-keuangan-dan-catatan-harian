package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Immutable
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val category: String,
    val paymentMethod: String,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

val ExpenseCategories = listOf(
    "Makanan & Minuman" to "🍔",
    "Transportasi" to "🚗",
    "Belanja Kebutuhan" to "🛒",
    "Tagihan & Utilitas" to "💡",
    "Hiburan & Hobi" to "🎮",
    "Kesehatan & Obat" to "💊",
    "Pendidikan & Buku" to "📚",
    "Sosial & Sedekah" to "🤝",
    "Lainnya" to "📦"
)

val IncomeCategories = listOf(
    "Gaji Bulanan" to "💰",
    "Bisnis & Jualan" to "💼",
    "Pekerjaan Sampingan" to "💻",
    "Bonus & THR" to "🎁",
    "Investasi" to "📈",
    "Uang Saku" to "🪙",
    "Lainnya" to "✨"
)

val PaymentMethods = listOf(
    "Tunai (Cash)" to "💵",
    "Transfer Bank" to "🏦",
    "E-Wallet (GoPay/OVO/Dana)" to "📱",
    "Kartu Debit/Kredit" to "💳",
    "QRIS" to "📲"
)
