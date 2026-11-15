package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FarmerPassportEntity::class,
        LandPassportEntity::class,
        ClimateSnapshotEntity::class,
        FarmingReminderEntity::class,
        LeafTransactionEntity::class,
        RedeemedPrizeEntity::class,
        LandSimulationEntity::class,
        OfflineDispatchLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GrievousDatabase : RoomDatabase() {
    abstract fun dao(): GrievousDao

    companion object {
        @Volatile
        private var INSTANCE: GrievousDatabase? = null

        fun getInstance(context: Context): GrievousDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrievousDatabase::class.java,
                    "grievous_farm_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(SeedCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback(private val appContext: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(appContext).dao()
                SeedData.populateIfEmpty(dao)
            }
        }
    }
}

object SeedData {
    suspend fun populateIfEmpty(dao: GrievousDao) {
        if (dao.getFarmerPassportOnce() != null) return

        dao.upsertFarmerPassport(
            FarmerPassportEntity(
                id = 1,
                photoUri = "",
                name = "Aarav Deshmukh",
                farmerIdCode = "GRV-FRM-4821",
                district = "Nashik, Maharashtra",
                connectionType = "ONLINE",
                hasSmartphone = true,
                experienceYears = 11,
                cultivatedAcres = 7.5,
                mainCrop = "Paddy Rice",
                governmentSchemes = "PM-KISAN, Soil Health Card, Fasal Bima Yojana",
                rotationStrategy = "Cereal -> Legume (Chickpea) -> Green Cover -> Millet",
                phoneNumber = "+91 98201 44510",
                preferredLanguage = "en",
                dispatchCadence = "WEEKLY",
                cycleNumber = 3
            )
        )

        val land1Id = dao.insertLand(
            LandPassportEntity(
                landName = "Riverbend Terrace Plot A",
                areaAcres = 4.5,
                soilType = "Alluvial Loam",
                locationLabel = "Nashik East • 19.997°N, 73.789°E",
                latitude = 19.9975,
                longitude = 73.7898,
                waterSources = "Drip Canal + Recharge Borewell",
                agriculturalHistory = "Cycle 1: Soybean (1.9 t/ac) • Cycle 2: Paddy Rice (2.4 t/ac)",
                landHealthScore = 86,
                soilMoisturePercent = 64,
                monitoringStatus = "Healthy • Active Satellite Telemetry",
                isSelected = true
            )
        )

        val land2Id = dao.insertLand(
            LandPassportEntity(
                landName = "Red Ridge Upland Plot B",
                areaAcres = 3.0,
                soilType = "Red Sandy Loam",
                locationLabel = "Dindori Ridge • 20.203°N, 73.840°E",
                latitude = 20.2031,
                longitude = 73.8402,
                waterSources = "Farm Check-Dam + Rainwater Basin",
                agriculturalHistory = "Cycle 1: Pearl Millet (1.4 t/ac) • Cycle 2: Pigeon Pea (1.1 t/ac)",
                landHealthScore = 78,
                soilMoisturePercent = 49,
                monitoringStatus = "Moderate Moisture • Mulch Advised",
                isSelected = false
            )
        )

        dao.upsertClimate(
            ClimateSnapshotEntity(
                landId = land1Id,
                temperatureC = 27.4,
                precipitationMm = 3.8,
                solarIrradianceMj = 19.6,
                humidityPercent = 61.0,
                soilWetnessRatio = 0.64,
                sourceLabel = "ECOSTRESS + Climate Baseline",
                isLiveRemote = false,
                fetchedAtMillis = System.currentTimeMillis(),
                landSurfaceTempC = 30.1,
                evapotranspirationMmDay = 3.9,
                potentialEtMmDay = 4.9,
                evaporativeStressIndex = 0.80,
                waterUseEfficiency = 2.65,
                ecostressTileId = "MGRS 43QCB • 70m",
                ecostressOverpassIso = "Baseline Window",
                ecostressGranuleTitle = "ECO_L4T_ESI • PT-JPL 70m"
            )
        )

        dao.upsertClimate(
            ClimateSnapshotEntity(
                landId = land2Id,
                temperatureC = 28.9,
                precipitationMm = 1.6,
                solarIrradianceMj = 21.2,
                humidityPercent = 52.0,
                soilWetnessRatio = 0.49,
                sourceLabel = "ECOSTRESS + Climate Baseline",
                isLiveRemote = false,
                fetchedAtMillis = System.currentTimeMillis(),
                landSurfaceTempC = 34.4,
                evapotranspirationMmDay = 2.6,
                potentialEtMmDay = 5.4,
                evaporativeStressIndex = 0.48,
                waterUseEfficiency = 1.92,
                ecostressTileId = "MGRS 43QCB • 70m",
                ecostressOverpassIso = "Baseline Window",
                ecostressGranuleTitle = "ECO_L4T_ESI • PT-JPL 70m"
            )
        )

        val reminders = listOf(
            FarmingReminderEntity(
                landId = land1Id,
                title = "Basal Bio-Fertilizer & Zinc Mix",
                notes = "Apply 25 kg/acre neem-coated urea + bio-compost along root zone.",
                category = "FERTILIZER",
                cadence = "WEEKLY",
                dayOfMonth = 15,
                scheduledLabel = "Day 15 • Weekly Nutrient Window",
                isCompleted = false,
                leavesReward = 20
            ),
            FarmingReminderEntity(
                landId = land1Id,
                title = "Inspect Drip Emitter & Seed Drill",
                notes = "Flush lateral drip lines and calibrate seed drill spacing before sowing.",
                category = "TOOLING",
                cadence = "DAILY",
                dayOfMonth = 14,
                scheduledLabel = "Daily • Morning Tooling Check",
                isCompleted = false,
                leavesReward = 15
            ),
            FarmingReminderEntity(
                landId = land1Id,
                title = "Grain Moisture Test & Crate Prep",
                notes = "Verify grain moisture <= 14% and sanitize breathable shipping crates.",
                category = "HARVESTING",
                cadence = "MONTHLY",
                dayOfMonth = 24,
                scheduledLabel = "Day 24 • Pre-Harvest Shipping Prep",
                isCompleted = false,
                leavesReward = 25
            ),
            FarmingReminderEntity(
                landId = land2Id,
                title = "Organic Mulch Top-Dressing",
                notes = "Spread 3 cm straw mulch to conserve upland root moisture.",
                category = "FERTILIZER",
                cadence = "WEEKLY",
                dayOfMonth = 18,
                scheduledLabel = "Day 18 • Soil Moisture Shield",
                isCompleted = true,
                leavesReward = 20,
                completedAtMillis = System.currentTimeMillis() - 86_400_000L
            )
        )
        reminders.forEach { dao.insertReminder(it) }

        dao.insertLeafTransaction(
            LeafTransactionEntity(
                title = "Cycle 2 Profit Efficiency Bonus",
                sourceType = "PROFIT",
                leavesDelta = 85,
                timestampMillis = System.currentTimeMillis() - 172_800_000L
            )
        )
        dao.insertLeafTransaction(
            LeafTransactionEntity(
                title = "Task Completed: Organic Mulch Top-Dressing",
                sourceType = "REMINDER",
                leavesDelta = 20,
                timestampMillis = System.currentTimeMillis() - 86_400_000L
            )
        )
        dao.insertLeafTransaction(
            LeafTransactionEntity(
                title = "Cycle 2 Passport Renewal Reward",
                sourceType = "RENEWAL",
                leavesDelta = 40,
                timestampMillis = System.currentTimeMillis() - 259_200_000L
            )
        )

        dao.insertSimulation(
            LandSimulationEntity(
                landId = land1Id,
                plotLabel = "Riverbend Terrace Plot A",
                cropName = "Paddy Rice -> Chickpea",
                areaAcres = 4.5,
                slopeDegrees = 3.5,
                soilType = "Alluvial Loam",
                waterScore = 82,
                fertilizerIntensity = 70,
                shippingLeadDays = 2,
                successRatePercent = 91,
                projectedYieldTons = 10.8,
                estimatedProfitUsd = 3420,
                lossAvoidedUsd = 790,
                riskSummary = "Low drought risk • Optimal shipping margin with 2-day cold-crate dispatch"
            )
        )
    }
}
