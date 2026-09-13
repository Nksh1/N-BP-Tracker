package com.example.data

import kotlinx.coroutines.flow.Flow
import java.io.StringWriter

class BloodPressureRepository(
    private val dao: BloodPressureDao,
    private val syncService: GoogleSheetsSyncService = GoogleSheetsSyncService()
) {
    val allReadings: Flow<List<BloodPressureReading>> = dao.getAllReadings()
    val latestReading: Flow<BloodPressureReading?> = dao.getLatestReading()
    val totalCount: Flow<Int> = dao.getCount()

    suspend fun insertReading(reading: BloodPressureReading, sheetsUrl: String, autoSync: Boolean): Long {
        val insertedId = dao.insert(reading)
        val readingWithId = reading.copy(id = insertedId)

        if (autoSync && sheetsUrl.isNotBlank()) {
            syncSingle(readingWithId, sheetsUrl)
        }
        return insertedId
    }

    suspend fun syncSingle(reading: BloodPressureReading, sheetsUrl: String): SyncResult {
        val result = syncService.syncReading(sheetsUrl, reading)
        when (result) {
            is SyncResult.Success -> {
                dao.markAsSynced(reading.id, System.currentTimeMillis())
            }
            is SyncResult.Error -> {
                dao.markSyncFailed(reading.id, result.errorMessage)
            }
        }
        return result
    }

    suspend fun syncAllPending(sheetsUrl: String): Pair<Int, Int> {
        val pending = dao.getUnsyncedReadings()
        var successCount = 0
        var failCount = 0

        for (reading in pending) {
            val result = syncSingle(reading, sheetsUrl)
            if (result is SyncResult.Success) {
                successCount++
            } else {
                failCount++
            }
        }
        return Pair(successCount, failCount)
    }

    suspend fun deleteReading(reading: BloodPressureReading) {
        dao.delete(reading)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun testSheetsConnection(url: String): SyncResult {
        return syncService.testConnection(url)
    }

    fun generateCsv(readings: List<BloodPressureReading>): String {
        val sw = StringWriter()
        sw.append("Date,Time,Systolic (mmHg),Diastolic (mmHg),Pulse (bpm),Category,Arm,Context,Notes,Google Sheet Synced\n")
        for (r in readings) {
            val escapedNotes = "\"" + r.notes.replace("\"", "\"\"") + "\""
            sw.append("${r.formattedDate},${r.formattedTime},${r.systolic},${r.diastolic},${r.pulse},\"${r.category}\",\"${r.arm}\",\"${r.tag}\",$escapedNotes,${if (r.isSynced) "Yes" else "No"}\n")
        }
        return sw.toString()
    }
}
