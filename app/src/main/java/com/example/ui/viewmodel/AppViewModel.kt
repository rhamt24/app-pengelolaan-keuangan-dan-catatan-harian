package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.CurrencyType
import com.example.data.model.NoteEntity
import com.example.data.model.PocketEntity
import com.example.data.model.PocketType
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.data.repository.NotesRepository
import com.example.data.repository.PocketRepository
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
    val todayExpense: Long = 0L,
    val monthlyIncome: Long = 0L,
    val monthlyExpense: Long = 0L
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
    private val pocketRepository = PocketRepository(database.pocketDao())

    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    // Settings States
    private val _selectedCurrency = MutableStateFlow(
        try {
            val code = prefs.getString("setting_currency", CurrencyType.IDR.name) ?: CurrencyType.IDR.name
            CurrencyType.valueOf(code)
        } catch (e: Exception) {
            CurrencyType.IDR
        }
    )
    val selectedCurrency: StateFlow<CurrencyType> = _selectedCurrency.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(
        try {
            val code = prefs.getString("setting_language", AppLanguage.INDONESIAN.name) ?: AppLanguage.INDONESIAN.name
            AppLanguage.valueOf(code)
        } catch (e: Exception) {
            AppLanguage.INDONESIAN
        }
    )
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _selectedThemeMode = MutableStateFlow(
        try {
            val code = prefs.getString("setting_theme", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
            AppThemeMode.valueOf(code)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    )
    val selectedThemeMode: StateFlow<AppThemeMode> = _selectedThemeMode.asStateFlow()

    // Sub-Allocations / Pockets
    val pockets: StateFlow<List<PocketEntity>> = pocketRepository.allPockets
        .catch { e ->
            Log.e("AppViewModel", "Error collecting pockets", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions & Notes
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

    private val _selectedPocketFilter = MutableStateFlow<Long?>(null) // null = all, 0 = main wallet, >0 = specific pocket
    val selectedPocketFilter: StateFlow<Long?> = _selectedPocketFilter.asStateFlow()

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        _financeSearchQuery,
        _selectedTypeFilter,
        _selectedCategoryFilter,
        _selectedPocketFilter
    ) { list, query, typeFilter, catFilter, pocketFilter ->
        try {
            val cleanQuery = query.trim()
            list.filter { tx ->
                val matchesQuery = cleanQuery.isEmpty() ||
                        tx.title.contains(cleanQuery, ignoreCase = true) ||
                        tx.category.contains(cleanQuery, ignoreCase = true) ||
                        tx.note.contains(cleanQuery, ignoreCase = true)
                val matchesType = typeFilter == null || tx.type == typeFilter
                val matchesCat = catFilter == null || tx.category == catFilter
                val matchesPocket = pocketFilter == null || tx.pocketId == pocketFilter
                matchesQuery && matchesType && matchesCat && matchesPocket
            }
        } catch (e: Exception) {
            Log.e("AppViewModel", "Error filtering transactions", e)
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Finance Summary: Includes Today & Monthly breakdowns
    val financeSummary: StateFlow<FinanceSummary> = transactions.combine(_selectedPocketFilter) { list, pocketFilter ->
        try {
            val now = System.currentTimeMillis()
            var totalInc = 0L
            var totalExp = 0L
            var todayInc = 0L
            var todayExp = 0L
            var monthlyInc = 0L
            var monthlyExp = 0L

            val targetList = if (pocketFilter == null) list else list.filter { it.pocketId == pocketFilter }

            for (tx in targetList) {
                val isToday = Formatters.isSameDay(tx.dateMillis, now)
                val isThisMonth = Formatters.isSameMonth(tx.dateMillis, now)

                if (tx.type == TransactionType.INCOME) {
                    totalInc += tx.amount
                    if (isToday) todayInc += tx.amount
                    if (isThisMonth) monthlyInc += tx.amount
                } else {
                    totalExp += tx.amount
                    if (isToday) todayExp += tx.amount
                    if (isThisMonth) monthlyExp += tx.amount
                }
            }

            FinanceSummary(
                totalIncome = totalInc,
                totalExpense = totalExp,
                balance = totalInc - totalExp,
                todayIncome = todayInc,
                todayExpense = todayExp,
                monthlyIncome = monthlyInc,
                monthlyExpense = monthlyExp
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
            val cleanQuery = query.trim()
            list.filter { note ->
                val matchesQuery = cleanQuery.isEmpty() ||
                        note.title.contains(cleanQuery, ignoreCase = true) ||
                        note.content.contains(cleanQuery, ignoreCase = true)
                val matchesTag = tagFilter == null || note.tag == tagFilter
                val matchesFav = !favOnly || note.isFavorite
                matchesQuery && matchesTag && matchesFav
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
    fun setPocketFilter(pocketId: Long?) { _selectedPocketFilter.value = pocketId }

    fun setNotesSearchQuery(q: String) { _notesSearchQuery.value = q }
    fun setNoteTagFilter(tag: String?) { _selectedNoteTag.value = tag }
    fun toggleFavoriteFilter() { _showOnlyFavorites.value = !_showOnlyFavorites.value }

    // Settings actions
    fun setCurrency(currency: CurrencyType) {
        _selectedCurrency.value = currency
        prefs.edit().putString("setting_currency", currency.name).apply()
    }

    fun setLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang
        prefs.edit().putString("setting_language", lang.name).apply()
    }

    fun setThemeMode(mode: AppThemeMode) {
        _selectedThemeMode.value = mode
        prefs.edit().putString("setting_theme", mode.name).apply()
    }

    // Pocket / Sub-Allocation Actions
    fun addPocket(pocket: PocketEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pocketRepository.insert(pocket)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error adding pocket", e)
            }
        }
    }

    fun updatePocket(pocket: PocketEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pocketRepository.update(pocket)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error updating pocket", e)
            }
        }
    }

    fun deletePocket(pocket: PocketEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pocketRepository.delete(pocket)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error deleting pocket", e)
            }
        }
    }

    fun allocateFundsToPocket(pocketId: Long, amount: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pocketRepository.adjustBalance(pocketId, amount)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Error allocating funds", e)
            }
        }
    }

    // Finance Actions
    fun addTransaction(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                financeRepository.insert(tx)
                // If the transaction belongs to a pocket, adjust the pocket balance
                if (tx.pocketId > 0L) {
                    val delta = if (tx.type == TransactionType.INCOME) tx.amount else -tx.amount
                    pocketRepository.adjustBalance(tx.pocketId, delta)
                }
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
                // Revert pocket balance if needed
                if (tx.pocketId > 0L) {
                    val revertDelta = if (tx.type == TransactionType.INCOME) -tx.amount else tx.amount
                    pocketRepository.adjustBalance(tx.pocketId, revertDelta)
                }
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
