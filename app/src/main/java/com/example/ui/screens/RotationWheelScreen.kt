package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.AgroAnalyticsEngine
import com.example.domain.RotationRecommendation
import com.example.ui.GrievousUiState
import com.example.ui.components.RotationWheelLogo
import com.example.ui.theme.AmberHarvest
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.OffWhite
import com.example.ui.theme.WaterTeal
import kotlin.math.roundToInt

private val SeasonsList = listOf("Monsoon (Kharif)", "Winter (Rabi)", "Summer (Zaid)", "Spring Transition")
private val MonthsList = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

@Composable
fun RotationWheelScreen(
    state: GrievousUiState,
    onApplyStrategy: (RotationRecommendation) -> Unit
) {
    var selectedSeason by remember { mutableStateOf(SeasonsList[1]) }
    var selectedMonth by remember { mutableStateOf("Nov") }
    var wheelAngleTarget by remember { mutableFloatStateOf(0f) }
    var activeRecommendationIndex by remember { mutableIntStateOf(0) }

    val recommendations = remember(selectedSeason, selectedMonth, state.farmer, state.selectedLand, state.selectedClimate) {
        AgroAnalyticsEngine.recommendRotations(
            season = selectedSeason,
            monthName = selectedMonth,
            farmer = state.farmer,
            land = state.selectedLand,
            climate = state.selectedClimate
        )
    }

    val activeRec = recommendations.getOrElse(activeRecommendationIndex) { recommendations.first() }
    val animatedWheelAngle by animateFloatAsState(
        targetValue = wheelAngleTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "rotation_wheel_spin"
    )

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
                    RotationWheelLogo(size = 30.dp, tint = ForestGreen, accent = AmberHarvest)
                    Column {
                        Text(state.strings.rotationWheelTitle, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Driven by season, month, soil & orbital climate data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Season & Month Selectors
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Season", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SeasonsList.forEach { season ->
                                FilterChip(
                                    selected = selectedSeason == season,
                                    onClick = {
                                        selectedSeason = season
                                        wheelAngleTarget += 90f
                                    },
                                    label = { Text(season) }
                                )
                            }
                        }

                        Text("Target Month", style = MaterialTheme.typography.labelLarge, color = EarthBrown)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MonthsList.forEach { month ->
                                FilterChip(
                                    selected = selectedMonth == month,
                                    onClick = {
                                        selectedMonth = month
                                        wheelAngleTarget += 30f
                                    },
                                    label = { Text(month) }
                                )
                            }
                        }

                        val climate = state.selectedClimate
                        Text(
                            text = "Climate Input: ${(climate?.temperatureC ?: 27.0).roundToInt()}°C • Wetness ${((climate?.soilWetnessRatio ?: 0.6) * 100).roundToInt()}% • Soil: ${state.selectedLand?.soilType ?: "Alluvial Loam"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Interactive Spinning Crop Rotation Wheel
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(230.dp)
                                .clickable {
                                    wheelAngleTarget += 180f
                                    activeRecommendationIndex = (activeRecommendationIndex + 1) % recommendations.size
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CropRotationWheelCanvas(
                                rotationDegrees = animatedWheelAngle,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = CircleShape,
                                color = OffWhite,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(84.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(6.dp)
                                ) {
                                    Text(
                                        text = "${activeRec.matchScorePct}%",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                    Text(
                                        text = "FIT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EarthBrown
                                    )
                                }
                            }
                        }

                        // 4 Quadrant Legend Pills matching the Wheel Colors
                        val wedgeColors = listOf(ForestGreen, AmberHarvest, EarthBrown, WaterTeal)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeRec.wheelSegments.take(4).forEachIndexed { idx, cropLabel ->
                                Surface(
                                    color = wedgeColors[idx % wedgeColors.size].copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Q${idx + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = wedgeColors[idx % wedgeColors.size]
                                        )
                                        Text(
                                            text = cropLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                wheelAngleTarget += 270f
                                activeRecommendationIndex = (activeRecommendationIndex + 1) % recommendations.size
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("spin_rotation_wheel_button")
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(state.strings.spinWheel)
                        }
                    }
                }
            }

            // Ranked Rotation Recommendations
            itemsIndexed(recommendations, key = { _, rec -> rec.id }) { index, rec ->
                val isSelected = index == activeRecommendationIndex
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(2.dp, ForestGreen)
                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            activeRecommendationIndex = index
                            wheelAngleTarget += 90f
                        }
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
                            Text(
                                text = rec.sequenceTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(color = ForestGreen, shape = RoundedCornerShape(50)) {
                                Text(
                                    text = "${rec.matchScorePct}% Fit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OffWhite,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("N2 Fix: +${rec.nitrogenRecoveryPct}%", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
                            Text("Water Eff: ${rec.waterEfficiencyPct}%", style = MaterialTheme.typography.labelMedium, color = WaterTeal)
                            Text("Profit Lift: +${rec.profitLiftPct}%", style = MaterialTheme.typography.labelMedium, color = EarthBrown)
                        }

                        Text(rec.rationale, style = MaterialTheme.typography.bodySmall)

                        Button(
                            onClick = { onApplyStrategy(rec) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("apply_rotation_${rec.id}")
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${state.strings.applyStrategy} (+15 Leaves)")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun CropRotationWheelCanvas(
    rotationDegrees: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val diameter = size.minDimension * 0.92f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val colors = listOf(ForestGreen, AmberHarvest, EarthBrown, WaterTeal)

        rotate(degrees = rotationDegrees) {
            for (i in 0 until 4) {
                drawArc(
                    color = colors[i],
                    startAngle = i * 90f,
                    sweepAngle = 86f,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize
                )
            }
            drawCircle(
                color = OffWhite,
                radius = diameter * 0.5f,
                center = center,
                style = Stroke(width = 6f)
            )
            // Subtle inner spoke ring
            drawCircle(
                color = MeadowGreen.copy(alpha = 0.25f),
                radius = diameter * 0.34f,
                center = center,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }
        // Top pointer triangle indicator
        val pointerY = topLeft.y - 6f
        val pointerPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x, pointerY + 18f)
            lineTo(center.x - 12f, pointerY - 2f)
            lineTo(center.x + 12f, pointerY - 2f)
            close()
        }
        drawPath(pointerPath, color = Color(0xFF33231A))
    }
}
