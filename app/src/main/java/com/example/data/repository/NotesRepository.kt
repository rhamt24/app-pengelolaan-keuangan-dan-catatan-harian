package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()

    fun getNotesByTag(tag: String): Flow<List<NoteEntity>> =
        noteDao.getNotesByTag(tag)

    suspend fun getNoteById(id: Long): NoteEntity? =
        noteDao.getNoteById(id)

    suspend fun insert(note: NoteEntity): Long =
        noteDao.insertNote(note)

    suspend fun insertAll(notes: List<NoteEntity>) =
        noteDao.insertAll(notes)

    suspend fun update(note: NoteEntity) =
        noteDao.updateNote(note)

    suspend fun delete(note: NoteEntity) =
        noteDao.deleteNote(note)

    suspend fun deleteById(id: Long) =
        noteDao.deleteById(id)

    suspend fun togglePin(id: Long, currentPin: Boolean) =
        noteDao.updatePinStatus(id, !currentPin)

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) =
        noteDao.updateFavoriteStatus(id, !currentFavorite)

    suspend fun getCount(): Int =
        noteDao.getCount()
}
