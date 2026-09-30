package com.example.uzradyab.presentation.navigation

import org.maplibre.android.geometry.LatLng
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_M = 6_371_000.0

fun distanceMeters(a: LatLng, b: LatLng): Double {
    val lat1 = a.latitude.toRadians()
    val lat2 = b.latitude.toRadians()
    val dLat = lat2 - lat1
    val dLng = (b.longitude - a.longitude).toRadians()
    val h = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLng / 2) * sin(dLng / 2)
    return 2 * EARTH_RADIUS_M * asin(sqrt(h.coerceIn(0.0, 1.0)))
}

/** Initial bearing from [a] to [b], 0..360 degrees. */
fun bearingDegrees(a: LatLng, b: LatLng): Double {
    val lat1 = a.latitude.toRadians()
    val lat2 = b.latitude.toRadians()
    val dLng = (b.longitude - a.longitude).toRadians()
    val y = sin(dLng) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
    return (atan2(y, x) * 180 / PI + 360) % 360
}

fun polylineLength(points: List<LatLng>): Double {
    var total = 0.0
    for (i in 1 until points.size) total += distanceMeters(points[i - 1], points[i])
    return total
}

private fun Double.toRadians() = this * PI / 180

/** Where a GPS fix lands on the route. */
data class RouteProjection(
    /** Meters from the route start to the projected point. */
    val along: Double,
    /** Meters between the fix and the route. */
    val offRoute: Double,
    val point: LatLng,
    val segmentIndex: Int,
)

/** A route polyline with cumulative distances, for projecting GPS fixes onto it. */
class RouteLine(val points: List<LatLng>) {

    private val cumulative = DoubleArray(points.size).also { acc ->
        for (i in 1 until points.size) acc[i] = acc[i - 1] + distanceMeters(points[i - 1], points[i])
    }

    val length: Double get() = cumulative.lastOrNull() ?: 0.0

    /**
     * Projects [location] onto the line. With [hintAlong], segments near the previous position
     * are preferred, so a road that passes close to itself does not make the progress jump.
     */
    fun project(location: LatLng, hintAlong: Double? = null): RouteProjection? {
        if (points.size < 2) return null
        if (hintAlong != null) {
            val near = projectRange(location, hintAlong - 100.0, hintAlong + 1_000.0)
            if (near != null && near.offRoute <= 50.0) return near
        }
        return projectRange(location, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
    }

    private fun projectRange(location: LatLng, fromAlong: Double, toAlong: Double): RouteProjection? {
        var best: RouteProjection? = null
        val cosLat = cos(location.latitude.toRadians())
        for (i in 0 until points.size - 1) {
            if (cumulative[i + 1] < fromAlong || cumulative[i] > toAlong) continue
            val a = points[i]
            val b = points[i + 1]
            // Local flat projection around the fix, in meters
            val ax = (a.longitude - location.longitude).toRadians() * cosLat * EARTH_RADIUS_M
            val ay = (a.latitude - location.latitude).toRadians() * EARTH_RADIUS_M
            val bx = (b.longitude - location.longitude).toRadians() * cosLat * EARTH_RADIUS_M
            val by = (b.latitude - location.latitude).toRadians() * EARTH_RADIUS_M
            val dx = bx - ax
            val dy = by - ay
            val lengthSq = dx * dx + dy * dy
            val t = if (lengthSq == 0.0) 0.0 else ((-ax * dx - ay * dy) / lengthSq).coerceIn(0.0, 1.0)
            val px = ax + t * dx
            val py = ay + t * dy
            val offRoute = sqrt(px * px + py * py)
            if (best == null || offRoute < best.offRoute) {
                val point = LatLng(
                    a.latitude + t * (b.latitude - a.latitude),
                    a.longitude + t * (b.longitude - a.longitude),
                )
                best = RouteProjection(
                    along = cumulative[i] + t * (cumulative[i + 1] - cumulative[i]),
                    offRoute = offRoute,
                    point = point,
                    segmentIndex = i,
                )
            }
        }
        return best
    }

    /** Splits the line at [projection] into the driven part and the part still ahead. */
    fun split(projection: RouteProjection): Pair<List<LatLng>, List<LatLng>> {
        val index = projection.segmentIndex.coerceIn(0, points.size - 2)
        val traveled = points.subList(0, index + 1) + projection.point
        val remaining = listOf(projection.point) + points.subList(index + 1, points.size)
        return traveled to remaining
    }

    /** Direction of travel at [along], 0..360 degrees. */
    fun bearingAt(along: Double): Double {
        if (points.size < 2) return 0.0
        var i = 0
        while (i < points.size - 2 && cumulative[i + 1] < along) i++
        return bearingDegrees(points[i], points[i + 1])
    }
}

/** Live navigation numbers for the banner and the bottom bar. */
data class RouteProgress(
    val projection: RouteProjection,
    /** Index in the guidance list of the maneuver shown in the banner; null past the last one. */
    val nextStepIndex: Int?,
    val distanceToNext: Double,
    val remainingMeters: Double,
    val remainingSeconds: Double,
)

fun computeProgress(
    line: RouteLine,
    guidance: List<GuidanceStep>,
    routeDurationSeconds: Double,
    projection: RouteProjection,
): RouteProgress {
    val along = projection.along
    val remainingMeters = (line.length - along).coerceAtLeast(0.0)

    if (guidance.isEmpty()) {
        val fraction = if (line.length > 0) remainingMeters / line.length else 0.0
        return RouteProgress(projection, null, remainingMeters, remainingMeters, routeDurationSeconds * fraction)
    }

    // The step we are driving on is the last one that started behind us.
    var current = 0
    for (i in guidance.indices) {
        if (guidance[i].startAlong <= along + 1.0) current = i else break
    }
    val next = (current + 1).takeIf { it < guidance.size }
    val nextStart = next?.let { guidance[it].startAlong } ?: line.length
    val distanceToNext = (nextStart - along).coerceAtLeast(0.0)

    val currentLength = (nextStart - guidance[current].startAlong).coerceAtLeast(1.0)
    val currentFraction = (distanceToNext / currentLength).coerceIn(0.0, 1.0)
    val stepSeconds = guidance[current].durationSeconds * currentFraction +
        guidance.drop(current + 1).sumOf { it.durationSeconds }
    val remainingSeconds = if (guidance.sumOf { it.durationSeconds } > 0.0) {
        stepSeconds
    } else if (line.length > 0) {
        routeDurationSeconds * remainingMeters / line.length
    } else {
        0.0
    }

    return RouteProgress(projection, next, distanceToNext, remainingMeters, remainingSeconds)
}

/** Which side of the driver the car is on: "در سمت راست شما", "روبه‌روی شما", ... */
fun relativeSide(userHeading: Double?, user: LatLng, target: LatLng): String {
    if (userHeading == null || distanceMeters(user, target) < 5.0) return "در نزدیکی شما"
    val delta = ((bearingDegrees(user, target) - userHeading + 540) % 360) - 180
    return when {
        delta in -30.0..30.0 -> "روبه‌روی شما"
        delta > 30.0 && delta < 150.0 -> "در سمت راست شما"
        delta < -30.0 && delta > -150.0 -> "در سمت چپ شما"
        else -> "پشت سر شما"
    }
}
