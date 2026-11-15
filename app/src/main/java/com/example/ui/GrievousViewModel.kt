package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.i18n.LocalizedStrings
import com.example.core.i18n.StringsRegistry
import com.example.core.voice.VoiceController
import com.example.data.local.ClimateSnapshotEntity
import com.example.data.local.FarmerPassportEntity
import com.example.data.local.FarmingReminderEntity
import com.example.data.local.GrievousDatabase
import com.example.data.local.LandPassportEntity
import com.example.data.local.LandSimulationEntity
import com.example.data.local.LeafTransactionEntity
import com.example.data.local.OfflineDispatchLogEntity
import com.example.data.local.PreferencesRepository
import com.example.data.local.RedeemedPrizeEntity
import com.example.data.remote.AgroClimateRepository
import com.example.data.remote.EcostressCmrApi
import com.example.data.remote.NasaPowerApi
import com.example.domain.AgroAnalyticsEngine
import com.example.domain.CropMarketInsight
import com.example.domain.RotationRecommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RewardPrize(
    val code: String,
    val title: String,
    val subtitle: String,
    val costLeaves: Int
)

val GrievousPrizeCatalog = listOf(
    RewardPrize("BIO_KIT", "Organic Bio-Fertilizer Kit", "25kg enriched compost & micronutrients", 100),
    RewardPrize("MOISTURE_PROBE", "Digital Soil Moisture Probe", "Handheld root-zone salinity & moisture meter", 200),
    RewardPrize("DRIP_VOUCHER", "Micro-Drip Line Expansion", "50% voucher for solar drip lateral tubing", 350),
    RewardPrize("CLIMATE_SEEDS", "Drought-Resilient Seed Pack", "Certified high-yield rotation seed bundle", 500)
)

data class GrievousUiState(
    val languageCode: String = "en",
    val strings: LocalizedStrings = StringsRegistry.forLanguage("en"),
    val isSpeaking: Boolean = false,
    val farmer: FarmerPassportEntity? = null,
    val lands: List<LandPassportEntity> = emptyList(),
    val selectedLand: LandPassportEntity? = null,
    val selectedClimate: ClimateSnapshotEntity? = null,
    val insight: CropMarketInsight = AgroAnalyticsEngine.buildDashboardInsight(null, null, null),
    val reminders: List<FarmingReminderEntity> = emptyList(),
    val leafTransactions: List<LeafTransactionEntity> = emptyList(),
    val redeemedPrizes: List<RedeemedPrizeEntity> = emptyList(),
    val totalLeavesBalance: Int = 0,
    val vaultSavedLeaves: Int = 0,
    val simulations: List<LandSimulationEntity> = emptyList(),
    val dispatchLogs: List<OfflineDispatchLogEntity> = emptyList(),
    val isSyncingTelemetry: Boolean = false,
    val isDarkTheme: Boolean = false,
    val statusMessage: String? = null
)

class GrievousViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = GrievousDatabase.getInstance(application).dao()
    private val prefs = PreferencesRepository(application)
    private val climateRepo = AgroClimateRepository(
        api = NasaPowerApi.create(),
        ecostressApi = EcostressCmrApi.create(),
        dao = dao
    )
    val voiceController = VoiceController(application)

    private val isSyncing = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            val lands = dao.observeLands().first { it.isNotEmpty() }
            val active = lands.firstOrNull { it.isSelected } ?: lands.first()
            syncTelemetryForLandInternal(active, showToast = false)
        }
    }

    private val baseDataFlow = combine(
        dao.observeFarmerPassport(),
        dao.observeLands(),
        dao.observeAllClimates(),
        dao.observeReminders(),
        dao.observeLeafTransactions()
    ) { farmer, lands, climates, reminders, leafTxs ->
        BaseDataBundle(farmer, lands, climates, reminders, leafTxs)
    }

    private val prefsBundleFlow = combine(
        prefs.languageCode,
        prefs.vaultSavedLeaves,
        prefs.isDarkMode
    ) { lang, vault, dark ->
        Triple(lang, vault, dark)
    }

    private val secondaryDataFlow = combine(
        dao.observeRedeemedPrizes(),
        dao.observeSimulations(),
        dao.observeDispatchLogs(),
        prefsBundleFlow
    ) { prizes, sims, dispatches, prefsTriple ->
        SecondaryDataBundle(prizes, sims, dispatches, prefsTriple.first, prefsTriple.second, prefsTriple.third)
    }

    val uiState: StateFlow<GrievousUiState> = combine(
        baseDataFlow,
        secondaryDataFlow,
        voiceController.isSpeaking,
        isSyncing,
        statusMessage
    ) { base, sec, speaking, syncing, msg ->
        val selectedLand = base.lands.firstOrNull { it.isSelected } ?: base.lands.firstOrNull()
        val climate = selectedLand?.let { land ->
            base.climates.firstOrNull { it.landId == land.id }
        }
        val balance = base.leafTxs.sumOf { it.leavesDelta }.coerceAtLeast(0)
        val clampedVault = sec.vaultSavedLeaves.coerceAtMost(balance)
        val insight = AgroAnalyticsEngine.buildDashboardInsight(base.farmer, selectedLand, climate)
        GrievousUiState(
            languageCode = sec.languageCode,
            strings = StringsRegistry.forLanguage(sec.languageCode),
            isSpeaking = speaking,
            farmer = base.farmer,
            lands = base.lands,
            selectedLand = selectedLand,
            selectedClimate = climate,
            insight = insight,
            reminders = base.reminders,
            leafTransactions = base.leafTxs,
            redeemedPrizes = sec.prizes,
            totalLeavesBalance = balance,
            vaultSavedLeaves = clampedVault,
            simulations = sec.simulations,
            dispatchLogs = sec.dispatches,
            isSyncingTelemetry = syncing,
            isDarkTheme = sec.isDarkTheme,
            statusMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GrievousUiState()
    )

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            prefs.setLanguageCode(code)
            uiState.value.farmer?.let { farmer ->
                dao.upsertFarmerPassport(farmer.copy(preferredLanguage = code))
            }
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            prefs.setDarkMode(!uiState.value.isDarkTheme)
        }
    }

    fun speakText(text: String) {
        voiceController.speak(text, uiState.value.languageCode)
    }

    fun stopSpeaking() {
        voiceController.stop()
    }

    fun selectLand(landId: Long) {
        viewModelScope.launch {
            dao.selectLand(landId)
            val land = dao.getLandsOnce().firstOrNull { it.id == landId } ?: return@launch
            syncTelemetryForLandInternal(land)
        }
    }

    fun syncSelectedLandTelemetry() {
        val land = uiState.value.selectedLand ?: return
        viewModelScope.launch {
            syncTelemetryForLandInternal(land)
        }
    }

    private suspend fun syncTelemetryForLandInternal(land: LandPassportEntity, showToast: Boolean = true) {
        isSyncing.value = true
        val result = climateRepo.syncTelemetryForLand(land)
        isSyncing.value = false
        if (showToast) {
            statusMessage.value = if (result.isSuccess) {
                "ECOSTRESS 70m & POWER telemetry synced for ${land.landName}"
            } else {
                "Offline geospatial climate baseline active for ${land.landName}"
            }
        }
    }

    fun saveFarmerPassport(updated: FarmerPassportEntity) {
        viewModelScope.launch {
            dao.upsertFarmerPassport(updated.copy(updatedAtMillis = System.currentTimeMillis()))
            prefs.setLanguageCode(updated.preferredLanguage)
            statusMessage.value = "Farmer passport saved"
        }
    }

    fun renewFarmerPassport(
        newMainCrop: String,
        newAcres: Double,
        newRotationStrategy: String,
        harvestYieldNote: String
    ) {
        viewModelScope.launch {
            val current = uiState.value.farmer ?: return@launch
            val selectedLand = uiState.value.selectedLand
            val nextCycle = current.cycleNumber + 1
            dao.upsertFarmerPassport(
                current.copy(
                    mainCrop = newMainCrop,
                    cultivatedAcres = newAcres,
                    rotationStrategy = newRotationStrategy,
                    experienceYears = current.experienceYears + 1,
                    cycleNumber = nextCycle,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            if (selectedLand != null) {
                val updatedHistory = buildString {
                    append(selectedLand.agriculturalHistory)
                    if (selectedLand.agriculturalHistory.isNotBlank()) append(" • ")
                    append("Cycle ${current.cycleNumber}: ${current.mainCrop} ($harvestYieldNote)")
                }
                dao.updateLand(selectedLand.copy(agriculturalHistory = updatedHistory))
            }
            dao.insertLeafTransaction(
                LeafTransactionEntity(
                    title = "Passport Renewed for Cycle $nextCycle",
                    sourceType = "RENEWAL",
                    leavesDelta = 40
                )
            )
            statusMessage.value = "Passport renewed for Cycle $nextCycle (+40 Leaves earned)"
        }
    }

    fun saveLandPassport(land: LandPassportEntity) {
        viewModelScope.launch {
            if (land.id == 0L) {
                val newId = dao.insertLand(land.copy(isSelected = true))
                dao.selectLand(newId)
                syncTelemetryForLandInternal(land.copy(id = newId, isSelected = true))
            } else {
                dao.updateLand(land)
                if (land.isSelected) {
                    syncTelemetryForLandInternal(land)
                }
            }
            statusMessage.value = "Land passport saved"
        }
    }

    fun logOfflineDispatch(channel: String, messageText: String) {
        viewModelScope.launch {
            val farmer = uiState.value.farmer ?: return@launch
            dao.insertDispatchLog(
                OfflineDispatchLogEntity(
                    farmerName = farmer.name,
                    phoneNumber = farmer.phoneNumber,
                    channel = channel,
                    cadence = farmer.dispatchCadence,
                    languageCode = farmer.preferredLanguage,
                    summaryText = messageText
                )
            )
            statusMessage.value = "$channel dispatch prepared for ${farmer.phoneNumber}"
        }
    }

    fun addReminder(
        title: String,
        notes: String,
        category: String,
        cadence: String,
        dayOfMonth: Int
    ) {
        viewModelScope.launch {
            val landId = uiState.value.selectedLand?.id ?: 1L
            val reward = when (category) {
                "HARVESTING" -> 25
                "FERTILIZER" -> 20
                else -> 15
            }
            dao.insertReminder(
                FarmingReminderEntity(
                    landId = landId,
                    title = title,
                    notes = notes,
                    category = category,
                    cadence = cadence,
                    dayOfMonth = dayOfMonth.coerceIn(1, 31),
                    scheduledLabel = "Day $dayOfMonth • ${cadence.lowercase().replaceFirstChar { it.uppercase() }}",
                    isCompleted = false,
                    leavesReward = reward
                )
            )
            statusMessage.value = "Reminder added (+${reward} Leaves on proper completion)"
        }
    }

    fun completeReminderProperly(reminder: FarmingReminderEntity) {
        viewModelScope.launch {
            dao.completeReminderWithReward(reminder)
            statusMessage.value = "Great work! +${reminder.leavesReward} Leaves added to your ledger"
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            dao.deleteReminder(id)
        }
    }

    fun updateVaultSavedLeaves(amount: Int) {
        viewModelScope.launch {
            val maxAvailable = uiState.value.totalLeavesBalance
            prefs.setVaultSavedLeaves(amount.coerceIn(0, maxAvailable))
        }
    }

    fun recordProfitMilestone() {
        viewModelScope.launch {
            dao.insertLeafTransaction(
                LeafTransactionEntity(
                    title = "Verified Field-Shipping Profit Milestone",
                    sourceType = "PROFIT",
                    leavesDelta = 50
                )
            )
            statusMessage.value = "+50 Leaves awarded for profit milestone!"
        }
    }

    fun redeemPrize(prize: RewardPrize) {
        viewModelScope.launch {
            val currentBalance = uiState.value.totalLeavesBalance
            val spendable = currentBalance - uiState.value.vaultSavedLeaves
            if (currentBalance < prize.costLeaves) {
                statusMessage.value = "Need ${prize.costLeaves - currentBalance} more Leaves for ${prize.title}"
                return@launch
            }
            if (spendable < prize.costLeaves) {
                statusMessage.value = "Unlock Leaves from your Future Savings Vault to redeem ${prize.title}"
                return@launch
            }
            dao.insertLeafTransaction(
                LeafTransactionEntity(
                    title = "Prize Redeemed: ${prize.title}",
                    sourceType = "PRIZE",
                    leavesDelta = -prize.costLeaves
                )
            )
            dao.insertRedeemedPrize(
                RedeemedPrizeEntity(
                    prizeCode = prize.code,
                    prizeTitle = prize.title,
                    leavesCost = prize.costLeaves
                )
            )
            statusMessage.value = "Redeemed ${prize.title}!"
        }
    }

    fun runLandSimulation(
        cropName: String,
        areaAcres: Double,
        slopeDegrees: Double,
        soilType: String,
        waterScore: Int,
        fertilizerIntensity: Int,
        shippingLeadDays: Int
    ): LandSimulationEntity {
        val land = uiState.value.selectedLand
        val sim = AgroAnalyticsEngine.simulateVirtualLand(
            landId = land?.id ?: 1L,
            plotLabel = land?.landName ?: "Virtual Plot",
            cropName = cropName,
            areaAcres = areaAcres,
            slopeDegrees = slopeDegrees,
            soilType = soilType,
            waterScore = waterScore,
            fertilizerIntensity = fertilizerIntensity,
            shippingLeadDays = shippingLeadDays,
            climate = uiState.value.selectedClimate
        )
        viewModelScope.launch {
            dao.insertSimulation(sim)
            dao.insertLeafTransaction(
                LeafTransactionEntity(
                    title = "3D Risk Test: ${sim.cropName} (${sim.successRatePercent}%)",
                    sourceType = "SIMULATION",
                    leavesDelta = 10
                )
            )
            statusMessage.value = "3D Model simulated: ${sim.successRatePercent}% success • +10 Leaves"
        }
        return sim
    }

    fun applyRotationStrategy(recommendation: RotationRecommendation) {
        viewModelScope.launch {
            val farmer = uiState.value.farmer ?: return@launch
            dao.upsertFarmerPassport(
                farmer.copy(
                    rotationStrategy = recommendation.sequenceTitle,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            dao.insertLeafTransaction(
                LeafTransactionEntity(
                    title = "Applied Rotation: ${recommendation.sequenceTitle}",
                    sourceType = "SIMULATION",
                    leavesDelta = 15
                )
            )
            statusMessage.value = "Rotation strategy saved to Passport (+15 Leaves)"
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceController.shutdown()
    }

    private data class BaseDataBundle(
        val farmer: FarmerPassportEntity?,
        val lands: List<LandPassportEntity>,
        val climates: List<ClimateSnapshotEntity>,
        val reminders: List<FarmingReminderEntity>,
        val leafTxs: List<LeafTransactionEntity>
    )

    private data class SecondaryDataBundle(
        val prizes: List<RedeemedPrizeEntity>,
        val simulations: List<LandSimulationEntity>,
        val dispatches: List<OfflineDispatchLogEntity>,
        val languageCode: String,
        val vaultSavedLeaves: Int,
        val isDarkTheme: Boolean
    )
}
