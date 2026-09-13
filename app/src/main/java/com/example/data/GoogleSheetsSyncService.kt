package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SyncResult {
    data class Success(val message: String) : SyncResult()
    data class Error(val errorMessage: String) : SyncResult()
}

class GoogleSheetsSyncService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun syncReading(url: String, reading: BloodPressureReading, sheetName: String = "Sheet1"): SyncResult =
        withContext(Dispatchers.IO) {
            if (url.isBlank()) {
                return@withContext SyncResult.Error("Google Sheets Webhook URL is not configured.")
            }

            try {
                val jsonPayload = JSONObject().apply {
                    put("id", reading.id)
                    put("timestamp", reading.timestamp)
                    put("date", reading.formattedDate)
                    put("time", reading.formattedTime)
                    put("dateTime", reading.formattedDateTimeIso)
                    put("systolic", reading.systolic)
                    put("diastolic", reading.diastolic)
                    put("pulse", reading.pulse)
                    put("category", reading.category)
                    put("notes", reading.notes)
                    put("arm", reading.arm)
                    put("tag", reading.tag)
                    put("sheetName", sheetName)
                }

                val requestBody = jsonPayload.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .header("Accept", "application/json, text/plain, */*")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 302 || response.code == 200 || response.code == 201) {
                        val bodyText = response.body?.string() ?: ""
                        SyncResult.Success(
                            if (bodyText.isNotBlank() && bodyText.length < 100) bodyText else "Logged to Google Sheet successfully"
                        )
                    } else {
                        SyncResult.Error("HTTP Error: ${response.code} ${response.message}")
                    }
                }
            } catch (e: IOException) {
                SyncResult.Error("Network error: ${e.localizedMessage ?: "Failed to connect to Google Sheets"}")
            } catch (e: Exception) {
                SyncResult.Error("Error: ${e.localizedMessage ?: "Unexpected error"}")
            }
        }

    suspend fun testConnection(url: String): SyncResult = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            return@withContext SyncResult.Error("Please enter a valid Google Sheets Webhook URL")
        }

        try {
            val testPayload = JSONObject().apply {
                put("type", "TEST_PING")
                put("date", "Test Date")
                put("time", "Test Time")
                put("systolic", 120)
                put("diastolic", 80)
                put("pulse", 72)
                put("category", "Test Ping (OK)")
                put("notes", "Connection verified from Nani BP Tracker app")
                put("arm", "N/A")
                put("tag", "Setup Test")
            }

            val requestBody = testPayload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Accept", "application/json, text/plain, */*")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 302 || response.code == 200) {
                    SyncResult.Success("Connected successfully! Your Google Sheet is ready.")
                } else {
                    SyncResult.Error("Google Sheets responded with error code: ${response.code}")
                }
            }
        } catch (e: IOException) {
            SyncResult.Error("Could not reach Google Sheets: ${e.localizedMessage ?: "Check your internet connection"}")
        } catch (e: Exception) {
            SyncResult.Error("Test failed: ${e.localizedMessage}")
        }
    }

    companion object {
        val SAMPLE_APPS_SCRIPT_CODE = """
// --- Google Apps Script for Nani's Blood Pressure Tracker ---
// 1. Open your Google Sheet
// 2. Click Extensions > Apps Script
// 3. Delete everything and paste this code:

function doPost(e) {
  var lock = LockService.getScriptLock();
  lock.tryLock(10000);
  
  try {
    var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
    
    // Create header row if empty
    if (sheet.getLastRow() === 0) {
      sheet.appendRow([
        "Date", "Time", "Systolic (Top)", "Diastolic (Bottom)", 
        "Pulse (bpm)", "Health Category", "Arm", "Context", "Notes"
      ]);
      sheet.getRange(1, 1, 1, 9).setFontWeight("bold").setBackground("#FCE4EC");
    }
    
    var data = JSON.parse(e.postData.contents);
    sheet.appendRow([
      data.date || new Date().toLocaleDateString(),
      data.time || new Date().toLocaleTimeString(),
      data.systolic || "",
      data.diastolic || "",
      data.pulse || "",
      data.category || "",
      data.arm || "",
      data.tag || "",
      data.notes || ""
    ]);
    
    return ContentService
      .createTextOutput(JSON.stringify({ status: "success", message: "Recorded to Google Sheet" }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService
      .createTextOutput(JSON.stringify({ status: "error", error: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  } finally {
    lock.releaseLock();
  }
}

// 4. Click 'Deploy' > 'New deployment'
// 5. Select type: 'Web app'
// 6. Set 'Execute as': 'Me' and 'Who has access': 'Anyone'
// 7. Click 'Deploy' and copy the Web App URL into the app!
        """.trimIndent()
    }
}
