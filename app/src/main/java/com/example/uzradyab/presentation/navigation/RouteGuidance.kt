package com.example.uzradyab.presentation.navigation

import com.example.uzradyab.domain.model.RouteDomainModel
import com.example.uzradyab.domain.model.RouteStep
import org.maplibre.android.geometry.LatLng

/** Icon family for a maneuver. Real-world directions: never mirrored for RTL. */
enum class ManeuverKind {
    DEPART, ARRIVE, ROTARY,
    TURN_RIGHT, TURN_LEFT,
    SLIGHT_RIGHT, SLIGHT_LEFT,
    STRAIGHT, UTURN
}

fun maneuverKindOf(type: String, modifier: String): ManeuverKind {
    when (type.trim().lowercase()) {
        "depart" -> return ManeuverKind.DEPART
        "arrive" -> return ManeuverKind.ARRIVE
        "rotary", "roundabout" -> return ManeuverKind.ROTARY
    }
    return when (modifier.trim().lowercase()) {
        "right", "sharp right" -> ManeuverKind.TURN_RIGHT
        "left", "sharp left" -> ManeuverKind.TURN_LEFT
        "slight right" -> ManeuverKind.SLIGHT_RIGHT
        "slight left" -> ManeuverKind.SLIGHT_LEFT
        "uturn" -> ManeuverKind.UTURN
        else -> ManeuverKind.STRAIGHT
    }
}

/**
 * One row of guidance: a single maneuver, with each rotary already merged with its exit.
 * [startAlong] is the distance in meters from the route start to this maneuver.
 */
data class GuidanceStep(
    val kind: ManeuverKind,
    /** Row title and banner road line: road name or roundabout name. */
    val title: String,
    /** Row subtitle: instruction without the road name. */
    val subtitle: String,
    /** Small line in the instruction banner. */
    val bannerInstruction: String,
    /** Text in the "سپس" chip when this step comes after the banner step. */
    val chipText: String,
    val rotaryExit: Int?,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val location: LatLng,
    /** Where the rotary is left; null for other steps. */
    val exitLocation: LatLng?,
    val startAlong: Double,
    /** Midpoint of the step geometry, used for road labels in overview. */
    val labelPoint: LatLng?,
    val roadName: String,
)

private val ROTARY_TYPES = setOf("rotary", "roundabout")
private val EXIT_ROTARY_TYPES = setOf("exit rotary", "exit roundabout")

/**
 * Builds the guidance rows for a route. [destinationLabel] is shown for the arrive step,
 * e.g. "خودروی 114".
 */
fun buildGuidance(route: RouteDomainModel, destinationLabel: String): List<GuidanceStep> {
    val steps = route.steps
    if (steps.isEmpty()) return emptyList()

    // Distance from the route start to the beginning of each raw step, measured on the
    // same joined geometry that is drawn and used for progress.
    val startAlong = DoubleArray(steps.size)
    var along = 0.0
    steps.forEachIndexed { index, step ->
        startAlong[index] = along
        val geometric = polylineLength(step.points)
        along += if (step.points.size >= 2) geometric else step.distanceMeters.toDouble()
    }

    val result = mutableListOf<GuidanceStep>()
    var i = 0
    while (i < steps.size) {
        val step = steps[i]
        val type = step.maneuver.type.trim().lowercase()
        val next = steps.getOrNull(i + 1)
        val nextType = next?.maneuver?.type?.trim()?.lowercase()

        if (type in ROTARY_TYPES) {
            val mergedExit = next != null && nextType in EXIT_ROTARY_TYPES
            val exit = step.rotaryExit ?: next?.rotaryExit
            val rotaryTitle = step.rotaryName?.takeIf { it.isNotBlank() } ?: "میدان"
            val roadAfter = step.streetName.ifBlank { next?.streetName.orEmpty() }
            val exitWord = exit?.let { NavigationFormat.ordinal(it) }
            val subtitle = when {
                exitWord != null && roadAfter.isNotBlank() -> "از خروجی $exitWord به $roadAfter"
                exitWord != null -> "از خروجی $exitWord خارج شوید"
                else -> stripRoadName(step.instruction, step.streetName)
            }
            val bannerInstruction = if (exitWord != null && roadAfter.isNotBlank()) "$subtitle بروید" else subtitle
            val chip = if (exitWord != null) "$rotaryTitle · خروجی $exitWord" else rotaryTitle
            result += GuidanceStep(
                kind = ManeuverKind.ROTARY,
                title = rotaryTitle,
                subtitle = subtitle,
                bannerInstruction = bannerInstruction,
                chipText = chip,
                rotaryExit = exit,
                distanceMeters = step.distanceMeters.toDouble() + if (mergedExit) next!!.distanceMeters.toDouble() else 0.0,
                durationSeconds = step.durationSeconds.toDouble() + if (mergedExit) next!!.durationSeconds.toDouble() else 0.0,
                location = step.startLocation,
                exitLocation = if (mergedExit) next!!.startLocation else null,
                startAlong = startAlong[i],
                labelPoint = null,
                roadName = roadAfter,
            )
            i += if (mergedExit) 2 else 1
            continue
        }

        val kind = maneuverKindOf(step.maneuver.type, step.maneuver.modifier)
        result += when (kind) {
            ManeuverKind.DEPART -> {
                val direction = step.bearingAfter?.let { "به سمت ${NavigationFormat.compassDirection(it)} حرکت کنید" }
                    ?: stripRoadName(step.instruction, step.streetName)
                val title = if (step.streetName.isNotBlank()) "شروع از ${step.streetName}" else "شروع مسیر"
                simpleStep(step, kind, title, direction, startAlong[i])
            }
            ManeuverKind.ARRIVE -> {
                val subtitle = listOf(destinationLabel, step.streetName).filter { it.isNotBlank() }.joinToString(" · ")
                simpleStep(step, kind, "رسیدن به مقصد", subtitle, startAlong[i]).copy(
                    bannerInstruction = step.instruction.ifBlank { "در مقصد قرار دارید" },
                    chipText = "مقصد",
                )
            }
            else -> {
                val title = step.streetName.ifBlank { step.instruction.trim() }
                val subtitle = if (step.streetName.isBlank()) "" else stripRoadName(step.instruction, step.streetName)
                simpleStep(step, kind, title, subtitle, startAlong[i])
            }
        }
        i++
    }
    return result
}

private fun simpleStep(step: RouteStep, kind: ManeuverKind, title: String, subtitle: String, startAlong: Double) =
    GuidanceStep(
        kind = kind,
        title = title,
        subtitle = subtitle,
        bannerInstruction = subtitle,
        chipText = title,
        rotaryExit = null,
        distanceMeters = step.distanceMeters.toDouble(),
        durationSeconds = step.durationSeconds.toDouble(),
        location = step.startLocation,
        exitLocation = null,
        startAlong = startAlong,
        labelPoint = step.points.takeIf { it.size >= 2 }?.get(step.points.size / 2),
        roadName = step.streetName,
    )

/**
 * Removes the road name from an instruction because the row title already shows it:
 * "به سمت کمیل، به راست بپیچید" -> "به راست بپیچید",
 * "در امیر قلی به مسیر خود ادامه دهید" -> "به مسیر خود ادامه دهید",
 * "به چپ بپیچید و وارد کاسبی شوید" -> "به چپ بپیچید".
 */
fun stripRoadName(instruction: String, roadName: String): String {
    val text = instruction.trim().replace(Regex("\\s+"), " ")
    val name = roadName.trim().replace(Regex("\\s+"), " ")
    if (name.isEmpty()) return text
    var result = text
    listOf("به سمت $name، ", "به سمت $name ,", "به سمت $name, ", "در $name ").forEach { prefix ->
        if (result.startsWith(prefix)) result = result.removePrefix(prefix)
    }
    result = result.removeSuffix(" و وارد $name شوید")
    result = result.trim()
    return result.ifEmpty { text }
}

/** Number of roundabouts on the route. */
fun List<GuidanceStep>.rotaryCount(): Int = count { it.kind == ManeuverKind.ROTARY }

/** Road pills for the overview map: the longest named roads, at most [max]. */
fun List<GuidanceStep>.overviewLabels(max: Int = 4): List<Pair<String, LatLng>> =
    asSequence()
        .filter { it.kind != ManeuverKind.DEPART && it.kind != ManeuverKind.ARRIVE && it.kind != ManeuverKind.ROTARY }
        .filter { it.roadName.isNotBlank() && it.labelPoint != null }
        .sortedByDescending { it.distanceMeters }
        .distinctBy { it.roadName }
        .take(max)
        .map { it.roadName to it.labelPoint!! }
        .toList()
