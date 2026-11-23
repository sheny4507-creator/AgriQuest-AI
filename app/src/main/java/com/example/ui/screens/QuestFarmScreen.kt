package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.content.AgriKnowledgeBase
import com.example.data.model.AgriChallenge
import com.example.data.model.AgriQuizQuestion
import com.example.data.model.ChallengeChoice
import com.example.data.model.FarmPlot
import com.example.data.model.UserProfile
import com.example.data.model.VirtualCropOption
import com.example.ui.state.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestFarmScreen(
    profileState: UiState<UserProfile?>,
    farmPlotsState: UiState<List<FarmPlot>>,
    onPlantCrop: (FarmPlot, VirtualCropOption) -> Unit,
    onWaterPlot: (FarmPlot) -> Unit,
    onFertilizePlot: (FarmPlot) -> Unit,
    onProtectPlot: (FarmPlot) -> Unit,
    onHarvestPlot: (FarmPlot) -> Unit,
    onSubmitChallengeChoice: (String, ChallengeChoice) -> Unit,
    onSubmitQuizAnswer: (String, Boolean, Int) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by rememberSaveable { mutableStateOf(0) }
    val subTabs = listOf("Virtual Farm", "Challenges & Quiz", "Badges")
    val profile = (profileState as? UiState.Success)?.data

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedSubTab) {
            subTabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedSubTab == idx,
                    onClick = { selectedSubTab = idx },
                    text = { Text(title) },
                    modifier = Modifier.testTag("quest_tab_$idx")
                )
            }
        }
        when (selectedSubTab) {
            0 -> VirtualFarmTab(
                profile = profile,
                farmPlotsState = farmPlotsState,
                onPlantCrop = onPlantCrop,
                onWaterPlot = onWaterPlot,
                onFertilizePlot = onFertilizePlot,
                onProtectPlot = onProtectPlot,
                onHarvestPlot = onHarvestPlot
            )
            1 -> ChallengesAndQuizTab(
                completedIds = profile?.completedChallenges ?: emptyList(),
                onSubmitChallengeChoice = onSubmitChallengeChoice,
                onSubmitQuizAnswer = onSubmitQuizAnswer
            )
            else -> BadgesAndAccountTab(
                profile = profile,
                onSignOut = onSignOut
            )
        }
    }
}

@Composable
private fun VirtualFarmTab(
    profile: UserProfile?,
    farmPlotsState: UiState<List<FarmPlot>>,
    onPlantCrop: (FarmPlot, VirtualCropOption) -> Unit,
    onWaterPlot: (FarmPlot) -> Unit,
    onFertilizePlot: (FarmPlot) -> Unit,
    onProtectPlot: (FarmPlot) -> Unit,
    onHarvestPlot: (FarmPlot) -> Unit
) {
    var selectedCrop by remember { mutableStateOf(AgriKnowledgeBase.virtualCrops.first()) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Virtual Smart Farm", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Plant crops, manage water & soil health, protect from pests, and harvest for coins!",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.MonetizationOn, contentDescription = "Coins", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${profile?.coins ?: 150}", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Seed to Plant:", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AgriKnowledgeBase.virtualCrops) { crop ->
                            val isSelected = crop.cropId == selectedCrop.cropId
                            Surface(
                                onClick = { selectedCrop = crop },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("select_seed_${crop.cropId}")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = crop.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Cost ${crop.seedCost} • Harvest +${crop.harvestCoins}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tip: ${selectedCrop.careFact}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            when (farmPlotsState) {
                is UiState.Loading -> Text("Preparing your virtual farm plots...")
                is UiState.Error -> Text(farmPlotsState.message, color = MaterialTheme.colorScheme.error)
                is UiState.Success -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        farmPlotsState.data.forEach { plot ->
                            VirtualPlotCard(
                                plot = plot,
                                selectedSeed = selectedCrop,
                                onPlant = { onPlantCrop(plot, selectedCrop) },
                                onWater = { onWaterPlot(plot) },
                                onFertilize = { onFertilizePlot(plot) },
                                onProtect = { onProtectPlot(plot) },
                                onHarvest = { onHarvestPlot(plot) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualPlotCard(
    plot: FarmPlot,
    selectedSeed: VirtualCropOption,
    onPlant: () -> Unit,
    onWater: () -> Unit,
    onFertilize: () -> Unit,
    onProtect: () -> Unit,
    onHarvest: () -> Unit
) {
    val isEmpty = plot.cropId == "none"
    val isReady = !isEmpty && plot.growthStage >= 3
    val stageLabel = when {
        isEmpty -> "Empty Soil Plot"
        plot.growthStage == 1 -> "Seedling Stage"
        plot.growthStage == 2 -> "Growing Strong"
        else -> "Ready to Harvest!"
    }
    val animatedGrowth by animateFloatAsState(
        targetValue = if (isEmpty) 0f else plot.growthStage / 3f,
        animationSpec = spring(),
        label = "plot_growth"
    )
    val cardBg by animateColorAsState(
        targetValue = when {
            isReady -> MaterialTheme.colorScheme.secondaryContainer
            isEmpty -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        label = "plot_bg"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("virtual_plot_${plot.plotIndex}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    isReady -> Icons.Rounded.Agriculture
                                    isEmpty -> Icons.Rounded.Spa
                                    else -> Icons.Rounded.Eco
                                },
                                contentDescription = stageLabel,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Plot #${plot.plotIndex + 1}: ${plot.cropName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$stageLabel • Harvested ${plot.harvestCount}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (plot.pestProtected) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Text(
                            text = "IPM Protected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { animatedGrowth },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Water ${plot.waterLevel}% • Soil Health ${plot.soilHealth}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            if (isEmpty) {
                Button(
                    onClick = onPlant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("plant_plot_${plot.plotIndex}")
                ) {
                    Text("Plant ${selectedSeed.name} (${selectedSeed.seedCost} Coins)")
                }
            } else if (isReady) {
                Button(
                    onClick = onHarvest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("harvest_plot_${plot.plotIndex}")
                ) {
                    Icon(Icons.Rounded.Agriculture, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Harvest Crop for Coins & XP!")
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = onWater,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("water_plot_${plot.plotIndex}")
                    ) {
                        Icon(Icons.Rounded.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Water")
                    }
                    FilledTonalButton(
                        onClick = onFertilize,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("fertilize_plot_${plot.plotIndex}")
                    ) {
                        Icon(Icons.Rounded.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compost")
                    }
                    OutlinedButton(
                        onClick = onProtect,
                        enabled = !plot.pestProtected,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("protect_plot_${plot.plotIndex}")
                    ) {
                        Icon(Icons.Rounded.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("IPM")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengesAndQuizTab(
    completedIds: List<String>,
    onSubmitChallengeChoice: (String, ChallengeChoice) -> Unit,
    onSubmitQuizAnswer: (String, Boolean, Int) -> Unit
) {
    val challengeFeedback = remember { mutableStateMapOf<String, ChallengeChoice>() }
    val quizSelected = remember { mutableStateMapOf<String, Int>() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "AgriQuest Farm Decision Challenges",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            )
        }

        items(AgriKnowledgeBase.agriChallenges, key = { it.id }) { challenge ->
            ChallengeCard(
                challenge = challenge,
                isCompleted = completedIds.contains(challenge.id),
                selectedChoice = challengeFeedback[challenge.id],
                onChoose = { choice ->
                    challengeFeedback[challenge.id] = choice
                    onSubmitChallengeChoice(challenge.id, choice)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Smart Farming Knowledge Quiz",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            )
        }

        items(AgriKnowledgeBase.quizQuestions, key = { it.id }) { question ->
            QuizCard(
                question = question,
                isCompleted = completedIds.contains(question.id),
                selectedOption = quizSelected[question.id],
                onSelectOption = { idx ->
                    quizSelected[question.id] = idx
                    onSubmitQuizAnswer(question.id, idx == question.correctIndex, question.xpReward)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            )
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: AgriChallenge,
    isCompleted: Boolean,
    selectedChoice: ChallengeChoice?,
    onChoose: (ChallengeChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = challenge.regionContext,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isCompleted) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = "Completed", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(challenge.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(challenge.scenario, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(10.dp))

            challenge.choices.forEach { choice ->
                OutlinedButton(
                    onClick = { onChoose(choice) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .heightIn(min = 48.dp)
                        .testTag("challenge_${challenge.id}_${choice.id}")
                ) {
                    Text(
                        text = choice.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (selectedChoice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (selectedChoice.isRecommended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedChoice.outcome,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizCard(
    question: AgriQuizQuestion,
    isCompleted: Boolean,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(question.topic, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
                if (isCompleted) {
                    Text("Solved (+${question.xpReward} XP)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(question.question, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            question.options.forEachIndexed { idx, opt ->
                FilledTonalButton(
                    onClick = { onSelectOption(idx) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .heightIn(min = 48.dp)
                        .testTag("quiz_${question.id}_opt_$idx")
                ) {
                    Text(opt, modifier = Modifier.fillMaxWidth())
                }
            }
            if (selectedOption != null) {
                val isCorrect = selectedOption == question.correctIndex
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isCorrect) "Correct! ${question.explanation}" else "Not quite — ${question.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgesAndAccountTab(
    profile: UserProfile?,
    onSignOut: () -> Unit
) {
    val unlocked = profile?.earnedBadges ?: listOf("seedling")
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile?.displayName ?: "Kaibigan Farmer", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${profile?.xp ?: 0} XP • ${profile?.coins ?: 150} Coins • ${unlocked.size} Badges",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Sign out", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out")
                    }
                }
            }
        }

        items(AgriKnowledgeBase.badges, key = { it.id }) { badge ->
            val isUnlocked = unlocked.contains(badge.id)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isUnlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Rounded.EmojiEvents else Icons.Rounded.Shield,
                                contentDescription = badge.title,
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = badge.title + if (isUnlocked) " (Unlocked)" else " (Locked)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(badge.description, style = MaterialTheme.typography.bodySmall)
                        if (!isUnlocked) {
                            Text(
                                text = "How to unlock: ${badge.unlockHint}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
