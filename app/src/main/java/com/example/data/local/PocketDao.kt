package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PocketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PocketDao {
    @Query("SELECT * FROM pockets ORDER BY id ASC")
    fun getAllPockets(): Flow<List<PocketEntity>>

    @Query("SELECT * FROM pockets WHERE id = :id LIMIT 1")
    suspend fun getPocketById(id: Long): PocketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPocket(pocket: PocketEntity): Long

    @Update
    suspend fun updatePocket(pocket: PocketEntity)

    @Delete
    suspend fun deletePocket(pocket: PocketEntity)

    @Query("UPDATE pockets SET allocatedBalance = allocatedBalance + :delta WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Long)

    @Query("SELECT COUNT(*) FROM pockets")
    suspend fun getCount(): Int
}
