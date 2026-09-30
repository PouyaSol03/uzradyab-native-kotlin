package com.example.uzradyab.presentation.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Icons for the routing screens, drawn on a 24x24 grid with 2dp round strokes (same as the mockups).
 * Maneuver icons are real-world directions and are never mirrored for RTL.
 */
object NavigationIcons {

    /** Path data per maneuver; also used to draw the map markers on a Canvas. */
    fun maneuverPaths(kind: ManeuverKind): List<String> = when (kind) {
        ManeuverKind.DEPART -> listOf("M12 15V4", "M8 8l4-4 4 4", "M14.5 19a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0")
        ManeuverKind.ARRIVE -> listOf(
            "M12 21s-6-5.5-6-10.5a6 6 0 0 1 12 0C18 15.5 12 21 12 21z",
            "M14 10.5a2 2 0 1 1-4 0a2 2 0 1 1 4 0",
        )
        ManeuverKind.ROTARY -> listOf(
            "M16.5 11a4.5 4.5 0 1 1-9 0a4.5 4.5 0 1 1 9 0",
            "M12 15.5V21",
            "M15.2 7.8L19.5 3.5",
            "M15 3.5h4.5V8",
        )
        ManeuverKind.TURN_RIGHT -> listOf("M7 21v-8a4 4 0 0 1 4-4h9", "M16 5l4 4-4 4")
        ManeuverKind.TURN_LEFT -> listOf("M17 21v-8a4 4 0 0 0-4-4H4", "M8 5L4 9l4 4")
        ManeuverKind.SLIGHT_RIGHT -> listOf("M8 21v-6.5c0-1 .4-2 1.2-2.8L17.5 4", "M11.5 4h6v6")
        ManeuverKind.SLIGHT_LEFT -> listOf("M16 21v-6.5c0-1-.4-2-1.2-2.8L6.5 4", "M12.5 4h-6v6")
        ManeuverKind.STRAIGHT -> listOf("M12 21V4", "M6 10l6-6 6 6")
        ManeuverKind.UTURN -> listOf("M8 21V10a4 4 0 0 1 8 0v7", "M12 14l4 4 4-4")
    }

    private val maneuverCache = HashMap<ManeuverKind, ImageVector>()

    fun maneuver(kind: ManeuverKind): ImageVector =
        maneuverCache.getOrPut(kind) { stroke("maneuver_$kind", *maneuverPaths(kind).toTypedArray()) }

    /** Points right. Used for "back" in RTL and for "return to route". */
    val ArrowRight = stroke("arrow_right", "M5 12h14", "M13 6l6 6-6 6")
    val Close = stroke("close", "M6 6l12 12", "M18 6L6 18", width = 2.4f)
    val Check = stroke("check", "M5 12.5l4.5 4.5L19 7.5", width = 2.4f)
    val Share = stroke(
        "share",
        "M20.5 5a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0",
        "M8.5 12a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0",
        "M20.5 19a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0",
        "M8.2 10.8l7.6-4.4",
        "M8.2 13.2l7.6 4.4",
    )
    val StepList = stroke(
        "list",
        "M9 6h11", "M9 12h11", "M9 18h11",
        "M5.5 6a1 1 0 1 1-2 0a1 1 0 1 1 2 0",
        "M5.5 12a1 1 0 1 1-2 0a1 1 0 1 1 2 0",
        "M5.5 18a1 1 0 1 1-2 0a1 1 0 1 1 2 0",
    )
    val Clock = stroke("clock", "M21 12a9 9 0 1 1-18 0a9 9 0 1 1 18 0", "M12 7v5l3 2")
    val FitRoute = stroke("fit_route", "M4 9V4h5", "M20 9V4h-5", "M4 15v5h5", "M20 15v5h-5")
    val Route = stroke(
        "route",
        "M8.5 19a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0",
        "M20.5 5a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0",
        "M6 16.5V13a3 3 0 0 1 3-3h6a3 3 0 0 0 3-3V7.5",
    )
    val Rotary = maneuver(ManeuverKind.ROTARY)
    val VolumeOn = stroke("volume_on", "M4 9h3l5-4v14l-5-4H4z", "M16 9a4 4 0 0 1 0 6", "M18.5 6.5a7.5 7.5 0 0 1 0 11")
    val VolumeOff = stroke("volume_off", "M4 9h3l5-4v14l-5-4H4z", "M16 9.5l5 5", "M21 9.5l-5 5")
    val Car = stroke("car", "M3 13l2-6h14l2 6v5H3z", "M3 13h18", "M7 18v2", "M17 18v2")
    val Pin = stroke(
        "pin",
        "M12 21s-6-5.5-6-10.5a6 6 0 0 1 12 0C18 15.5 12 21 12 21z",
        "M14 10.5a2 2 0 1 1-4 0a2 2 0 1 1 4 0",
    )
    val Reroute = stroke("reroute", "M20 12a8 8 0 1 1-2.34-5.66", "M20 4v5h-5", width = 2.6f)

    /** Filled navigation arrow used on the start / return buttons. */
    val NavigationArrow: ImageVector = ImageVector.Builder("nav_arrow", 24.dp, 24.dp, 24f, 24f).apply {
        addPath(pathData = addPathNodes("M12 3l7 18-7-4-7 4z"), fill = SolidColor(Color.Black))
    }.build()

    /** Two-colored compass needle; draw with Image, not Icon, to keep the colors. */
    val Compass: ImageVector = ImageVector.Builder("compass", 24.dp, 24.dp, 24f, 24f).apply {
        addPath(pathData = addPathNodes("M12 3l4 9h-8z"), fill = SolidColor(Color(0xFFE5484D)))
        addPath(pathData = addPathNodes("M12 21l-4-9h8z"), fill = SolidColor(Color(0xFF9AA1AE)))
    }.build()

    private fun stroke(name: String, vararg paths: String, width: Float = 2f): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach { data ->
                addPath(
                    pathData = addPathNodes(data),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = width,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
