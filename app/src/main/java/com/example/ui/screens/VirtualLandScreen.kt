package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LandSimulationEntity
import com.example.ui.GrievousUiState
import com.example.ui.components.Land3DStructureLogo
import com.example.ui.components.VoiceInputTextField
import com.example.ui.theme.AmberHarvest
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LoamBrown
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.OffWhite
import com.example.ui.theme.SproutGreen
import com.example.ui.theme.WaterTeal
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun VirtualLandScreen(
    state: GrievousUiState,
    onRunSimulation: (
        cropName: String,
        areaAcres: Double,
        slopeDegrees: Double,
        soilType: String,
        waterScore: Int,
        fertilizerIntensity: Int,
        shippingLeadDays: Int
    ) -> LandSimulationEntity
) {
    val activeLand = state.selectedLand
    var cropName by remember(state.farmer?.mainCrop) {
        mutableStateOf(state.farmer?.mainCrop ?: "Paddy Rice")
    }
    var soilType by remember(activeLand?.soilType) {
        mutableStateOf(activeLand?.soilType ?: "Alluvial Loam")
    }
    var areaAcres by remember(activeLand?.areaAcres) {
        mutableFloatStateOf((activeLand?.areaAcres ?: 4.5).toFloat())
    }
    var slopeDegrees by remember { mutableFloatStateOf(3.5f) }
    var waterScore by remember(state.insight.waterAvailabilityPct) {
        mutableIntStateOf(state.insight.waterAvailabilityPct)
    }
    var fertilizerIntensity by remember { mutableIntStateOf(70) }
    var shippingLeadDays by remember { mutableIntStateOf(2) }

    var yawDegrees by remember { mutableFloatStateOf(38f) }
    var pitchDegrees by remember { mutableFloatStateOf(28f) }
    var latestResult by remember(state.simulations) {
        mutableStateOf(state.simulations.firstOrNull())
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Land3DStructureLogo(size = 30.dp)
                    Column {
                        Text(state.strings.virtualLandTitle, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Drag 3D geospatial model to inspect slope, water flow & loss risk",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Interactive 3D Geospatial Land Renderer
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ViewInAr, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${activeLand?.landName ?: "Virtual Plot"} • ${String.format("%.3f", activeLand?.latitude ?: 19.997)}°N",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "Success: ${latestResult?.successRatePercent ?: 88}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ForestGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        yawDegrees = (yawDegrees + dragAmount.x * 0.4f).coerceIn(-65f, 75f)
                                        pitchDegrees = (pitchDegrees - dragAmount.y * 0.25f).coerceIn(14f, 52f)
                                    }
                                }
                        ) {
                            Geospatial3DLandCanvas(
                                yawDegrees = yawDegrees,
                                pitchDegrees = pitchDegrees,
                                slopeDegrees = slopeDegrees,
                                waterScore = waterScore,
                                successRate = latestResult?.successRatePercent ?: 88,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "Drag to orbit • $soilType • LST ${String.format("%.1f", state.insight.landSurfaceTempC)}°C • ESI ${String.format("%.2f", state.insight.evaporativeStressIndex)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Parameter Inputs Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Geospatial & Strategy Inputs", style = MaterialTheme.typography.titleMedium)

                        VoiceInputTextField(
                            value = cropName,
                            onValueChange = { cropName = it },
                            label = "Target Crop Strategy",
                            languageCode = state.languageCode,
                            testTag = "sim_crop_input"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Alluvial Loam", "Black Cotton", "Red Sandy Loam", "Clay Loam").forEach { soil ->
                                FilterChip(
                                    selected = soilType.equals(soil, ignoreCase = true),
                                    onClick = { soilType = soil },
                                    label = { Text(soil, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        SliderParam("Plot Area: ${String.format("%.1f", areaAcres)} acres", areaAcres, 1f..25f) {
                            areaAcres = it
                        }
                        SliderParam("Terrain Slope: ${String.format("%.1f", slopeDegrees)}°", slopeDegrees, 0f..18f) {
                            slopeDegrees = it
                        }
                        SliderParam("Water Availability: $waterScore%", waterScore.toFloat(), 15f..98f) {
                            waterScore = it.roundToInt()
                        }
                        SliderParam("Fertilizer Balance: $fertilizerIntensity%", fertilizerIntensity.toFloat(), 20f..100f) {
                            fertilizerIntensity = it.roundToInt()
                        }
                        SliderParam("Field Shipping Lead Time: $shippingLeadDays days", shippingLeadDays.toFloat(), 1f..7f) {
                            shippingLeadDays = it.roundToInt()
                        }

                        Button(
                            onClick = {
                                latestResult = onRunSimulation(
                                    cropName.ifBlank { "Paddy Rice" },
                                    areaAcres.toDouble(),
                                    slopeDegrees.toDouble(),
                                    soilType,
                                    waterScore,
                                    fertilizerIntensity,
                                    shippingLeadDays
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("run_3d_simulation_button")
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(state.strings.runSimulation)
                        }
                    }
                }
            }

            // Latest Simulation Outcome
            latestResult?.let { sim ->
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = ForestGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simulation Outcome: ${sim.cropName}", style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    "${sim.successRatePercent}% Success",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SimStat("Est. Yield", "${sim.projectedYieldTons} tons")
                                SimStat("Net Profit", "$${sim.estimatedProfitUsd}")
                                SimStat("Loss Avoided", "$${sim.lossAvoidedUsd}")
                            }
                            Text(sim.riskSummary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (state.simulations.size > 1) {
                item {
                    Text("Recent Virtual Tests", style = MaterialTheme.typography.titleMedium)
                }
                items(state.simulations.drop(1).take(4), key = { it.id }) { past ->
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${past.cropName} (${past.soilType})", style = MaterialTheme.typography.titleSmall)
                                Text("${past.areaAcres} ac • Profit $${past.estimatedProfitUsd}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "${past.successRatePercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun Geospatial3DLandCanvas(
    yawDegrees: Float,
    pitchDegrees: Float,
    slopeDegrees: Float,
    waterScore: Int,
    successRate: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.56f
        val yawRad = Math.toRadians(yawDegrees.toDouble())
        val pitchRad = Math.toRadians(pitchDegrees.toDouble())
        val cosY = cos(yawRad).toFloat()
        val sinY = sin(yawRad).toFloat()
        val sinP = sin(pitchRad).toFloat()
        val scale = size.minDimension * 0.088f

        fun project(x: Float, y: Float, z: Float): Offset {
            val rx = x * cosY - z * sinY
            val rz = x * sinY + z * cosY
            val sx = cx + rx * scale
            val sy = cy + (rz * sinP - y) * scale
            return Offset(sx, sy)
        }

        val gridN = 5
        val half = (gridN - 1) / 2f
        val cells = mutableListOf<Triple<Int, Int, Float>>()
        for (gx in 0 until gridN) {
            for (gz in 0 until gridN) {
                val wx = (gx - half) * 1.15f
                val wz = (gz - half) * 1.15f
                val depth = wx * sinY + wz * cosY
                cells.add(Triple(gx, gz, depth))
            }
        }
        // Painter's algorithm: draw farthest voxels first
        cells.sortedBy { it.third }.forEach { (gx, gz, _) ->
            val x0 = (gx - half) * 1.1f - 0.5f
            val x1 = x0 + 1.0f
            val z0 = (gz - half) * 1.1f - 0.5f
            val z1 = z0 + 1.0f

            val isChannel = (gz == 2)
            val elevation = ((gridN - 1 - gx) * (slopeDegrees / 14f)) + if (isChannel) 0.35f else 0.75f
            val base = -0.7f

            val topColor = when {
                isChannel && waterScore >= 40 -> WaterTeal
                isChannel -> AmberHarvest
                successRate >= 82 -> MeadowGreen
                successRate >= 65 -> SproutGreen
                else -> AmberHarvest
            }
            val sideColorA = EarthBrown
            val sideColorB = LoamBrown

            val t00 = project(x0, elevation, z0)
            val t10 = project(x1, elevation, z0)
            val t11 = project(x1, elevation, z1)
            val t01 = project(x0, elevation, z1)

            val b00 = project(x0, base, z0)
            val b10 = project(x1, base, z0)
            val b11 = project(x1, base, z1)
            val b01 = project(x0, base, z1)

            // Front-left soil stratum face
            val leftFace = Path().apply {
                moveTo(t01.x, t01.y); lineTo(t11.x, t11.y); lineTo(b11.x, b11.y); lineTo(b01.x, b01.y); close()
            }
            drawPath(leftFace, color = sideColorA)

            // Front-right soil stratum face
            val rightFace = Path().apply {
                moveTo(t10.x, t10.y); lineTo(t11.x, t11.y); lineTo(b11.x, b11.y); lineTo(b10.x, b10.y); close()
            }
            drawPath(rightFace, color = sideColorB)

            // Top vegetative / water surface face
            val topFace = Path().apply {
                moveTo(t00.x, t00.y); lineTo(t10.x, t10.y); lineTo(t11.x, t11.y); lineTo(t01.x, t01.y); close()
            }
            drawPath(topFace, color = topColor)
            drawPath(topFace, color = OffWhite.copy(alpha = 0.35f), style = Stroke(width = 1.6f, join = StrokeJoin.Round))
            // Keep unused b00 referenced cleanly for compiler
            if (gx == -1) drawCircle(Color.Transparent, 1f, b00)
        }
    }
}

@Composable
private fun SliderParam(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun SimStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreen)
    }
}
