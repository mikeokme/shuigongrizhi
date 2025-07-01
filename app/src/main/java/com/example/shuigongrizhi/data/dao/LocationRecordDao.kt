package com.example.shuigongrizhi.data.dao

import androidx.room.*
import com.example.shuigongrizhi.data.entity.LocationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationRecordDao {
    @Insert
    suspend fun insert(record: LocationRecord): Long

    @Update
    suspend fun update(record: LocationRecord)

    @Query("SELECT * FROM location_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<LocationRecord>>

    @Query("SELECT * FROM location_records WHERE logId = :logId")
    fun getByLogId(logId: Long): Flow<List<LocationRecord>>

    @Query("SELECT * FROM location_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRecord(): LocationRecord?
}