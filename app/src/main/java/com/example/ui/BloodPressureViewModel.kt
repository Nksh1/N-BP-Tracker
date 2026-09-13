package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BloodPressureClassifier
import com.example.data.BloodPressureReading
import com.example.data.BloodPressureRepository
import com.example.data.GoogleSheetsPreferences
import com.example.data.GoogleSheetsSyncService
import com.example.data.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BloodPressureViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = BloodPressureRepository(database.bloodPressureDao())
    private val prefs = GoogleSheetsPreferences(application)

    // Form inputs
    val systolicInput = MutableStateFlow("120")
    val diastolicInput = MutableStateFlow("80")
    val pulseInput = MutableStateFlow("72")
    val notesInput = MutableStateFlow("")
    val selectedArm = MutableStateFlow("Left Arm")
    val selectedTag = MutableStateFlow("Morning")

    // UI state
    val isSaving = MutableStateFlow(false)
    val isSyncing = MutableStateFlow(false)
    val userMessage = MutableStateFlow<String?>(null)

    // Sheets configuration state
    val sheetsUrl = MutableStateFlow(prefs.webhookUrl)
    val autoSyncEnabled = MutableStateFlow(prefs.autoSyncEnabled)

    val isSheetsConfigured: StateFlow<Boolean> = sheetsUrl.map {
        it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), prefs.isConfigured)

    // Reactive list of all readings
    val allReadings: StateFlow<List<BloodPressureReading>> = repository.allReadings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestReading: StateFlow<BloodPressureReading?> = repository.latestReading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendingSyncCount: StateFlow<Int> = allReadings.map { list ->
        list.count { !it.isSynced }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSystolicChange(value: String) {
        systolicInput.value = value
    }

    fun onDiastolicChange(value: String) {
        diastolicInput.value = value
    }

    fun onPulseChange(value: String) {
        pulseInput.value = value
    }

    fun onNotesChange(value: String) {
        notesInput.value = value
    }

    fun onArmSelect(arm: String) {
        selectedArm.value = arm
    }

    fun onTagSelect(tag: String) {
        selectedTag.value = tag
    }

    fun incrementSystolic(delta: Int = 1) {
        val curr = systolicInput.value.toIntOrNull() ?: 120
        systolicInput.value = (curr + delta).coerceIn(40, 300).toString()
    }

    fun decrementSystolic(delta: Int = 1) {
        val curr = systolicInput.value.toIntOrNull() ?: 120
        systolicInput.value = (curr - delta).coerceIn(40, 300).toString()
    }

    fun incrementDiastolic(delta: Int = 1) {
        val curr = diastolicInput.value.toIntOrNull() ?: 80
        diastolicInput.value = (curr + delta).coerceIn(30, 200).toString()
    }

    fun decrementDiastolic(delta: Int = 1) {
        val curr = diastolicInput.value.toIntOrNull() ?: 80
        diastolicInput.value = (curr - delta).coerceIn(30, 200).toString()
    }

    fun incrementPulse(delta: Int = 1) {
        val curr = pulseInput.value.toIntOrNull() ?: 70
        pulseInput.value = (curr + delta).coerceIn(30, 220).toString()
    }

    fun decrementPulse(delta: Int = 1) {
        val curr = pulseInput.value.toIntOrNull() ?: 70
        pulseInput.value = (curr - delta).coerceIn(30, 220).toString()
    }

    fun saveReading() {
        val sys = systolicInput.value.toIntOrNull()
        val dia = diastolicInput.value.toIntOrNull()
        val pulse = pulseInput.value.toIntOrNull() ?: 0

        if (sys == null || sys <= 0) {
            userMessage.value = "Please enter Systolic (Top) reading"
            return
        }
        if (dia == null || dia <= 0) {
            userMessage.value = "Please enter Diastolic (Bottom) reading"
            return
        }

        val categoryInfo = BloodPressureClassifier.classify(sys, dia)

        viewModelScope.launch {
            isSaving.value = true
            val reading = BloodPressureReading(
                systolic = sys,
                diastolic = dia,
                pulse = pulse,
                category = categoryInfo.title,
                notes = notesInput.value.trim(),
                arm = selectedArm.value,
                tag = selectedTag.value,
                isSynced = false,
                syncStatus = if (isSheetsConfigured.value) "PENDING" else "LOCAL_ONLY"
            )

            try {
                repository.insertReading(
                    reading = reading,
                    sheetsUrl = sheetsUrl.value,
                    autoSync = autoSyncEnabled.value
                )

                if (isSheetsConfigured.value && autoSyncEnabled.value) {
                    userMessage.value = "Saved & sent to Google Sheet!"
                } else {
                    userMessage.value = "Saved to Nani's log!"
                }

                // Reset notes
                notesInput.value = ""
            } catch (e: Exception) {
                userMessage.value = "Saved locally. Sync error: ${e.localizedMessage}"
            } finally {
                isSaving.value = false
            }
        }
    }

    fun retrySyncReading(reading: BloodPressureReading) {
        val url = sheetsUrl.value
        if (url.isBlank()) {
            userMessage.value = "Please configure your Google Sheets URL first"
            return
        }

        viewModelScope.launch {
            isSyncing.value = true
            val result = repository.syncSingle(reading, url)
            when (result) {
                is SyncResult.Success -> {
                    userMessage.value = "Synced to Google Sheet successfully!"
                }
                is SyncResult.Error -> {
                    userMessage.value = "Sync failed: ${result.errorMessage}"
                }
            }
            isSyncing.value = false
        }
    }

    fun syncAllPending() {
        val url = sheetsUrl.value
        if (url.isBlank()) {
            userMessage.value = "Please enter your Google Sheets URL in settings"
            return
        }

        viewModelScope.launch {
            isSyncing.value = true
            val (success, failed) = repository.syncAllPending(url)
            if (failed == 0) {
                userMessage.value = "All $success readings synced to Google Sheet!"
            } else {
                userMessage.value = "Synced $success readings. $failed failed to sync."
            }
            isSyncing.value = false
        }
    }

    fun deleteReading(reading: BloodPressureReading) {
        viewModelScope.launch {
            repository.deleteReading(reading)
            userMessage.value = "Reading removed"
        }
    }

    fun updateSheetsConfig(url: String, autoSync: Boolean) {
        val trimmed = url.trim()
        prefs.webhookUrl = trimmed
        prefs.autoSyncEnabled = autoSync
        sheetsUrl.value = trimmed
        autoSyncEnabled.value = autoSync
        userMessage.value = "Google Sheets settings saved!"
    }

    fun testConnection(url: String, onResult: (SyncResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.testSheetsConnection(url)
            onResult(result)
        }
    }

    fun shareAsCsv(context: Context) {
        val readings = allReadings.value
        if (readings.isEmpty()) {
            userMessage.value = "No readings to export yet"
            return
        }

        val csvData = repository.generateCsv(readings)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvData)
            putExtra(Intent.EXTRA_SUBJECT, "Nani Blood Pressure Log (${readings.size} entries)")
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Blood Pressure Log to Google Sheets / Drive")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun dismissUserMessage() {
        userMessage.value = null
    }
}
