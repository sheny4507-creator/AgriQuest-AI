package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.auth.attemptAutoSignIn
import com.example.data.auth.signOutFromGoogle
import com.example.data.firebase.configureAppCheck
import com.example.data.remote.WeatherRepository
import com.example.data.repository.AgriAiRepository
import com.example.data.repository.AgriFirestoreRepository
import com.example.ui.screens.AskAniScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.PlantDoctorScreen
import com.example.ui.screens.QuestFarmScreen
import com.example.ui.screens.ToolsHubScreen
import com.example.ui.theme.AgriQuestTheme
import com.example.ui.viewmodel.AgriQuestViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class AgriTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    DOCTOR("Doctor", Icons.Rounded.LocalHospital, Icons.Outlined.LocalHospital, "nav_doctor"),
    ANI("Ask Ani", Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_ani"),
    TOOLS("Tools", Icons.Rounded.Build, Icons.Outlined.Build, "nav_tools"),
    QUEST("Farm", Icons.Rounded.Agriculture, Icons.Outlined.Agriculture, "nav_quest")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory Step 1: Configure App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            AgriQuestTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AgriQuestAppRoot()
                }
            }
        }
    }
}

@Composable
private fun AgriQuestAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onUnauthenticated = { /* Interactive AuthScreen handles sign-in */ },
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        val uid = user.uid
        key(uid) {
            AuthenticatedAgriQuestShell(
                currentUserId = uid,
                displayName = user.displayName,
                onSignOut = {
                    signOutFromGoogle(
                        credentialManager = credentialManager,
                        scope = scope,
                        onSignOutComplete = { currentUser = null }
                    )
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedAgriQuestShell(
    currentUserId: String,
    displayName: String?,
    onSignOut: () -> Unit
) {
    val viewModel: AgriQuestViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                AgriQuestViewModel(
                    firestoreRepo = AgriFirestoreRepository(db),
                    aiRepo = AgriAiRepository(),
                    weatherRepo = WeatherRepository(),
                    currentUserId = currentUserId,
                    userDisplayName = displayName
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val budgetsState by viewModel.budgetsState.collectAsStateWithLifecycle()
    val farmPlotsState by viewModel.farmPlotsState.collectAsStateWithLifecycle()
    val plantScansState by viewModel.plantScansState.collectAsStateWithLifecycle()
    val dashboardData by viewModel.dashboardData.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val plantDoctorState by viewModel.plantDoctorState.collectAsStateWithLifecycle()
    val aniMessages by viewModel.aniMessages.collectAsStateWithLifecycle()
    val isAniStreaming by viewModel.isAniStreaming.collectAsStateWithLifecycle()
    val budgetDraft by viewModel.budgetDraft.collectAsStateWithLifecycle()
    val budgetAiTipState by viewModel.budgetAiTipState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(AgriTab.HOME) }
    var toolsSubTab by rememberSaveable { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    if (currentTab != AgriTab.HOME) {
        BackHandler {
            currentTab = AgriTab.HOME
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        AgriTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail {
                        AgriTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                when (currentTab) {
                    AgriTab.HOME -> HomeDashboardScreen(
                        profileState = profileState,
                        dashboardData = dashboardData,
                        weatherState = weatherState,
                        onNavigateToPlantDoctor = { currentTab = AgriTab.DOCTOR },
                        onNavigateToAskAni = { currentTab = AgriTab.ANI },
                        onNavigateToToolsTab = { sub ->
                            toolsSubTab = sub
                            currentTab = AgriTab.TOOLS
                        },
                        onNavigateToQuest = { currentTab = AgriTab.QUEST },
                        onNextTip = viewModel::nextDailyTip,
                        onMarkTipRead = viewModel::markCurrentTipRead
                    )
                    AgriTab.DOCTOR -> PlantDoctorScreen(
                        state = plantDoctorState,
                        scanHistoryState = plantScansState,
                        onPhotoSelected = viewModel::setPlantPhoto,
                        onCropHintChanged = viewModel::updateCropHint,
                        onAnalyzeClick = viewModel::analyzeSelectedPlantPhoto,
                        onSelectSampleCase = viewModel::runSamplePlantCase
                    )
                    AgriTab.ANI -> AskAniScreen(
                        messages = aniMessages,
                        selectedLanguage = dashboardData.aniLanguage,
                        isStreaming = isAniStreaming,
                        isDemoMode = dashboardData.isAniDemoMode,
                        onSelectLanguage = viewModel::setAniLanguage,
                        onToggleDemoMode = viewModel::toggleAniDemoMode,
                        onSendQuestion = viewModel::sendAniQuestion,
                        onUseDemoFallback = viewModel::useDemoForLastAniQuestion
                    )
                    AgriTab.TOOLS -> ToolsHubScreen(
                        selectedTab = toolsSubTab,
                        onSelectTab = { toolsSubTab = it },
                        selectedRegion = dashboardData.selectedRegion,
                        weatherState = weatherState,
                        onSelectRegion = viewModel::selectRegion,
                        onRefreshWeather = viewModel::refreshWeather,
                        profileState = profileState,
                        onMarkGuideStudied = viewModel::markGuideStudied,
                        onAskAniAboutCrop = { prompt ->
                            currentTab = AgriTab.ANI
                            viewModel.sendAniQuestion(prompt)
                        },
                        budgetDraft = budgetDraft,
                        budgetsState = budgetsState,
                        budgetAiTipState = budgetAiTipState,
                        onUpdateBudgetDraft = viewModel::updateBudgetDraft,
                        onApplyPreset = viewModel::applyBudgetPreset,
                        onSaveBudget = viewModel::saveCurrentBudget,
                        onDeleteBudget = viewModel::deleteBudget,
                        onGenerateBudgetAiTip = viewModel::generateBudgetAiTip
                    )
                    AgriTab.QUEST -> QuestFarmScreen(
                        profileState = profileState,
                        farmPlotsState = farmPlotsState,
                        onPlantCrop = viewModel::plantCrop,
                        onWaterPlot = viewModel::waterPlot,
                        onFertilizePlot = viewModel::fertilizePlot,
                        onProtectPlot = viewModel::protectPlot,
                        onHarvestPlot = viewModel::harvestPlot,
                        onSubmitChallengeChoice = viewModel::submitChallengeChoice,
                        onSubmitQuizAnswer = viewModel::submitQuizAnswer,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}
