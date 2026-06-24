package com.example.data.repository

import com.example.data.database.CalculationHistory
import com.example.data.database.HistoryDao
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<CalculationHistory>> = historyDao.getAllHistory()

    suspend fun insert(history: CalculationHistory) {
        historyDao.insertHistory(history)
    }

    suspend fun deleteById(id: Int) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearAll() {
        historyDao.clearAllHistory()
    }
}
