package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.content.AgriKnowledgeBase
import com.example.data.model.AniLanguage
import com.example.data.model.BudgetPreset
import com.example.data.model.ChallengeChoice
import com.example.data.model.ChatMessage
import com.example.data.model.FarmBudget
import com.example.data.model.FarmPlot
import com.example.data.model.PhilippineRegion
import com.example.data.model.PlantDiagnosisResult
import com.example.data.model.PlantScan
import com.example.data.model.SamplePlantCase
import com.example.data.model.UserProfile
import com.example.data.model.VirtualCropOption
import com.example.data.model.WeatherForecast
import com.example.data.remote.WeatherRepository
import com.example.data.repository.AgriAiRepository
import com.example.data.repository.AgriFirestoreRepository
import com.example.ui.state.AgriQuestDashboardData
import com.example.ui.state.AiTaskState
import com.example.ui.state.BudgetDraft
import com.example.ui.state.PlantDoctorScreenState
import com.example.ui.state.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "AgriQuestVM"
private const val STOP_TIMEOUT_MS = 5000L

class AgriQuestViewModel(
    private val firestoreRepo: AgriFirestoreRepository,
    private val aiRepo: AgriAiRepository,
    private val weatherRepo: WeatherRepository,
    private val currentUserId: String,
    private val userDisplayName: String?
) : ViewModel() {

    val profileState: StateFlow<UiState<UserProfile?>> =
        firestoreRepo.observeUserProfile(currentUserId)
            .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Profile stream error", root)
                emit(UiState.Error(root.localizedMessage ?: "Failed to load farmer profile."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val budgetsState: StateFlow<UiState<List<FarmBudget>>> =
        firestoreRepo.observeBudgets(currentUserId)
            .map<List<FarmBudget>, UiState<List<FarmBudget>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Budgets stream error", root)
                emit(UiState.Error(root.localizedMessage ?: "Failed to load saved budgets."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val farmPlotsState: StateFlow<UiState<List<FarmPlot>>> =
        firestoreRepo.observeFarmPlots(currentUserId)
            .map<List<FarmPlot>, UiState<List<FarmPlot>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Farm plots stream error", root)
                emit(UiState.Error(root.localizedMessage ?: "Failed to load virtual farm."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val plantScansState: StateFlow<UiState<List<PlantScan>>> =
        firestoreRepo.observePlantScans(currentUserId)
            .map<List<PlantScan>, UiState<List<PlantScan>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Plant scans stream error", root)
                emit(UiState.Error(root.localizedMessage ?: "Failed to load scan history."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    private val _dashboardData = MutableStateFlow(AgriQuestDashboardData())
    val dashboardData: StateFlow<AgriQuestDashboardData> = _dashboardData.asStateFlow()

    private val _weatherState = MutableStateFlow<UiState<WeatherForecast>>(UiState.Loading)
    val weatherState: StateFlow<UiState<WeatherForecast>> = _weatherState.asStateFlow()

    private val _plantDoctorState = MutableStateFlow(PlantDoctorScreenState())
    val plantDoctorState: StateFlow<PlantDoctorScreenState> = _plantDoctorState.asStateFlow()

    private val _aniMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "welcome_ani",
                isFromUser = false,
                text = "Kumusta, Kaibigan! I'm Ani, your smart farming assistant. Ask me anything about plant pests, soil health, weather prep, or lowering farm costs!"
            )
        )
    )
    val aniMessages: StateFlow<List<ChatMessage>> = _aniMessages.asStateFlow()

    private val _isAniStreaming = MutableStateFlow(false)
    val isAniStreaming: StateFlow<Boolean> = _isAniStreaming.asStateFlow()

    private val _budgetDraft = MutableStateFlow(BudgetDraft())
    val budgetDraft: StateFlow<BudgetDraft> = _budgetDraft.asStateFlow()

    private val _budgetAiTipState = MutableStateFlow<AiTaskState<String>>(AiTaskState.Idle)
    val budgetAiTipState: StateFlow<AiTaskState<String>> = _budgetAiTipState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var lastQuestionForRetry: String? = null

    init {
        viewModelScope.launch {
            val initResult = firestoreRepo.ensureInitialized(currentUserId, userDisplayName)
            initResult.onFailure {
                _snackbarMessage.value = "Could not sync cloud profile: ${it.localizedMessage}"
            }
        }
        refreshWeather()
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // --- Daily Farming Tips ---
    fun nextDailyTip() {
        _dashboardData.update { current ->
            current.copy(currentTipIndex = (current.currentTipIndex + 1) % AgriKnowledgeBase.dailyTips.size)
        }
    }

    fun markCurrentTipRead() {
        val tip = _dashboardData.value.currentTip
        if (_dashboardData.value.tipRewardedIds.contains(tip.id)) {
            _snackbarMessage.value = "You already collected XP for today's tip! Tap Next Tip for more."
            return
        }
        _dashboardData.update { it.copy(tipRewardedIds = it.tipRewardedIds + tip.id) }
        viewModelScope.launch {
            firestoreRepo.awardProgress(currentUserId, xpDelta = 10, coinDelta = 10)
                .onSuccess { _snackbarMessage.value = "+10 XP & +10 Coins for learning a Daily Farming Tip!" }
                .onFailure { _snackbarMessage.value = "Saved tip locally; cloud sync will retry." }
        }
    }

    // --- Weather Watch ---
    fun selectRegion(region: PhilippineRegion) {
        _dashboardData.update { it.copy(selectedRegion = region) }
        refreshWeather()
        viewModelScope.launch {
            firestoreRepo.updatePreferredRegion(currentUserId, region.name)
        }
    }

    fun refreshWeather() {
        val region = _dashboardData.value.selectedRegion
        viewModelScope.launch {
            _weatherState.value = UiState.Loading
            val forecast = weatherRepo.fetchWeatherForRegion(region)
            _weatherState.value = UiState.Success(forecast)
        }
    }

    // --- AI Plant Doctor ---
    fun setPlantPhoto(bitmap: Bitmap) {
        _plantDoctorState.update {
            it.copy(selectedBitmap = bitmap, diagnosisState = AiTaskState.Idle)
        }
    }

    fun updateCropHint(hint: String) {
        _plantDoctorState.update { it.copy(cropHint = hint) }
    }

    fun analyzeSelectedPlantPhoto() {
        val state = _plantDoctorState.value
        val bitmap = state.selectedBitmap ?: return
        viewModelScope.launch {
            _plantDoctorState.update { it.copy(diagnosisState = AiTaskState.Loading) }
            try {
                val result = aiRepo.analyzePlantPhoto(bitmap, state.cropHint)
                _plantDoctorState.update { it.copy(diagnosisState = AiTaskState.Success(result)) }
                firestoreRepo.savePlantScan(currentUserId, result)
                _snackbarMessage.value = "Diagnosis saved! +25 XP & +20 Coins earned."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Plant Doctor error", e)
                _plantDoctorState.update {
                    it.copy(
                        diagnosisState = AiTaskState.Error(
                            e.localizedMessage ?: "AI Plant Doctor could not analyze this photo right now."
                        )
                    )
                }
            }
        }
    }

    fun runSamplePlantCase(sampleCase: SamplePlantCase) {
        viewModelScope.launch {
            _plantDoctorState.update {
                it.copy(
                    cropHint = sampleCase.cropName,
                    diagnosisState = AiTaskState.Success(sampleCase.demoDiagnosis)
                )
            }
            firestoreRepo.savePlantScan(currentUserId, sampleCase.demoDiagnosis)
            _snackbarMessage.value = "Demo case loaded & logged (+25 XP)."
        }
    }

    // --- Ask Ani AI ---
    fun setAniLanguage(language: AniLanguage) {
        _dashboardData.update { it.copy(aniLanguage = language) }
    }

    fun toggleAniDemoMode(enabled: Boolean) {
        _dashboardData.update { it.copy(isAniDemoMode = enabled) }
    }

    fun sendAniQuestion(rawQuestion: String) {
        val question = rawQuestion.trim()
        if (question.isEmpty() || _isAniStreaming.value) return
        lastQuestionForRetry = question

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = question
        )
        val replyId = UUID.randomUUID().toString()
        val language = _dashboardData.value.aniLanguage

        if (_dashboardData.value.isAniDemoMode) {
            val demoReply = aiRepo.buildDemoAniReply(question, language)
            _aniMessages.update {
                it + userMsg + ChatMessage(
                    id = replyId,
                    isFromUser = false,
                    text = demoReply,
                    isDemoMode = true
                )
            }
            return
        }

        val priorHistory = _aniMessages.value
        _aniMessages.update {
            it + userMsg + ChatMessage(
                id = replyId,
                isFromUser = false,
                text = "",
                isStreaming = true
            )
        }
        _isAniStreaming.value = true

        viewModelScope.launch {
            try {
                aiRepo.streamAniReply(priorHistory, question, language).collect { partial ->
                    _aniMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == replyId) msg.copy(text = partial, isStreaming = true) else msg
                        }
                    }
                }
                _aniMessages.update { list ->
                    list.map { msg ->
                        if (msg.id == replyId) msg.copy(isStreaming = false) else msg
                    }
                }
                firestoreRepo.awardProgress(currentUserId, xpDelta = 5, coinDelta = 5)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Ask Ani streaming failed", e)
                val errText = e.localizedMessage ?: "Ani couldn't connect to Gemini right now."
                _aniMessages.update { list ->
                    list.map { msg ->
                        if (msg.id == replyId) {
                            msg.copy(
                                text = "Unable to reach Gemini AI ($errText). You can Retry or switch to Demo Mode below.",
                                isStreaming = false,
                                isError = true
                            )
                        } else msg
                    }
                }
            } finally {
                _isAniStreaming.value = false
            }
        }
    }

    fun useDemoForLastAniQuestion() {
        val question = lastQuestionForRetry ?: "How do I improve soil health?"
        val demoText = aiRepo.buildDemoAniReply(question, _dashboardData.value.aniLanguage)
        _dashboardData.update { it.copy(isAniDemoMode = true) }
        _aniMessages.update { list ->
            val cleaned = list.dropLastWhile { it.isError }
            cleaned + ChatMessage(
                id = UUID.randomUUID().toString(),
                isFromUser = false,
                text = demoText,
                isDemoMode = true
            )
        }
    }

    // --- Smart Farming Guide ---
    fun markGuideStudied(guideId: String) {
        viewModelScope.launch {
            val profile = (profileState.value as? UiState.Success)?.data
            if (profile != null && profile.studiedGuides.contains(guideId)) {
                _snackbarMessage.value = "You already earned XP for this guide!"
                return@launch
            }
            firestoreRepo.awardProgress(
                userId = currentUserId,
                xpDelta = 15,
                coinDelta = 10,
                newStudiedGuideId = guideId
            ).onSuccess {
                _snackbarMessage.value = "Guide completed! +15 XP & +10 Coins."
            }.onFailure {
                _snackbarMessage.value = "Failed to save guide progress."
            }
        }
    }

    // --- Farm Budget Challenge ---
    fun updateBudgetDraft(transform: (BudgetDraft) -> BudgetDraft) {
        _budgetDraft.update(transform)
    }

    fun applyBudgetPreset(preset: BudgetPreset) {
        _budgetDraft.value = BudgetDraft(
            cropName = preset.cropName,
            farmAreaLabel = preset.farmAreaLabel,
            seedsCost = preset.seedsCost.toInt().toString(),
            fertilizerCost = preset.fertilizerCost.toInt().toString(),
            pestControlCost = preset.pestControlCost.toInt().toString(),
            laborCost = preset.laborCost.toInt().toString(),
            otherCost = preset.otherCost.toInt().toString(),
            expectedYieldKg = preset.expectedYieldKg.toInt().toString(),
            pricePerKg = preset.pricePerKg.toString().removeSuffix(".0")
        )
        _budgetAiTipState.value = AiTaskState.Idle
    }

    fun saveCurrentBudget() {
        val draft = _budgetDraft.value
        if (draft.cropName.isBlank() || draft.farmAreaLabel.isBlank()) {
            _snackbarMessage.value = "Please enter a crop name and farm area."
            return
        }
        val budget = FarmBudget(
            userId = currentUserId,
            cropName = draft.cropName,
            farmAreaLabel = draft.farmAreaLabel,
            seedsCost = draft.seedsNum,
            fertilizerCost = draft.fertilizerNum,
            pestControlCost = draft.pestNum,
            laborCost = draft.laborNum,
            otherCost = draft.otherNum,
            expectedYieldKg = draft.yieldNum,
            pricePerKg = draft.priceNum,
            totalExpense = draft.totalExpense,
            grossIncome = draft.grossIncome,
            netProfit = draft.netProfit
        )
        viewModelScope.launch {
            firestoreRepo.saveBudget(currentUserId, budget)
                .onSuccess { _snackbarMessage.value = "Budget saved to cloud! +20 XP & Wise Budgeter badge unlocked." }
                .onFailure { _snackbarMessage.value = "Could not save budget: ${it.localizedMessage}" }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteBudget(currentUserId, budgetId)
                .onFailure { _snackbarMessage.value = "Could not delete budget: ${it.localizedMessage}" }
        }
    }

    fun generateBudgetAiTip() {
        val draft = _budgetDraft.value
        val tempBudget = FarmBudget(
            userId = currentUserId,
            cropName = draft.cropName.ifBlank { "Crop" },
            farmAreaLabel = draft.farmAreaLabel.ifBlank { "Farm" },
            seedsCost = draft.seedsNum,
            fertilizerCost = draft.fertilizerNum,
            pestControlCost = draft.pestNum,
            laborCost = draft.laborNum,
            otherCost = draft.otherNum,
            expectedYieldKg = draft.yieldNum,
            pricePerKg = draft.priceNum,
            totalExpense = draft.totalExpense,
            grossIncome = draft.grossIncome,
            netProfit = draft.netProfit
        )
        viewModelScope.launch {
            _budgetAiTipState.value = AiTaskState.Loading
            try {
                val tip = aiRepo.getBudgetOptimizationTip(tempBudget)
                _budgetAiTipState.value = AiTaskState.Success(tip)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Budget AI tip error", e)
                _budgetAiTipState.value = AiTaskState.Error(
                    e.localizedMessage ?: "Could not load AI cost tip right now."
                )
            }
        }
    }

    // --- Virtual Farm ---
    fun plantCrop(plot: FarmPlot, crop: VirtualCropOption) {
        val currentCoins = (profileState.value as? UiState.Success)?.data?.coins ?: 0
        if (currentCoins < crop.seedCost) {
            _snackbarMessage.value = "Need ${crop.seedCost} coins to plant ${crop.name}. Complete quizzes or tips to earn more!"
            return
        }
        viewModelScope.launch {
            firestoreRepo.updateFarmPlot(
                userId = currentUserId,
                plotId = plot.id.ifBlank { "plot_${plot.plotIndex}" },
                cropId = crop.cropId,
                cropName = crop.name,
                growthStage = 1,
                waterLevel = 80,
                soilHealth = (plot.soilHealth).coerceAtLeast(70),
                pestProtected = false,
                harvestCount = plot.harvestCount
            ).onSuccess {
                firestoreRepo.awardProgress(currentUserId, xpDelta = 10, coinDelta = -crop.seedCost)
                _snackbarMessage.value = "Planted ${crop.name}! Water and care for it to harvest."
            }.onFailure {
                _snackbarMessage.value = "Failed to plant crop: ${it.localizedMessage}"
            }
        }
    }

    fun waterPlot(plot: FarmPlot) {
        if (plot.cropId == "none") return
        val nextStage = (plot.growthStage + 1).coerceAtMost(3)
        viewModelScope.launch {
            firestoreRepo.updateFarmPlot(
                userId = currentUserId,
                plotId = plot.id.ifBlank { "plot_${plot.plotIndex}" },
                cropId = plot.cropId,
                cropName = plot.cropName,
                growthStage = nextStage,
                waterLevel = (plot.waterLevel + 30).coerceAtMost(100),
                soilHealth = plot.soilHealth,
                pestProtected = plot.pestProtected,
                harvestCount = plot.harvestCount
            ).onSuccess {
                firestoreRepo.awardProgress(currentUserId, xpDelta = 5, coinDelta = 0)
                _snackbarMessage.value = if (nextStage == 3) {
                    "${plot.cropName} is Ready to Harvest!"
                } else {
                    "Watered ${plot.cropName}! It grew stronger."
                }
            }
        }
    }

    fun fertilizePlot(plot: FarmPlot) {
        if (plot.cropId == "none") return
        val nextStage = (plot.growthStage + 1).coerceAtMost(3)
        viewModelScope.launch {
            firestoreRepo.updateFarmPlot(
                userId = currentUserId,
                plotId = plot.id.ifBlank { "plot_${plot.plotIndex}" },
                cropId = plot.cropId,
                cropName = plot.cropName,
                growthStage = nextStage,
                waterLevel = plot.waterLevel,
                soilHealth = (plot.soilHealth + 20).coerceAtMost(100),
                pestProtected = plot.pestProtected,
                harvestCount = plot.harvestCount
            ).onSuccess {
                firestoreRepo.awardProgress(currentUserId, xpDelta = 8, coinDelta = 0)
                _snackbarMessage.value = "Applied organic compost to ${plot.cropName}!"
            }
        }
    }

    fun protectPlot(plot: FarmPlot) {
        if (plot.cropId == "none") return
        viewModelScope.launch {
            firestoreRepo.updateFarmPlot(
                userId = currentUserId,
                plotId = plot.id.ifBlank { "plot_${plot.plotIndex}" },
                cropId = plot.cropId,
                cropName = plot.cropName,
                growthStage = plot.growthStage,
                waterLevel = plot.waterLevel,
                soilHealth = plot.soilHealth,
                pestProtected = true,
                harvestCount = plot.harvestCount
            ).onSuccess {
                firestoreRepo.awardProgress(currentUserId, xpDelta = 5, coinDelta = 0)
                _snackbarMessage.value = "${plot.cropName} is now protected with natural IPM!"
            }
        }
    }

    fun harvestPlot(plot: FarmPlot) {
        if (plot.cropId == "none" || plot.growthStage < 3) return
        val cropOption = AgriKnowledgeBase.virtualCrops.firstOrNull { it.cropId == plot.cropId }
        val bonus = if (plot.pestProtected && plot.soilHealth >= 80) 20 else 0
        val coinsEarned = (cropOption?.harvestCoins ?: 80) + bonus
        val xpEarned = cropOption?.xpReward ?: 30

        viewModelScope.launch {
            firestoreRepo.updateFarmPlot(
                userId = currentUserId,
                plotId = plot.id.ifBlank { "plot_${plot.plotIndex}" },
                cropId = "none",
                cropName = "Empty Plot",
                growthStage = 0,
                waterLevel = 60,
                soilHealth = (plot.soilHealth - 10).coerceAtLeast(50),
                pestProtected = false,
                harvestCount = plot.harvestCount + 1
            ).onSuccess {
                firestoreRepo.awardProgress(
                    userId = currentUserId,
                    xpDelta = xpEarned,
                    coinDelta = coinsEarned,
                    newBadgeId = "harvest_master"
                )
                _snackbarMessage.value = "Harvested ${plot.cropName}! +$coinsEarned Coins & +$xpEarned XP!"
            }
        }
    }

    // --- Challenges & Quizzes ---
    fun submitChallengeChoice(challengeId: String, choice: ChallengeChoice) {
        viewModelScope.launch {
            firestoreRepo.awardProgress(
                userId = currentUserId,
                xpDelta = choice.xpReward,
                coinDelta = choice.coinDelta,
                newCompletedChallengeId = challengeId
            ).onSuccess {
                val coinStr = if (choice.coinDelta >= 0) "+${choice.coinDelta}" else "${choice.coinDelta}"
                _snackbarMessage.value = "Challenge recorded: +${choice.xpReward} XP, $coinStr Coins"
            }
        }
    }

    fun submitQuizAnswer(questionId: String, isCorrect: Boolean, xpReward: Int) {
        if (!isCorrect) return
        viewModelScope.launch {
            firestoreRepo.awardProgress(
                userId = currentUserId,
                xpDelta = xpReward,
                coinDelta = 20,
                newCompletedChallengeId = questionId
            ).onSuccess {
                _snackbarMessage.value = "Correct! +$xpReward XP & +20 Coins!"
            }
        }
    }
}
