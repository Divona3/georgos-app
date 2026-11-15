package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberHarvest
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MeadowGreen
import com.example.ui.theme.OffWhite
import com.example.ui.theme.SoftBeige
import com.example.ui.theme.SproutGreen
import kotlin.math.cos
import kotlin.math.sin

/**
 * Module 1 Logo: Farmer & Land Digital Passport Emblem
 */
@Composable
fun PassportBadgeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ForestGreen
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.14f, h * 0.1f),
            size = Size(w * 0.72f, h * 0.8f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
            style = Stroke(width = stroke)
        )
        // Farmer head
        drawCircle(
            color = tint,
            radius = w * 0.13f,
            center = Offset(w * 0.5f, h * 0.36f)
        )
        // Farmer shoulders + field horizon
        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.68f)
            quadraticTo(w * 0.5f, h * 0.48f, w * 0.7f, h * 0.68f)
            moveTo(w * 0.24f, h * 0.78f)
            lineTo(w * 0.76f, h * 0.78f)
        }
        drawPath(path, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

/**
 * Module 2 Logo: Satellite-Backed Agro Dashboard Gauge
 */
@Composable
fun DashboardTelemetryLogo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ForestGreen
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.085f
        drawArc(
            color = tint,
            startAngle = 150f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(w * 0.1f, h * 0.15f),
            size = Size(w * 0.8f, h * 0.8f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Rising crop/price bars inside
        drawLine(tint, Offset(w * 0.33f, h * 0.72f), Offset(w * 0.33f, h * 0.56f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.50f, h * 0.72f), Offset(w * 0.50f, h * 0.42f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.67f, h * 0.72f), Offset(w * 0.67f, h * 0.49f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/**
 * Module 3 Logo: Farmer + Calendar Emblem
 */
@Composable
fun FarmerCalendarLogo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    primaryColor: Color = ForestGreen,
    accentColor: Color = EarthBrown
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.08f
        // Calendar frame
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(w * 0.1f, h * 0.18f),
            size = Size(w * 0.8f, h * 0.72f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = stroke)
        )
        // Calendar binding rings
        drawLine(primaryColor, Offset(w * 0.32f, h * 0.09f), Offset(w * 0.32f, h * 0.25f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(primaryColor, Offset(w * 0.68f, h * 0.09f), Offset(w * 0.68f, h * 0.25f), strokeWidth = stroke, cap = StrokeCap.Round)
        // Farmer with straw hat inside calendar
        drawLine(accentColor, Offset(w * 0.32f, h * 0.44f), Offset(w * 0.68f, h * 0.44f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawArc(
            color = accentColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.38f, h * 0.31f),
            size = Size(w * 0.24f, h * 0.22f)
        )
        drawCircle(primaryColor, radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.55f))
        val shoulders = Path().apply {
            moveTo(w * 0.31f, h * 0.80f)
            quadraticTo(w * 0.5f, h * 0.61f, w * 0.69f, h * 0.80f)
        }
        drawPath(shoulders, color = primaryColor, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

/**
 * Module 4 Logo: Twin Botanical Leaves Emblem
 */
@Composable
fun LeavesLogo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    primaryColor: Color = ForestGreen,
    secondaryColor: Color = SproutGreen
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.07f

        val mainLeaf = Path().apply {
            moveTo(w * 0.22f, h * 0.82f)
            cubicTo(w * 0.16f, h * 0.35f, w * 0.52f, h * 0.12f, w * 0.84f, h * 0.16f)
            cubicTo(w * 0.86f, h * 0.52f, w * 0.60f, h * 0.82f, w * 0.22f, h * 0.82f)
            close()
        }
        drawPath(mainLeaf, color = primaryColor)

        val smallLeaf = Path().apply {
            moveTo(w * 0.22f, h * 0.82f)
            cubicTo(w * 0.10f, h * 0.66f, w * 0.12f, h * 0.42f, w * 0.34f, h * 0.38f)
            cubicTo(w * 0.34f, h * 0.60f, w * 0.30f, h * 0.74f, w * 0.22f, h * 0.82f)
            close()
        }
        drawPath(smallLeaf, color = secondaryColor)

        // Stem & leaf vein
        drawLine(
            color = OffWhite,
            start = Offset(w * 0.24f, h * 0.80f),
            end = Offset(w * 0.72f, h * 0.28f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Module 5 Logo: 3D Isometric Land Structure Emblem
 */
@Composable
fun Land3DStructureLogo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    topColor: Color = MeadowGreen,
    leftSoilColor: Color = EarthBrown,
    rightSoilColor: Color = AmberHarvest
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val topCenter = Offset(w * 0.5f, h * 0.14f)
        val midRight = Offset(w * 0.88f, h * 0.36f)
        val midCenter = Offset(w * 0.5f, h * 0.58f)
        val midLeft = Offset(w * 0.12f, h * 0.36f)
        val botLeft = Offset(w * 0.12f, h * 0.64f)
        val botCenter = Offset(w * 0.5f, h * 0.86f)
        val botRight = Offset(w * 0.88f, h * 0.64f)

        val topFace = Path().apply {
            moveTo(topCenter.x, topCenter.y)
            lineTo(midRight.x, midRight.y)
            lineTo(midCenter.x, midCenter.y)
            lineTo(midLeft.x, midLeft.y)
            close()
        }
        drawPath(topFace, color = topColor)

        val leftFace = Path().apply {
            moveTo(midLeft.x, midLeft.y)
            lineTo(midCenter.x, midCenter.y)
            lineTo(botCenter.x, botCenter.y)
            lineTo(botLeft.x, botLeft.y)
            close()
        }
        drawPath(leftFace, color = leftSoilColor)

        val rightFace = Path().apply {
            moveTo(midCenter.x, midCenter.y)
            lineTo(midRight.x, midRight.y)
            lineTo(botRight.x, botRight.y)
            lineTo(botCenter.x, botCenter.y)
            close()
        }
        drawPath(rightFace, color = rightSoilColor)

        val wireframe = Path().apply {
            moveTo(topCenter.x, topCenter.y)
            lineTo(midRight.x, midRight.y)
            lineTo(botRight.x, botRight.y)
            lineTo(botCenter.x, botCenter.y)
            lineTo(botLeft.x, botLeft.y)
            lineTo(midLeft.x, midLeft.y)
            close()
        }
        drawPath(
            wireframe,
            color = OffWhite.copy(alpha = 0.7f),
            style = Stroke(width = w * 0.04f, join = StrokeJoin.Round)
        )
    }
}

/**
 * Module 6 Logo: Segmented Crop Rotation Wheel Emblem
 */
@Composable
fun RotationWheelLogo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = ForestGreen,
    accent: Color = AmberHarvest
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val outerR = w * 0.42f
        val innerR = w * 0.14f
        val stroke = w * 0.08f

        drawCircle(color = tint, radius = outerR, center = center, style = Stroke(width = stroke))
        drawCircle(color = accent, radius = innerR, center = center)

        // 4 seasonal spokes
        for (i in 0 until 4) {
            val angleRad = Math.toRadians((i * 90.0) + 45.0)
            val start = Offset(
                center.x + (innerR * cos(angleRad)).toFloat(),
                center.y + (innerR * sin(angleRad)).toFloat()
            )
            val end = Offset(
                center.x + (outerR * cos(angleRad)).toFloat(),
                center.y + (outerR * sin(angleRad)).toFloat()
            )
            drawLine(color = tint, start = start, end = end, strokeWidth = stroke * 0.85f, cap = StrokeCap.Round)
        }
        // Subtle outer seasonal dots
        drawCircle(color = SoftBeige, radius = w * 0.05f, center = center)
    }
}
