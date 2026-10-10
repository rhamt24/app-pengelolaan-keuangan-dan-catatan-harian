package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(startTime, endTime)

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    suspend fun insert(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun insertAll(transactions: List<TransactionEntity>) =
        transactionDao.insertAll(transactions)

    suspend fun update(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun delete(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteById(id: Long) =
        transactionDao.deleteById(id)

    suspend fun getCount(): Int =
        transactionDao.getCount()
}
