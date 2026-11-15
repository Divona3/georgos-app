package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.voice.ReminderNotifier
import com.example.domain.AgroAnalyticsEngine
import com.example.ui.GrievousUiState
import com.example.ui.GrievousViewModel
import com.example.ui.components.DashboardTelemetryLogo
import com.example.ui.components.FarmerCalendarLogo
import com.example.ui.components.GrievousTopUtilityBar
import com.example.ui.components.Land3DStructureLogo
import com.example.ui.components.LeavesLogo
import com.example.ui.components.PassportBadgeLogo
import com.example.ui.components.RotationWheelLogo
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PassportsScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.RotationWheelScreen
import com.example.ui.screens.VirtualLandScreen
import com.example.ui.theme.AmberHarvest
import com.example.ui.theme.GrievousTheme
import com.example.ui.theme.SproutGreen

object GrievousRoutes {
    const val PASSPORTS = "passports"
    const val DASHBOARD = "dashboard"
    const val CALENDAR = "calendar"
    const val REWARDS = "rewards"
    const val VIRTUAL_LAND = "virtual_land"
    const val ROTATION = "rotation"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderNotifier.ensureChannel(this)
        setContent {
            val viewModel: GrievousViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            GrievousTheme(darkTheme = state.isDarkTheme) {
                GrievousApp(viewModel = viewModel, state = state)
            }
        }
    }
}

@Composable
fun GrievousApp(viewModel: GrievousViewModel, state: GrievousUiState) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: GrievousRoutes.PASSPORTS
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearStatusMessage()
    }

    val navItems = listOf(
        GrievousRoutes.PASSPORTS to state.strings.navPassports,
        GrievousRoutes.DASHBOARD to state.strings.navDashboard,
        GrievousRoutes.CALENDAR to state.strings.navCalendar,
        GrievousRoutes.REWARDS to state.strings.navRewards,
        GrievousRoutes.VIRTUAL_LAND to state.strings.navVirtualLand,
        GrievousRoutes.ROTATION to state.strings.navRotation
    )

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Column(modifier = Modifier.statusBarsPadding()) {
                    GrievousTopUtilityBar(
                        strings = state.strings,
                        currentLanguageCode = state.languageCode,
                        activeLandName = state.selectedLand?.landName,
                        isSpeaking = state.isSpeaking,
                        isDarkTheme = state.isDarkTheme,
                        onToggleTheme = { viewModel.toggleDarkTheme() },
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        onSpeakSummary = {
                            val summary = buildVoiceSummaryForRoute(currentRoute, state)
                            viewModel.speakText(summary)
                        },
                        onStopSpeaking = { viewModel.stopSpeaking() }
                    )
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        navItems.forEach { (route, label) ->
                            val selected = currentRoute == route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(route) },
                                icon = { ModuleNavIcon(route = route, selected = selected) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_$route")
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
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        navItems.forEach { (route, label) ->
                            val selected = currentRoute == route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(route) },
                                icon = { ModuleNavIcon(route = route, selected = selected) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_rail_$route")
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = GrievousRoutes.PASSPORTS,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(GrievousRoutes.PASSPORTS) {
                        PassportsScreen(
                            state = state,
                            onSaveFarmer = viewModel::saveFarmerPassport,
                            onRenewPassport = viewModel::renewFarmerPassport,
                            onSelectLand = viewModel::selectLand,
                            onSaveLand = viewModel::saveLandPassport,
                            onLogDispatch = viewModel::logOfflineDispatch,
                            onSpeakText = viewModel::speakText
                        )
                    }
                    composable(GrievousRoutes.DASHBOARD) {
                        DashboardScreen(
                            state = state,
                            onSelectLand = viewModel::selectLand,
                            onRefreshTelemetry = viewModel::syncSelectedLandTelemetry
                        )
                    }
                    composable(GrievousRoutes.CALENDAR) {
                        CalendarScreen(
                            state = state,
                            onAddReminder = viewModel::addReminder,
                            onCompleteReminder = viewModel::completeReminderProperly,
                            onDeleteReminder = viewModel::deleteReminder,
                            onSpeakText = viewModel::speakText
                        )
                    }
                    composable(GrievousRoutes.REWARDS) {
                        RewardsScreen(
                            state = state,
                            onUpdateVault = viewModel::updateVaultSavedLeaves,
                            onRecordProfitMilestone = viewModel::recordProfitMilestone,
                            onRedeemPrize = viewModel::redeemPrize
                        )
                    }
                    composable(GrievousRoutes.VIRTUAL_LAND) {
                        VirtualLandScreen(
                            state = state,
                            onRunSimulation = viewModel::runLandSimulation
                        )
                    }
                    composable(GrievousRoutes.ROTATION) {
                        RotationWheelScreen(
                            state = state,
                            onApplyStrategy = viewModel::applyRotationStrategy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleNavIcon(route: String, selected: Boolean) {
    val primaryTint = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val secondarySoilTint = MaterialTheme.colorScheme.secondary
    when (route) {
        GrievousRoutes.PASSPORTS -> PassportBadgeLogo(size = 24.dp, tint = primaryTint)
        GrievousRoutes.DASHBOARD -> DashboardTelemetryLogo(size = 24.dp, tint = primaryTint)
        GrievousRoutes.CALENDAR -> FarmerCalendarLogo(size = 24.dp, primaryColor = primaryTint, accentColor = AmberHarvest)
        GrievousRoutes.REWARDS -> LeavesLogo(size = 24.dp, primaryColor = primaryTint, secondaryColor = SproutGreen)
        GrievousRoutes.VIRTUAL_LAND -> Land3DStructureLogo(size = 24.dp, topColor = primaryTint, leftSoilColor = secondarySoilTint, rightSoilColor = AmberHarvest)
        GrievousRoutes.ROTATION -> RotationWheelLogo(size = 24.dp, tint = primaryTint, accent = AmberHarvest)
    }
}

private fun buildVoiceSummaryForRoute(route: String, state: GrievousUiState): String {
    val farmerName = state.farmer?.name ?: "Farmer"
    val landName = state.selectedLand?.landName ?: "your selected land"
    return when (route) {
        GrievousRoutes.PASSPORTS ->
            "Digital Passport for $farmerName in ${state.farmer?.district ?: "your district"}. Active plot is $landName. Connection mode is ${state.farmer?.connectionType ?: "ONLINE"}."
        GrievousRoutes.DASHBOARD -> {
            val ins = state.insight
            "Dashboard for $landName. ${ins.cropName} market price is ${ins.pricePerTonUsd} dollars per ton. Land surface temperature is ${String.format("%.1f", ins.landSurfaceTempC)} degrees Celsius, evapotranspiration is ${String.format("%.1f", ins.evapotranspirationMmDay)} millimeters per day, and evaporative stress index is ${String.format("%.2f", ins.evaporativeStressIndex)}. Water availability is ${ins.waterAvailabilityPct} percent. ${ins.bestHarvestWindow}."
        }
        GrievousRoutes.CALENDAR -> {
            val pending = state.reminders.count { !it.isCompleted }
            "Farming calendar has $pending upcoming reminders for fertilizer, tools, and harvesting. Complete tasks properly to earn Leaves."
        }
        GrievousRoutes.REWARDS ->
            "You have ${state.totalLeavesBalance} Leaves total, with ${state.vaultSavedLeaves} saved in your future harvest vault."
        GrievousRoutes.VIRTUAL_LAND -> {
            val latest = state.simulations.firstOrNull()
            if (latest != null) {
                "3D Geospatial land model for ${latest.cropName} shows ${latest.successRatePercent} percent success rate and ${latest.lossAvoidedUsd} dollars in avoided loss."
            } else {
                "Enter your land and crop parameters to simulate success rates in 3D."
            }
        }
        GrievousRoutes.ROTATION -> {
            val recs = AgroAnalyticsEngine.recommendRotations("Winter (Rabi)", "Nov", state.farmer, state.selectedLand, state.selectedClimate)
            val top = recs.first()
            "Crop rotation wheel recommends ${top.sequenceTitle} with ${top.matchScorePct} percent climate fit and ${top.profitLiftPct} percent profit lift."
        }
        else -> "Welcome to Georgos precision farming."
    }
}
