package com.example.ui.state

import android.graphics.Bitmap
import com.example.data.content.AgriKnowledgeBase
import com.example.data.model.AniLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.DailyTip
import com.example.data.model.FarmBudget
import com.example.data.model.FarmPlot
import com.example.data.model.PhilippineRegion
import com.example.data.model.PlantDiagnosisResult
import com.example.data.model.PlantScan
import com.example.data.model.UserProfile
import com.example.data.model.WeatherForecast

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiTaskState<out T> {
    data object Idle : AiTaskState<Nothing>
    data object Loading : AiTaskState<Nothing>
    data class Success<T>(val data: T) : AiTaskState<T>
    data class Error(val message: String) : AiTaskState<Nothing>
}

data class BudgetDraft(
    val cropName: String = "Palay (Rice)",
    val farmAreaLabel: String = "1.0 Hectare",
    val seedsCost: String = "3600",
    val fertilizerCost: String = "9500",
    val pestControlCost: String = "2800",
    val laborCost: String = "14500",
    val otherCost: String = "3200",
    val expectedYieldKg: String = "4800",
    val pricePerKg: String = "21"
) {
    val seedsNum: Double get() = seedsCost.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val fertilizerNum: Double get() = fertilizerCost.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val pestNum: Double get() = pestControlCost.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val laborNum: Double get() = laborCost.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val otherNum: Double get() = otherCost.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val yieldNum: Double get() = expectedYieldKg.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val priceNum: Double get() = pricePerKg.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

    val totalExpense: Double get() = seedsNum + fertilizerNum + pestNum + laborNum + otherNum
    val grossIncome: Double get() = yieldNum * priceNum
    val netProfit: Double get() = grossIncome - totalExpense
    val roiPercent: Double get() = if (totalExpense > 0) (netProfit / totalExpense) * 100.0 else 0.0
    val breakEvenPrice: Double get() = if (yieldNum > 0) totalExpense / yieldNum else 0.0
}

data class PlantDoctorScreenState(
    val selectedBitmap: Bitmap? = null,
    val cropHint: String = "",
    val diagnosisState: AiTaskState<PlantDiagnosisResult> = AiTaskState.Idle
)

data class AgriQuestDashboardData(
    val selectedRegion: PhilippineRegion = AgriKnowledgeBase.philippineRegions.first(),
    val currentTipIndex: Int = 0,
    val tipRewardedIds: Set<String> = emptySet(),
    val aniLanguage: AniLanguage = AniLanguage.TAGLISH,
    val isAniDemoMode: Boolean = false
) {
    val currentTip: DailyTip
        get() = AgriKnowledgeBase.dailyTips[currentTipIndex % AgriKnowledgeBase.dailyTips.size]
}
