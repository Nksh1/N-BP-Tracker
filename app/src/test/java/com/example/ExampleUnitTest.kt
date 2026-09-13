package com.example

import com.example.data.BloodPressureClassifier
import com.example.data.BpCategoryLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testNormalBloodPressure() {
    val result = BloodPressureClassifier.classify(115, 75)
    assertEquals(BpCategoryLevel.NORMAL, result.level)
  }

  @Test
  fun testElevatedBloodPressure() {
    val result = BloodPressureClassifier.classify(125, 75)
    assertEquals(BpCategoryLevel.ELEVATED, result.level)
  }

  @Test
  fun testStage1Hypertension() {
    val result = BloodPressureClassifier.classify(135, 85)
    assertEquals(BpCategoryLevel.STAGE_1, result.level)
  }

  @Test
  fun testStage2Hypertension() {
    val result = BloodPressureClassifier.classify(145, 95)
    assertEquals(BpCategoryLevel.STAGE_2, result.level)
  }

  @Test
  fun testHypertensiveCrisis() {
    val result = BloodPressureClassifier.classify(190, 125)
    assertEquals(BpCategoryLevel.CRISIS, result.level)
  }

  @Test
  fun testLowBloodPressure() {
    val result = BloodPressureClassifier.classify(85, 55)
    assertEquals(BpCategoryLevel.LOW, result.level)
  }
}
