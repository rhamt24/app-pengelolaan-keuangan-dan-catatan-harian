package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.local.AppDatabase
import com.example.data.model.NoteEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.data.repository.NotesRepository
import com.example.ui.components.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class FinanceSummary(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val balance: Long = 0L,
    val todayIncome: Long = 0L,
    val todayExpense: Long = 0L
)

@Immutable
data class CategoryStat(
    val category: String,
    val totalAmount: Long,
    val percentage: Float,
    val count: Int
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val financeRepository = FinanceRepository(database.transactionDao())
    private val notesRepository = NotesRepository(database.noteDao())

    val transactions: StateFlow<List<TransactionEntity>> = financeRepository.allTransactions
        .catch { e ->
            Log.e("AppViewModel", "Error collecting transactions", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = notesRepository.allNotes
        .catch { e ->
            Log.e("AppViewModel", "Error collecting notes", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Finance Filters
    private val _financeSearchQuery = MutableStateFlow("")
    val financeSearchQuery: StateFlow<String> = _financeSearchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        _financeSearchQuery,
        _selectedTypeFilter,
        _selectedCategoryFilter
    ) { list, query, typeFilter, catFilter ->
        try {
            if (query.isBlank() && typeFilter == null && catFilter == null) {
                list
            } else {
                val cleanQuery = query.trim()
                list.filter { tx ->
                    val matchesQuery = cleanQuery.isEmpty() ||
                            tx.title.contains(cleanQuery, ignoreCase = true) ||
                            tx.category.contains(cleanQuery, ignoreCase = true) ||
                            tx.note.contains(cleanQuery, ignoreCase = true)
                    val matchesType = typeFilter == null || tx.type == typeFilter
                    val matchesCat = catFilter == null || tx.category == catFilter
                    matchesQuery && matchesType && matchesCat
                }
            }
        } catch (e: Exception) {
            Log.e("AppViewModel", "Error filtering transactions", e)
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Finance Summary
    val financeSummary: StateFlow<FinanceSummary> = transactions.combine(_financeSearchQuery) { list, _ ->
        try {
            val now = System.currentTimeMillis()
            var totalInc = 0L
            var totalExp = 0L
            var todayInc = 0L
            var todayExp = 0L

            for (tx in list) {
                val isToday = Formatters.isSameDay(tx.dateMillis, now)
                if (tx.type == TransactionType.INCOME) {
                    totalInc += tx.amount
                    if (isToday) todayInc += tx.amount
                } else {
                    totalExp += tx.amount
                    if (isToday) todayExp += tx.amount
                }
            }

            FinanceSummary(
                totalIncome = totalInc,
                totalExpense = totalExp,
                balance = totalInc - totalExp,
                todayIncome = todayInc,
                todayExpense = todayExp
            )
        } catch (e: Exception) {
            Log.e("AppViewModel", "Error calculating summary", e)
            FinanceSummary()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinanceSummary())

    // Expense Category Breakdown
    val expenseCategoryStats: StateFlow<List<CategoryStat>> = transactions.combine(_financeSearchQuery) { list, _ ->
        try {
            val expenses = list.filter { it.type == TransactionType.EXPENSE }
            val totalExp = expenses.sumOf { it.amount }

            if (totalExp <= 0L) {
                emptyList()
            } else {
                expenses.groupBy { it.category }
                    .map { (cat, txs) ->
                        val sum = txs.sumOf { it.amount }
                        val rawPercent = (sum.toFloat() / totalExp.toFloat()) * 100f
                        val safePercent = if (rawPercent.isNaN() || rawPercent.isInfinite()) 0f else rawPercent
                        CategoryStat(
                            category = cat,
                            totalAmount = sum,
                            percentage = safePercent,
                            count = txs.size
                        )
                    }
                    .sortedByDescending { it.totalAmount }
            }
        } catch (e: Exception) {
            Log.e("AppViewModel", "Error calculating category stats", e)
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notes Filters
    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    private val _selectedNoteTag = MutableStateFlow<String?>(null)
    val selectedNoteTag: StateFlow<String?> = _selectedNoteTag.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        notes,
        _notesSearchQuery,
        _selectedNoteTag,
        _showOnlyFavorites
    ) { list, query, tagFilter, favOnly ->
        try {
            if (query.isBlank() && tagFilter == null && !favOnly) {
                list
            } else {
                val cleanQuery = query.trim()
                list.filter { note ->
                    val matchesQuery = cleanQuery.isEmpty() ||
                            note.title.contains(cleanQuery, ignoreCase = true) ||
                            note.content.contains(cleanQuery, ignoreCase = true)
                    val matchesTag = tagFilter == null || note.tag == tagFilter
                    val matchesFav = !favOnly || note.isFavorite
                    matchesQuery && matchesTag && matchesFav
                }
            }
        } catch (e: Exception) {
            Log.e("AppViewModel", "Error filtering notes", e)
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Start with clean user data
    }

    // Filter controls
    fun setFinanceSearchQuery(q: String) { _financeSearchQuery.value = q }
    fun setTypeFilter(type: TransactionType?) { _selectedTypeFilter.value = type }
    fun setCategoryFilter(category: String?) { _selectedCategoryFilter.value = category }

    fun setNotesSearchQuery(q: String) { _notesSearchQuery.value = q }
    fun setNoteTagFilter(tag: String?) { _selectedNoteTag.value = tag }
    fun toggleFavoriteFilter() { _showOnlyFavorites.value = !_showOnlyFavorites.value }

    // Finance Actions
    fun addTransaction(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                financeRepository.insert(tx)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error inserting transaction", e)
            }
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                financeRepository.update(tx)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error updating transaction", e)
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                financeRepository.delete(tx)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error deleting transaction", e)
            }
        }
    }

    // Notes Actions
    fun addNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notesRepository.insert(note)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error inserting note", e)
            }
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notesRepository.update(note.copy(updatedAtMillis = System.currentTimeMillis()))
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error updating note", e)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notesRepository.delete(note)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error deleting note", e)
            }
        }
    }

    fun togglePinNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notesRepository.togglePin(note.id, note.isPinned)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error toggling pin", e)
            }
        }
    }

    fun toggleFavoriteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notesRepository.toggleFavorite(note.id, note.isFavorite)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error toggling favorite", e)
            }
        }
    }
}
