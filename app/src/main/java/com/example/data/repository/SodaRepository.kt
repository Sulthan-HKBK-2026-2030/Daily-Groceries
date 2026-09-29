package com.example.data.repository

import com.example.data.local.dao.SodaDao
import com.example.data.local.entity.SodaSaleEntity
import kotlinx.coroutines.flow.Flow

class SodaRepository(private val sodaDao: SodaDao) {

    val allSales: Flow<List<SodaSaleEntity>> = sodaDao.getAllSales()

    fun getSalesBetween(startTime: Long, endTime: Long): Flow<List<SodaSaleEntity>> =
        sodaDao.getSalesBetween(startTime, endTime)

    suspend fun insertSale(sale: SodaSaleEntity) =
        sodaDao.insertSale(sale)

    suspend fun deleteSale(id: Long) =
        sodaDao.deleteSale(id)
}
