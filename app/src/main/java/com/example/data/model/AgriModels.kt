package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Kaibigan Farmer",
    val xp: Int = 0,
    val coins: Int = 150,
    val streakDays: Int = 1,
    val preferredRegion: String = "Nueva Ecija",
    val completedChallenges: List<String> = emptyList(),
    val earnedBadges: List<String> = listOf("seedling"),
    val studiedGuides: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FarmBudget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val cropName: String = "",
    val farmAreaLabel: String = "",
    val seedsCost: Double = 0.0,
    val fertilizerCost: Double = 0.0,
    val pestControlCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val otherCost: Double = 0.0,
    val expectedYieldKg: Double = 0.0,
    val pricePerKg: Double = 0.0,
    val totalExpense: Double = 0.0,
    val grossIncome: Double = 0.0,
    val netProfit: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FarmPlot(
    @DocumentId val id: String = "",
    val userId: String = "",
    val plotIndex: Int = 0,
    val cropId: String = "none",
    val cropName: String = "Empty Plot",
    val growthStage: Int = 0,
    val waterLevel: Int = 60,
    val soilHealth: Int = 80,
    val pestProtected: Boolean = false,
    val harvestCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PlantScan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val cropName: String = "",
    val conditionName: String = "",
    val severity: String = "Low",
    val summary: String = "",
    val organicSolutions: List<String> = emptyList(),
    val preventionTips: List<String> = emptyList(),
    val demoMode: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class PhilippineRegion(
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double,
    val mainCrops: String
)

data class DailyWeatherDay(
    val dateLabel: String,
    val maxTempC: Double,
    val minTempC: Double,
    val rainMm: Double,
    val weatherCode: Int
)

data class WeatherForecast(
    val regionName: String,
    val province: String,
    val currentTempC: Double,
    val humidityPct: Int,
    val windKmh: Double,
    val rainMm: Double,
    val weatherCode: Int,
    val conditionLabel: String,
    val farmingAlertTitle: String,
    val farmingAlertMessage: String,
    val alertLevel: AlertLevel,
    val cropProtectionTips: List<String>,
    val dailyDays: List<DailyWeatherDay>,
    val isDemoFallback: Boolean = false
)

enum class AlertLevel { GOOD, CAUTION, WARNING }

data class CropGuideTopic(
    val id: String,
    val title: String,
    val category: String,
    val subtitle: String,
    val idealSeason: String,
    val soilPreference: String,
    val waterNeeds: String,
    val plantingSteps: List<String>,
    val careAndSoilTips: List<String>,
    val pestWatch: String,
    val budgetTip: String
)

data class DailyTip(
    val id: String,
    val category: String,
    val title: String,
    val tip: String,
    val funFact: String
)

data class ChallengeChoice(
    val id: String,
    val label: String,
    val outcome: String,
    val xpReward: Int,
    val coinDelta: Int,
    val isRecommended: Boolean
)

data class AgriChallenge(
    val id: String,
    val title: String,
    val regionContext: String,
    val scenario: String,
    val choices: List<ChallengeChoice>
)

data class AgriQuizQuestion(
    val id: String,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val xpReward: Int
)

data class BadgeInfo(
    val id: String,
    val title: String,
    val description: String,
    val unlockHint: String
)

data class VirtualCropOption(
    val cropId: String,
    val name: String,
    val seedCost: Int,
    val harvestCoins: Int,
    val xpReward: Int,
    val careFact: String
)

data class BudgetPreset(
    val label: String,
    val cropName: String,
    val farmAreaLabel: String,
    val seedsCost: Double,
    val fertilizerCost: Double,
    val pestControlCost: Double,
    val laborCost: Double,
    val otherCost: Double,
    val expectedYieldKg: Double,
    val pricePerKg: Double
)

data class PlantDiagnosisResult(
    val cropName: String,
    val conditionName: String,
    val severity: String,
    val summary: String,
    val organicSolutions: List<String>,
    val preventionTips: List<String>,
    val isDemoMode: Boolean = false
)

data class SamplePlantCase(
    val id: String,
    val label: String,
    val cropName: String,
    val symptomPreview: String,
    val demoDiagnosis: PlantDiagnosisResult
)

enum class AniLanguage(val label: String, val instruction: String) {
    SIMPLE_ENGLISH("Simple English", "Answer in very clear, friendly, simple English suitable for Filipino farmers and students."),
    TAGLISH("Taglish", "Answer in warm, natural Taglish (clear mix of Tagalog and simple English) used by Filipino farmers and students."),
    FILIPINO("Filipino", "Answer in clear, everyday Filipino/Tagalog that is easy for rural farmers and students to understand.")
}

data class ChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val isDemoMode: Boolean = false
)
