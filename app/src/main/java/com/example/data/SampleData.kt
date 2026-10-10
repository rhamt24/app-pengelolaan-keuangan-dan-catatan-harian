package com.example.data

import com.example.data.model.NoteEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.util.concurrent.TimeUnit

object SampleData {
    fun getInitialTransactions(): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - TimeUnit.HOURS.toMillis(2)
        val oneDayAgo = now - TimeUnit.DAYS.toMillis(1)
        val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)
        val threeDaysAgo = now - TimeUnit.DAYS.toMillis(3)

        return listOf(
            TransactionEntity(
                id = 1L,
                title = "Gaji Pokok Bulanan",
                amount = 7500000L,
                type = TransactionType.INCOME,
                category = "Gaji Bulanan",
                paymentMethod = "Transfer Bank",
                note = "Transfer payroll kantor",
                dateMillis = threeDaysAgo
            ),
            TransactionEntity(
                id = 2L,
                title = "Belanja Mingguan Supermarket",
                amount = 345000L,
                type = TransactionType.EXPENSE,
                category = "Belanja Kebutuhan",
                paymentMethod = "QRIS",
                note = "Bahan makanan, susu, dan perlengkapan mandi",
                dateMillis = twoDaysAgo
            ),
            TransactionEntity(
                id = 3L,
                title = "Token Listrik & Air PLN",
                amount = 200000L,
                type = TransactionType.EXPENSE,
                category = "Tagihan & Utilitas",
                paymentMethod = "Transfer Bank",
                note = "Pembayaran utilitas rumah",
                dateMillis = twoDaysAgo
            ),
            TransactionEntity(
                id = 4L,
                title = "Proyek Desain Sampingan",
                amount = 1200000L,
                type = TransactionType.INCOME,
                category = "Pekerjaan Sampingan",
                paymentMethod = "E-Wallet (GoPay/OVO/Dana)",
                note = "Desain logo dan banner UMKM",
                dateMillis = oneDayAgo
            ),
            TransactionEntity(
                id = 5L,
                title = "Makan Siang & Es Teh",
                amount = 35000L,
                type = TransactionType.EXPENSE,
                category = "Makanan & Minuman",
                paymentMethod = "Tunai (Cash)",
                note = "Makan siang di kantin",
                dateMillis = oneHourAgo
            ),
            TransactionEntity(
                id = 6L,
                title = "Bensin Motor & Parkir",
                amount = 40000L,
                type = TransactionType.EXPENSE,
                category = "Transportasi",
                paymentMethod = "Tunai (Cash)",
                note = "Isi bensin pertalite",
                dateMillis = now
            )
        )
    }

    fun getInitialNotes(): List<NoteEntity> {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - TimeUnit.DAYS.toMillis(1)
        val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)

        return listOf(
            NoteEntity(
                id = 1L,
                title = "Target Finansial & Tabungan Bulan Ini",
                content = "1. Sisihkan minimal 30% dari penghasilan untuk dana darurat.\n2. Kurangi jajan kopi di luar maksimal 2x seminggu.\n3. Catat setiap pengeluaran sekecil apapun di aplikasi ini.\n4. Alokasikan Rp 500.000 untuk instrumen reksadana.",
                tag = "Keuangan",
                mood = "🎯",
                isPinned = true,
                isFavorite = true,
                colorIndex = 1,
                createdAtMillis = twoDaysAgo,
                updatedAtMillis = twoDaysAgo
            ),
            NoteEntity(
                id = 2L,
                title = "Refleksi Harian & Evaluasi",
                content = "Hari ini pekerjaan berjalan lancar dan selesai tepat waktu. Sangat lega bisa menyelesaikan tugas lebih awal sehingga sore hari bisa istirahat dan berkumpul bersama keluarga.",
                tag = "Harian",
                mood = "😌",
                isPinned = true,
                isFavorite = false,
                colorIndex = 0,
                createdAtMillis = oneDayAgo,
                updatedAtMillis = oneDayAgo
            ),
            NoteEntity(
                id = 3L,
                title = "Daftar Belanja Kebutuhan Rumah",
                content = "• Beras 5 kg\n• Minyak goreng 2 liter\n• Telur ayam 1 kg\n• Kopi arabika\n• Sabun pencuci piring & deterjen\n• Buah apel & pisang",
                tag = "Belanja",
                mood = "😊",
                isPinned = false,
                isFavorite = false,
                colorIndex = 2,
                createdAtMillis = now,
                updatedAtMillis = now
            ),
            NoteEntity(
                id = 4L,
                title = "Ide Kreatif Proyek Akhir Pekan",
                content = "Bikin portofolio online baru dan eksplorasi tren UI terbaru. Buat catatan ide-ide konten edukasi keuangan sederhana untuk dibagikan di media sosial.",
                tag = "Ide",
                mood = "💡",
                isPinned = false,
                isFavorite = true,
                colorIndex = 3,
                createdAtMillis = now,
                updatedAtMillis = now
            )
        )
    }
}
