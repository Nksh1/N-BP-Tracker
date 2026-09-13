package com.example.data

import androidx.compose.ui.graphics.Color

enum class BpCategoryLevel {
    LOW,
    NORMAL,
    ELEVATED,
    STAGE_1,
    STAGE_2,
    CRISIS,
    UNKNOWN
}

data class BpCategoryInfo(
    val level: BpCategoryLevel,
    val title: String,
    val subtitle: String,
    val advice: String,
    val colorHex: Long,
    val containerColorHex: Long
) {
    val color: Color get() = Color(colorHex)
    val containerColor: Color get() = Color(containerColorHex)
}

object BloodPressureClassifier {
    fun classify(systolic: Int, diastolic: Int): BpCategoryInfo {
        return when {
            systolic <= 0 || diastolic <= 0 -> BpCategoryInfo(
                level = BpCategoryLevel.UNKNOWN,
                title = "Enter Values",
                subtitle = "Please enter both Top (Systolic) & Bottom (Diastolic) numbers",
                advice = "Take a restful seat for 5 minutes before measuring.",
                colorHex = 0xFF757575,
                containerColorHex = 0xFFF5F5F5
            )
            systolic > 180 || diastolic > 120 -> BpCategoryInfo(
                level = BpCategoryLevel.CRISIS,
                title = "Urgent: Crisis Level",
                subtitle = "Systolic >180 or Diastolic >120 mmHg",
                advice = "Very high reading! Contact Nani's doctor or emergency care immediately.",
                colorHex = 0xFFB71C1C,
                containerColorHex = 0xFFFFEBEE
            )
            systolic >= 140 || diastolic >= 90 -> BpCategoryInfo(
                level = BpCategoryLevel.STAGE_2,
                title = "Stage 2 High BP",
                subtitle = "Systolic ≥140 or Diastolic ≥90 mmHg",
                advice = "High blood pressure. Ensure Nani rests and check if prescribed meds were taken.",
                colorHex = 0xFFD32F2F,
                containerColorHex = 0xFFFDEDED
            )
            (systolic in 130..139) || (diastolic in 80..89) -> BpCategoryInfo(
                level = BpCategoryLevel.STAGE_1,
                title = "Stage 1 High BP",
                subtitle = "Systolic 130-139 or Diastolic 80-89 mmHg",
                advice = "Slightly high. Keep Nani calm, hydrate, and re-check after 15 minutes.",
                colorHex = 0xFFE65100,
                containerColorHex = 0xFFFFF3E0
            )
            (systolic in 120..129) && diastolic < 80 -> BpCategoryInfo(
                level = BpCategoryLevel.ELEVATED,
                title = "Elevated",
                subtitle = "Systolic 120-129 and Diastolic <80 mmHg",
                advice = "Slightly elevated. Good time for quiet rest and a glass of water.",
                colorHex = 0xFFF57C00,
                containerColorHex = 0xFFFFF8E1
            )
            systolic in 90..119 && diastolic in 60..79 -> BpCategoryInfo(
                level = BpCategoryLevel.NORMAL,
                title = "Normal & Healthy",
                subtitle = "Systolic <120 and Diastolic <80 mmHg",
                advice = "Wonderful! Nani's blood pressure is in the ideal healthy range.",
                colorHex = 0xFF2E7D32,
                containerColorHex = 0xFFE8F5E9
            )
            systolic < 90 || diastolic < 60 -> BpCategoryInfo(
                level = BpCategoryLevel.LOW,
                title = "Low Blood Pressure",
                subtitle = "Systolic <90 or Diastolic <60 mmHg",
                advice = "Blood pressure is low. Help Nani stand up slowly and offer fluids or a light snack.",
                colorHex = 0xFF0277BD,
                containerColorHex = 0xFFE1F5FE
            )
            else -> BpCategoryInfo(
                level = BpCategoryLevel.NORMAL,
                title = "Normal",
                subtitle = "Within acceptable range",
                advice = "Keep up healthy routines and hydration.",
                colorHex = 0xFF2E7D32,
                containerColorHex = 0xFFE8F5E9
            )
        }
    }
}
