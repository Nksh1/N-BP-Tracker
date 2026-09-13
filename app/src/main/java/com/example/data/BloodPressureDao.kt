package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {

    @Query("SELECT * FROM bp_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<BloodPressureReading>>

    @Query("SELECT * FROM bp_readings WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedReadings(): List<BloodPressureReading>

    @Query("SELECT * FROM bp_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReading(): Flow<BloodPressureReading?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: BloodPressureReading): Long

    @Update
    suspend fun update(reading: BloodPressureReading)

    @Delete
    suspend fun delete(reading: BloodPressureReading)

    @Query("DELETE FROM bp_readings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE bp_readings SET isSynced = 1, syncStatus = 'SYNCED', syncTime = :syncTime, syncError = null WHERE id = :id")
    suspend fun markAsSynced(id: Long, syncTime: Long)

    @Query("UPDATE bp_readings SET isSynced = 0, syncStatus = 'FAILED', syncError = :error WHERE id = :id")
    suspend fun markSyncFailed(id: Long, error: String)

    @Query("SELECT COUNT(*) FROM bp_readings")
    fun getCount(): Flow<Int>
}
