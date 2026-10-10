package com.example.data.local

import androidx.room.TypeConverter
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
}
