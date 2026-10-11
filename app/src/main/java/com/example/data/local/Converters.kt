package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.PocketType
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String {
        return value?.name ?: TransactionType.EXPENSE.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType {
        return try {
            if (value != null) TransactionType.valueOf(value) else TransactionType.EXPENSE
        } catch (e: Exception) {
            TransactionType.EXPENSE
        }
    }

    @TypeConverter
    fun fromPocketType(value: PocketType?): String {
        return value?.name ?: PocketType.SPENDING.name
    }

    @TypeConverter
    fun toPocketType(value: String?): PocketType {
        return try {
            if (value != null) PocketType.valueOf(value) else PocketType.SPENDING
        } catch (e: Exception) {
            PocketType.SPENDING
        }
    }
}
