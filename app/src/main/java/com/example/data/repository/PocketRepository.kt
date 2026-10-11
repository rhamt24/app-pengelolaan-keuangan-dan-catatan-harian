package com.example.data.repository

import com.example.data.local.PocketDao
import com.example.data.model.PocketEntity
import kotlinx.coroutines.flow.Flow

class PocketRepository(private val pocketDao: PocketDao) {
    val allPockets: Flow<List<PocketEntity>> = pocketDao.getAllPockets()

    suspend fun insert(pocket: PocketEntity): Long {
        return pocketDao.insertPocket(pocket)
    }

    suspend fun update(pocket: PocketEntity) {
        pocketDao.updatePocket(pocket)
    }

    suspend fun delete(pocket: PocketEntity) {
        pocketDao.deletePocket(pocket)
    }

    suspend fun adjustBalance(id: Long, delta: Long) {
        pocketDao.adjustBalance(id, delta)
    }

    suspend fun getCount(): Int {
        return pocketDao.getCount()
    }
}
