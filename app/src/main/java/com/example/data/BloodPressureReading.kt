package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "bp_readings")
data class BloodPressureReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int = 0,
    val category: String,
    val notes: String = "",
    val arm: String = "Left Arm",
    val tag: String = "General",
    val isSynced: Boolean = false,
    val syncStatus: String = "PENDING", // "SYNCED", "PENDING", "FAILED", "LOCAL_ONLY"
    val syncTime: Long = 0L,
    val syncError: String? = null
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

    val formattedTime: String
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

    val formattedDateTimeIso: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    val summaryText: String
        get() = "$systolic/$diastolic mmHg" + if (pulse > 0) " (Pulse: $pulse bpm)" else ""
}
