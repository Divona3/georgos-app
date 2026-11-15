package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmer_passport")
data class FarmerPassportEntity(
    @PrimaryKey val id: Int = 1,
    val photoUri: String = "",
    val name: String,
    val farmerIdCode: String,
    val district: String,
    val connectionType: String, // "ONLINE" or "OFFLINE"
    val hasSmartphone: Boolean,
    val experienceYears: Int,
    val cultivatedAcres: Double,
    val mainCrop: String,
    val governmentSchemes: String, // comma-separated
    val rotationStrategy: String,
    val phoneNumber: String,
    val preferredLanguage: String,
    val dispatchCadence: String, // "DAILY", "WEEKLY", "MONTHLY"
    val cycleNumber: Int = 1,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "land_passports")
data class LandPassportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val landName: String,
    val photoUri: String = "",
    val areaAcres: Double,
    val soilType: String,
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double,
    val waterSources: String,
    val agriculturalHistory: String,
    val landHealthScore: Int, // 0..100
    val soilMoisturePercent: Int, // 0..100
    val monitoringStatus: String,
    val isSelected: Boolean = false
)

@Entity(tableName = "climate_snapshots")
data class ClimateSnapshotEntity(
    @PrimaryKey val landId: Long,
    val temperatureC: Double,
    val precipitationMm: Double,
    val solarIrradianceMj: Double,
    val humidityPercent: Double,
    val soilWetnessRatio: Double,
    val sourceLabel: String,
    val isLiveRemote: Boolean,
    val fetchedAtMillis: Long,
    val windSpeedMs: Double = 2.4,
    val rootZoneWetnessRatio: Double = 0.62,
    val tempMinC: Double = 20.0,
    val tempMaxC: Double = 32.0,
    val observationDateRange: String = "Recent 10d Window",
    // ECOSTRESS 70m Thermal, Evapotranspiration & Plant Stress Telemetry
    val landSurfaceTempC: Double = 30.2,
    val evapotranspirationMmDay: Double = 3.6,
    val potentialEtMmDay: Double = 4.8,
    val evaporativeStressIndex: Double = 0.75,
    val waterUseEfficiency: Double = 2.35,
    val ecostressTileId: String = "MGRS 43QCB • 70m",
    val ecostressOverpassIso: String = "Baseline Window",
    val ecostressGranuleTitle: String = "ECO_L4T_ESI • PT-JPL 70m",
    val ecostressBrowseUrl: String = "",
    val ecostressProductCount: Int = 0,
    val ecostressLive: Boolean = false
)

@Entity(tableName = "farming_reminders")
data class FarmingReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val landId: Long,
    val title: String,
    val notes: String,
    val category: String, // "FERTILIZER", "TOOLING", "HARVESTING"
    val cadence: String,  // "DAILY", "WEEKLY", "MONTHLY"
    val dayOfMonth: Int,
    val scheduledLabel: String,
    val isCompleted: Boolean = false,
    val leavesReward: Int = 15,
    val completedAtMillis: Long? = null
)

@Entity(tableName = "leaf_transactions")
data class LeafTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sourceType: String, // "REMINDER", "RENEWAL", "PROFIT", "SIMULATION", "VAULT", "PRIZE"
    val leavesDelta: Int,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "redeemed_prizes")
data class RedeemedPrizeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prizeCode: String,
    val prizeTitle: String,
    val leavesCost: Int,
    val redeemedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "land_simulations")
data class LandSimulationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val landId: Long,
    val plotLabel: String,
    val cropName: String,
    val areaAcres: Double,
    val slopeDegrees: Double,
    val soilType: String,
    val waterScore: Int,
    val fertilizerIntensity: Int,
    val shippingLeadDays: Int,
    val successRatePercent: Int,
    val projectedYieldTons: Double,
    val estimatedProfitUsd: Int,
    val lossAvoidedUsd: Int,
    val riskSummary: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_dispatch_logs")
data class OfflineDispatchLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val farmerName: String,
    val phoneNumber: String,
    val channel: String, // "SMS" or "VOICE_CALL"
    val cadence: String,
    val languageCode: String,
    val summaryText: String,
    val dispatchedAtMillis: Long = System.currentTimeMillis()
)
