package com.example

import com.example.data.local.ClimateSnapshotEntity
import com.example.data.local.FarmerPassportEntity
import com.example.data.local.LandPassportEntity
import com.example.data.remote.CmrGranuleLink
import com.example.data.remote.EcostressCmrApi
import com.example.domain.AgroAnalyticsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgroAnalyticsEngineTest {

    private val sampleFarmer = FarmerPassportEntity(
        name = "Aarav",
        farmerIdCode = "GRV-FRM-4821",
        district = "Nashik",
        connectionType = "OFFLINE",
        hasSmartphone = false,
        experienceYears = 10,
        cultivatedAcres = 5.0,
        mainCrop = "Paddy Rice",
        governmentSchemes = "PM-KISAN",
        rotationStrategy = "Cereal -> Legume",
        phoneNumber = "+919820144510",
        preferredLanguage = "hi",
        dispatchCadence = "WEEKLY"
    )

    private val sampleLand = LandPassportEntity(
        id = 1L,
        landName = "Plot A",
        areaAcres = 5.0,
        soilType = "Alluvial Loam",
        locationLabel = "Nashik",
        latitude = 19.99,
        longitude = 73.78,
        waterSources = "Drip Canal",
        agriculturalHistory = "Cycle 1: Soybean",
        landHealthScore = 88,
        soilMoisturePercent = 64,
        monitoringStatus = "Healthy",
        isSelected = true
    )

    private val sampleClimate = ClimateSnapshotEntity(
        landId = 1L,
        temperatureC = 27.0,
        precipitationMm = 3.5,
        solarIrradianceMj = 19.2,
        humidityPercent = 62.0,
        soilWetnessRatio = 0.65,
        sourceLabel = "ECOSTRESS 70m CMR + POWER",
        isLiveRemote = true,
        fetchedAtMillis = 0L,
        landSurfaceTempC = 30.2,
        evapotranspirationMmDay = 3.9,
        potentialEtMmDay = 4.9,
        evaporativeStressIndex = 0.80,
        waterUseEfficiency = 2.65
    )

    @Test
    fun dashboardInsight_computesWaterMarketsAndRevenue() {
        val insight = AgroAnalyticsEngine.buildDashboardInsight(sampleFarmer, sampleLand, sampleClimate)
        assertTrue(insight.estimatedRevenueUsd > 0)
        assertTrue(insight.waterAvailabilityPct in 20..98)
        assertEquals(3, insight.nearbyMarkets.size)
        assertTrue(insight.evaporativeStressIndex >= 0.70)
    }

    @Test
    fun dashboardInsight_detectsEcostressFlashDroughtAndThermalStress() {
        val stressedClimate = sampleClimate.copy(
            temperatureC = 30.0,
            landSurfaceTempC = 36.2,
            evapotranspirationMmDay = 1.8,
            potentialEtMmDay = 5.8,
            evaporativeStressIndex = 0.34
        )
        val insight = AgroAnalyticsEngine.buildDashboardInsight(sampleFarmer, sampleLand, stressedClimate)
        assertTrue(insight.waterStatusLabel.contains("ESI", ignoreCase = true))
        assertTrue(insight.irrigationAdvice.contains("ECOSTRESS ESI", ignoreCase = true))
        assertTrue(insight.bestHarvestWindow.contains("LST", ignoreCase = true))
    }

    @Test
    fun ecostressCmrParsers_extractMgrsTileAndBrowseUrl() {
        val tile = EcostressCmrApi.extractMgrsTile("ECOv002_L4T_ESI_30492_003_43QCB_20250615T123045_0710_01_ESI.tif")
        assertEquals("43QCB", tile)

        val browse = EcostressCmrApi.extractBrowseUrl(
            listOf(
                CmrGranuleLink(rel = "http://esipfed.org/opensearch/spec/1.01/def#enclosure", href = "https://data.lpdaac.earthdatacloud.nasa.gov/file.tif"),
                CmrGranuleLink(rel = "http://esipfed.org/opensearch/spec/1.01/def#icon", href = "https://cmr.earthdata.nasa.gov/browse-scaler/browse_images/granules/G123")
            )
        )
        assertTrue(browse.contains("browse-scaler"))
    }

    @Test
    fun virtualLandSimulation_producesSuccessAndAvoidedLoss() {
        val sim = AgroAnalyticsEngine.simulateVirtualLand(
            landId = 1L,
            plotLabel = "Plot A",
            cropName = "Chickpea",
            areaAcres = 5.0,
            slopeDegrees = 2.5,
            soilType = "Alluvial Loam",
            waterScore = 75,
            fertilizerIntensity = 70,
            shippingLeadDays = 2,
            climate = sampleClimate
        )
        assertTrue(sim.successRatePercent in 40..97)
        assertTrue(sim.estimatedProfitUsd > 0)
        assertTrue(sim.lossAvoidedUsd > 0)
    }

    @Test
    fun rotationWheel_recommendsSortedFourStageStrategies() {
        val recs = AgroAnalyticsEngine.recommendRotations(
            season = "Winter (Rabi)",
            monthName = "Nov",
            farmer = sampleFarmer,
            land = sampleLand,
            climate = sampleClimate
        )
        assertEquals(3, recs.size)
        assertTrue(recs.first().matchScorePct >= recs.last().matchScorePct)
        assertEquals(4, recs.first().wheelSegments.size)
    }

    @Test
    fun offlineDispatchMessage_includesFarmerCadenceAndLanguage() {
        val insight = AgroAnalyticsEngine.buildDashboardInsight(sampleFarmer, sampleLand, sampleClimate)
        val msg = AgroAnalyticsEngine.buildOfflineDispatchMessage(sampleFarmer, sampleLand, insight)
        assertTrue(msg.contains("Weekly"))
        assertTrue(msg.contains("HI"))
        assertTrue(msg.contains("ESI"))
        assertTrue(msg.contains(sampleFarmer.farmerIdCode))
    }
}
