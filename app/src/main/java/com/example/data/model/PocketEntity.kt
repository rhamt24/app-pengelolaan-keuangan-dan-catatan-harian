package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

enum class PocketType {
    SPENDING, // Sub Alokasi Belanja / Pengeluaran (Terpisah dari Dompet Utama)
    SAVINGS   // Sub Alokasi Tabungan / Impian (Target Tabungan)
}

@Immutable
@Entity(tableName = "pockets")
data class PocketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: PocketType = PocketType.SPENDING,
    val allocatedBalance: Long = 0L,  // Saldo saat ini dalam sub-alokasi
    val targetAmount: Long = 0L,      // Target alokasi (khusus tabungan)
    val emoji: String = "👛",
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Immutable
data class TransactionSubItem(
    val name: String,
    val amount: Long
)

object SubItemParser {
    fun parse(json: String): List<TransactionSubItem> {
        if (json.isBlank() || !json.startsWith("[")) return emptyList()
        return try {
            val list = mutableListOf<TransactionSubItem>()
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.optString("name", "")
                val amount = obj.optLong("amount", 0L)
                if (name.isNotBlank() || amount > 0L) {
                    list.add(TransactionSubItem(name, amount))
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJson(items: List<TransactionSubItem>): String {
        if (items.isEmpty()) return ""
        val array = JSONArray()
        for (item in items) {
            if (item.name.isNotBlank() || item.amount > 0L) {
                val obj = JSONObject()
                obj.put("name", item.name)
                obj.put("amount", item.amount)
                array.put(obj)
            }
        }
        return array.toString()
    }
}
