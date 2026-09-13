package com.example.data

import android.content.Context
import android.content.SharedPreferences

class GoogleSheetsPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nani_bp_sheets_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WEBHOOK_URL = "key_webhook_url"
        private const val KEY_SHEET_NAME = "key_sheet_name"
        private const val KEY_AUTO_SYNC = "key_auto_sync"
        private const val KEY_NANI_NAME = "key_nani_name"
        private const val KEY_LAST_SYNC_TIME = "key_last_sync_time"
        private const val KEY_LAST_SYNC_STATUS = "key_last_sync_status"
    }

    var webhookUrl: String
        get() = prefs.getString(KEY_WEBHOOK_URL, "").orEmpty().trim()
        set(value) = prefs.edit().putString(KEY_WEBHOOK_URL, value.trim()).apply()

    var sheetName: String
        get() = prefs.getString(KEY_SHEET_NAME, "Sheet1").orEmpty()
        set(value) = prefs.edit().putString(KEY_SHEET_NAME, value.trim()).apply()

    var autoSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC, value).apply()

    var naniName: String
        get() = prefs.getString(KEY_NANI_NAME, "Nani").orEmpty()
        set(value) = prefs.edit().putString(KEY_NANI_NAME, value.trim()).apply()

    var lastSyncTime: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC_TIME, value).apply()

    var lastSyncStatus: String
        get() = prefs.getString(KEY_LAST_SYNC_STATUS, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_LAST_SYNC_STATUS, value).apply()

    val isConfigured: Boolean
        get() = webhookUrl.isNotBlank() && (webhookUrl.startsWith("http://") || webhookUrl.startsWith("https://"))
}
