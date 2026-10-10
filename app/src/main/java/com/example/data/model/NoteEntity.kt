package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val tag: String = "Harian",
    val mood: String = "😊",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val colorIndex: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

val NoteTags = listOf(
    "Harian",
    "Pekerjaan",
    "Pribadi",
    "Ide",
    "Rencana",
    "Keuangan",
    "Belanja"
)

val NoteMoods = listOf(
    "😊" to "Senang",
    "😌" to "Tenang",
    "💼" to "Produktif",
    "💡" to "Kreatif",
    "🎯" to "Fokus",
    "☕" to "Santai",
    "😴" to "Lelah"
)
