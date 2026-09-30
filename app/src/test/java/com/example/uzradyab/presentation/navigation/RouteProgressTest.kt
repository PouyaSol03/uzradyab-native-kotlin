package com.example.uzradyab.presentation.navigation

import com.example.uzradyab.domain.model.Maneuver
import com.example.uzradyab.domain.model.RouteDomainModel
import com.example.uzradyab.domain.model.RouteStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.geometry.LatLng

class RouteProgressTest {

    // An L-shaped route: ~1 km north, then ~1 km east.
    private val start = LatLng(35.700, 51.400)
    private val corner = LatLng(35.709, 51.400)
    private val end = LatLng(35.709, 51.411)

    private fun step(type: String, modifier: String, name: String, points: List<LatLng>, duration: Long) = RouteStep(
        instruction = "",
        streetName = name,
        distanceMeters = polylineLength(points).toLong(),
        durationSeconds = duration,
        distanceText = "",
        maneuver = Maneuver(type, modifier),
        startLocation = points.first(),
        bearingAfter = 0,
        rotaryExit = null,
        rotaryName = null,
        points = points,
    )

    private val route = RouteDomainModel(
        points = listOf(start, corner, end),
        distanceText = "",
        durationText = "",
        summaryText = "",
        durationSeconds = 200,
        steps = listOf(
            step("depart", "", "شمالی", listOf(start, corner), 100),
            step("turn", "right", "شرقی", listOf(corner, end), 100),
            step("arrive", "", "مقصد", listOf(end), 0),
        ),
    )
    private val line = RouteLine(route.points)
    private val guidance = buildGuidance(route, "خودرو")

    @Test
    fun projection_onTheRoute_hasNoOffset() {
        val halfway = LatLng(35.7045, 51.400)
        val projection = line.project(halfway)
        assertNotNull(projection)
        assertTrue(projection!!.offRoute < 1.0)
        assertEquals(line.length / 2 / 2, projection.along, 5.0)
    }

    @Test
    fun projection_farFromTheRoute_reportsTheOffset() {
        val aside = LatLng(35.7045, 51.401) // ~90 m east of the first leg
        val projection = line.project(aside)!!
        assertEquals(90.0, projection.offRoute, 5.0)
    }

    @Test
    fun progress_pointsAtTheNextTurn() {
        val halfway = LatLng(35.7045, 51.400)
        val progress = computeProgress(line, guidance, 200.0, line.project(halfway)!!)

        assertEquals(1, progress.nextStepIndex)
        assertEquals(ManeuverKind.TURN_RIGHT, guidance[progress.nextStepIndex!!].kind)
        assertEquals(500.0, progress.distanceToNext, 10.0)
        assertEquals(line.length - 500.0, progress.remainingMeters, 10.0)
        assertEquals(150.0, progress.remainingSeconds, 2.0)
    }

    @Test
    fun split_endsAndStartsAtTheProjectedPoint() {
        val projection = line.project(LatLng(35.7045, 51.400))!!
        val (traveled, remaining) = line.split(projection)
        assertEquals(start, traveled.first())
        assertEquals(projection.point, traveled.last())
        assertEquals(projection.point, remaining.first())
        assertEquals(end, remaining.last())
    }

    @Test
    fun relativeSide_usesTheHeading() {
        val user = LatLng(35.700, 51.400)
        val east = LatLng(35.700, 51.401)
        assertEquals("در سمت راست شما", relativeSide(0.0, user, east))
        assertEquals("در سمت چپ شما", relativeSide(180.0, user, east))
        assertEquals("روبه‌روی شما", relativeSide(90.0, user, east))
    }
}
