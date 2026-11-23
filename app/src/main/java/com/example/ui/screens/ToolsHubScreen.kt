package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.content.AgriKnowledgeBase
import com.example.data.model.AlertLevel
import com.example.data.model.BudgetPreset
import com.example.data.model.CropGuideTopic
import com.example.data.model.FarmBudget
import com.example.data.model.PhilippineRegion
import com.example.data.model.UserProfile
import com.example.data.model.WeatherForecast
import com.example.ui.state.AiTaskState
import com.example.ui.state.BudgetDraft
import com.example.ui.state.UiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    selectedRegion: PhilippineRegion,
    weatherState: UiState<WeatherForecast>,
    onSelectRegion: (PhilippineRegion) -> Unit,
    onRefreshWeather: () -> Unit,
    profileState: UiState<UserProfile?>,
    onMarkGuideStudied: (String) -> Unit,
    onAskAniAboutCrop: (String) -> Unit,
    budgetDraft: BudgetDraft,
    budgetsState: UiState<List<FarmBudget>>,
    budgetAiTipState: AiTaskState<String>,
    onUpdateBudgetDraft: ((BudgetDraft) -> BudgetDraft) -> Unit,
    onApplyPreset: (BudgetPreset) -> Unit,
    onSaveBudget: () -> Unit,
    onDeleteBudget: (String) -> Unit,
    onGenerateBudgetAiTip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Weather Watch", "Smart Guide", "Budget (₱)")
    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onSelectTab(index) },
                    text = { Text(title) },
                    modifier = Modifier.testTag("tools_tab_$index")
                )
            }
        }
        when (selectedTab) {
            0 -> WeatherWatchTab(
                selectedRegion = selectedRegion,
                weatherState = weatherState,
                onSelectRegion = onSelectRegion,
                onRefreshWeather = onRefreshWeather
            )
            1 -> SmartGuideTab(
                studiedGuides = (profileState as? UiState.Success)?.data?.studiedGuides ?: emptyList(),
                onMarkGuideStudied = onMarkGuideStudied,
                onAskAniAboutCrop = onAskAniAboutCrop
            )
            else -> FarmBudgetTab(
                draft = budgetDraft,
                budgetsState = budgetsState,
                aiTipState = budgetAiTipState,
                onUpdateDraft = onUpdateBudgetDraft,
                onApplyPreset = onApplyPreset,
                onSaveBudget = onSaveBudget,
                onDeleteBudget = onDeleteBudget,
                onGenerateAiTip = onGenerateBudgetAiTip
            )
        }
    }
}

@Composable
private fun WeatherWatchTab(
    selectedRegion: PhilippineRegion,
    weatherState: UiState<WeatherForecast>,
    onSelectRegion: (PhilippineRegion) -> Unit,
    onRefreshWeather: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Philippine Farming Regions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = onRefreshWeather,
                        modifier = Modifier.testTag("refresh_weather_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh weather forecast")
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AgriKnowledgeBase.philippineRegions) { region ->
                        FilterChip(
                            selected = region.name == selectedRegion.name,
                            onClick = { onSelectRegion(region) },
                            label = { Text(region.name) },
                            modifier = Modifier.testTag("region_chip_${region.name}")
                        )
                    }
                }
            }
        }

        item {
            when (weatherState) {
                is UiState.Loading -> {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Text("Fetching live Open-Meteo weather for ${selectedRegion.name}...")
                        }
                    }
                }
                is UiState.Error -> {
                    Text(weatherState.message, color = MaterialTheme.colorScheme.error)
                }
                is UiState.Success -> {
                    val forecast = weatherState.data
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        if (forecast.isDemoFallback) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Demo Weather Fallback: Live weather service was unreachable; showing typical seasonal estimates for ${forecast.regionName}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = forecast.regionName,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${forecast.province} • Main: ${selectedRegion.mainCrops}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                    Text(
                                        text = "${forecast.currentTempC.toInt()}°C",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    WeatherStatPill("Humidity", "${forecast.humidityPct}%", Icons.Rounded.WaterDrop, Modifier.weight(1f))
                                    WeatherStatPill("Rain Now", "${forecast.rainMm} mm", Icons.Rounded.Cloud, Modifier.weight(1f))
                                    WeatherStatPill("Wind", "${forecast.windKmh.toInt()} km/h", Icons.Rounded.Thermostat, Modifier.weight(1f))
                                }
                            }
                        }

                        val alertBg = when (forecast.alertLevel) {
                            AlertLevel.WARNING -> MaterialTheme.colorScheme.errorContainer
                            AlertLevel.CAUTION -> MaterialTheme.colorScheme.secondaryContainer
                            AlertLevel.GOOD -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val alertFg = when (forecast.alertLevel) {
                            AlertLevel.WARNING -> MaterialTheme.colorScheme.onErrorContainer
                            AlertLevel.CAUTION -> MaterialTheme.colorScheme.onSecondaryContainer
                            AlertLevel.GOOD -> MaterialTheme.colorScheme.onSurface
                        }

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = alertBg)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = forecast.farmingAlertTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = alertFg
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = forecast.farmingAlertMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = alertFg
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                forecast.cropProtectionTips.forEach { tip ->
                                    Text(
                                        text = "• $tip",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = alertFg,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "5-Day Farm Outlook",
                            style = MaterialTheme.typography.titleMedium
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(forecast.dailyDays) { day ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.width(116.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(day.dateLabel, style = MaterialTheme.typography.labelLarge)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Icon(
                                            imageVector = if (day.rainMm >= 5.0) Icons.Rounded.Cloud else Icons.Rounded.WbSunny,
                                            contentDescription = day.dateLabel,
                                            tint = if (day.rainMm >= 5.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${day.maxTempC.toInt()}° / ${day.minTempC.toInt()}°",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Rain ${day.rainMm} mm",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherStatPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SmartGuideTab(
    studiedGuides: List<String>,
    onMarkGuideStudied: (String) -> Unit,
    onAskAniAboutCrop: (String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    val categories = listOf("All", "Staple Crop", "High-Value Vegetable", "Soil Management", "Pest Control")

    val filtered = remember(searchQuery, selectedCategory) {
        AgriKnowledgeBase.cropGuides.filter { topic ->
            val matchCat = selectedCategory == "All" || topic.category == selectedCategory
            val q = searchQuery.trim().lowercase()
            val matchSearch = q.isEmpty() || topic.title.lowercase().contains(q) || topic.subtitle.lowercase().contains(q)
            matchCat && matchSearch
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search guide") },
                    placeholder = { Text("Search crops, soil compost, pests...") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guide_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { topic ->
            GuideTopicCard(
                topic = topic,
                isStudied = studiedGuides.contains(topic.id),
                onMarkStudied = { onMarkGuideStudied(topic.id) },
                onAskAni = { onAskAniAboutCrop("Give me practical tips for ${topic.title} in the Philippines.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            )
        }
    }
}

@Composable
private fun GuideTopicCard(
    topic: CropGuideTopic,
    isStudied: Boolean,
    onMarkStudied: () -> Unit,
    onAskAni: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = topic.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.testTag("expand_guide_${topic.id}")
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Collapse guide" else "Expand guide"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text("Season: ${topic.idealSeason}", style = MaterialTheme.typography.bodySmall)
                    Text("Soil: ${topic.soilPreference}", style = MaterialTheme.typography.bodySmall)
                    Text("Water: ${topic.waterNeeds}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Planting Steps", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    topic.plantingSteps.forEachIndexed { idx, step ->
                        Text("${idx + 1}. $step", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Crop Care & Soil Management", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    topic.careAndSoilTips.forEach { tip ->
                        Text("• $tip", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pest Watch: ${topic.pestWatch}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Money Saver: ${topic.budgetTip}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = onMarkStudied,
                            enabled = !isStudied,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("mark_studied_${topic.id}")
                        ) {
                            if (isStudied) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(if (isStudied) "Studied (+15 XP)" else "Complete (+15 XP)")
                        }
                        OutlinedButton(
                            onClick = onAskAni,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Ask Ani AI")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FarmBudgetTab(
    draft: BudgetDraft,
    budgetsState: UiState<List<FarmBudget>>,
    aiTipState: AiTaskState<String>,
    onUpdateDraft: ((BudgetDraft) -> BudgetDraft) -> Unit,
    onApplyPreset: (BudgetPreset) -> Unit,
    onSaveBudget: () -> Unit,
    onDeleteBudget: (String) -> Unit,
    onGenerateAiTip: () -> Unit
) {
    val phpFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "PH")).apply {
            maximumFractionDigits = 0
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // Presets
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Text("Farm Budget Challenge (₱ PHP)", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Calculate expenses, harvest income, break-even price, and net profit before planting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AgriKnowledgeBase.budgetPresets) { preset ->
                        AssistChip(
                            onClick = { onApplyPreset(preset) },
                            label = { Text(preset.label) },
                            modifier = Modifier.testTag("budget_preset_${preset.cropName}")
                        )
                    }
                }
            }
        }

        // Live Summary Card
        item {
            val isProfit = draft.netProfit >= 0
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProfit) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isProfit) "Projected Net Profit" else "Projected Net Loss",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = phpFormatter.format(draft.netProfit),
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Total Expenses", style = MaterialTheme.typography.labelSmall)
                            Text(phpFormatter.format(draft.totalExpense), style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text("Gross Income", style = MaterialTheme.typography.labelSmall)
                            Text(phpFormatter.format(draft.grossIncome), style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text("Break-Even", style = MaterialTheme.typography.labelSmall)
                            Text("₱${String.format(Locale.US, "%.2f", draft.breakEvenPrice)}/kg", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Estimated Return on Investment (ROI): ${String.format(Locale.US, "%.1f", draft.roiPercent)}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Input Form
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = draft.cropName,
                            onValueChange = { v -> onUpdateDraft { it.copy(cropName = v) } },
                            label = { Text("Crop") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = draft.farmAreaLabel,
                            onValueChange = { v -> onUpdateDraft { it.copy(farmAreaLabel = v) } },
                            label = { Text("Farm Area") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PesoField("Seeds (₱)", draft.seedsCost, { v -> onUpdateDraft { it.copy(seedsCost = v) } }, Modifier.weight(1f))
                        PesoField("Fertilizer (₱)", draft.fertilizerCost, { v -> onUpdateDraft { it.copy(fertilizerCost = v) } }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PesoField("Pest Care (₱)", draft.pestControlCost, { v -> onUpdateDraft { it.copy(pestControlCost = v) } }, Modifier.weight(1f))
                        PesoField("Labor (₱)", draft.laborCost, { v -> onUpdateDraft { it.copy(laborCost = v) } }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PesoField("Water/Other (₱)", draft.otherCost, { v -> onUpdateDraft { it.copy(otherCost = v) } }, Modifier.weight(1f))
                        PesoField("Yield (kg)", draft.expectedYieldKg, { v -> onUpdateDraft { it.copy(expectedYieldKg = v) } }, Modifier.weight(1f))
                    }
                    PesoField("Expected Farmgate Price (₱ per kg)", draft.pricePerKg, { v -> onUpdateDraft { it.copy(pricePerKg = v) } }, Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onSaveBudget,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("save_budget_button")
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Budget")
                        }
                        FilledTonalButton(
                            onClick = onGenerateAiTip,
                            enabled = aiTipState !is AiTaskState.Loading,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("budget_ai_tip_button")
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Cost Tip")
                        }
                    }

                    when (aiTipState) {
                        is AiTaskState.Idle -> Unit
                        is AiTaskState.Loading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing Philippine peso costs with Gemini AI...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is AiTaskState.Error -> {
                            Text(aiTipState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        is AiTaskState.Success -> {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = aiTipState.data,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Saved Budgets List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Text("Saved Farm Budgets", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                when (budgetsState) {
                    is UiState.Loading -> Text("Loading saved budgets...")
                    is UiState.Error -> Text(budgetsState.message, color = MaterialTheme.colorScheme.error)
                    is UiState.Success -> {
                        if (budgetsState.data.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "No saved budgets yet. Save your first budget calculation to earn the Wise Budgeter (₱) badge!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                budgetsState.data.forEach { budget ->
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${budget.cropName} (${budget.farmAreaLabel})", style = MaterialTheme.typography.titleMedium)
                                                Text(
                                                    "Expense: ${phpFormatter.format(budget.totalExpense)} • Profit: ${phpFormatter.format(budget.netProfit)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (budget.netProfit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteBudget(budget.id) },
                                                modifier = Modifier.testTag("delete_budget_${budget.id}")
                                            ) {
                                                Icon(Icons.Rounded.Delete, contentDescription = "Delete saved budget")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PesoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { text ->
            val filtered = text.filter { it.isDigit() || it == '.' }
            onValueChange(filtered)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}
