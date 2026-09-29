package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SodaSaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SodaDao {
    @Query("SELECT * FROM soda_sales ORDER BY dateMillis DESC")
    fun getAllSales(): Flow<List<SodaSaleEntity>>

    @Query("SELECT * FROM soda_sales WHERE dateMillis >= :startTime AND dateMillis <= :endTime ORDER BY dateMillis DESC")
    fun getSalesBetween(startTime: Long, endTime: Long): Flow<List<SodaSaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SodaSaleEntity): Long

    @Query("DELETE FROM soda_sales WHERE id = :id")
    suspend fun deleteSale(id: Long)
}
