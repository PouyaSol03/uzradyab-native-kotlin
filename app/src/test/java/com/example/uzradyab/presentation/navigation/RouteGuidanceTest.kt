package com.example.uzradyab.presentation.navigation

import com.example.uzradyab.domain.model.Maneuver
import com.example.uzradyab.domain.model.RouteDomainModel
import com.example.uzradyab.domain.model.RouteStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.maplibre.android.geometry.LatLng

class RouteGuidanceTest {

    private fun step(
        type: String,
        modifier: String = "",
        name: String = "",
        instruction: String = "",
        distance: Long = 100,
        duration: Long = 10,
        exit: Int? = null,
        rotaryName: String? = null,
        bearingAfter: Int? = null,
        points: List<LatLng> = emptyList(),
        start: LatLng = points.firstOrNull() ?: LatLng(35.0, 51.0),
    ) = RouteStep(
        instruction = instruction,
        streetName = name,
        distanceMeters = distance,
        durationSeconds = duration,
        distanceText = "",
        maneuver = Maneuver(type, modifier),
        startLocation = start,
        bearingAfter = bearingAfter,
        rotaryExit = exit,
        rotaryName = rotaryName,
        points = points,
    )

    private fun route(vararg steps: RouteStep) = RouteDomainModel(
        points = steps.flatMap { it.points },
        distanceText = "",
        durationText = "",
        summaryText = "",
        steps = steps.toList(),
    )

    @Test
    fun maneuverKind_followsTheHandoffTable() {
        assertEquals(ManeuverKind.DEPART, maneuverKindOf("depart", ""))
        assertEquals(ManeuverKind.ARRIVE, maneuverKindOf("arrive", ""))
        assertEquals(ManeuverKind.ROTARY, maneuverKindOf("rotary", "right"))
        assertEquals(ManeuverKind.TURN_RIGHT, maneuverKindOf("turn", "sharp right"))
        assertEquals(ManeuverKind.TURN_LEFT, maneuverKindOf("end of road", "left"))
        assertEquals(ManeuverKind.SLIGHT_RIGHT, maneuverKindOf("fork", "slight right"))
        assertEquals(ManeuverKind.SLIGHT_LEFT, maneuverKindOf("off ramp", "slight left"))
        assertEquals(ManeuverKind.STRAIGHT, maneuverKindOf("new name", "straight"))
        assertEquals(ManeuverKind.STRAIGHT, maneuverKindOf("continue", ""))
    }

    @Test
    fun stripRoadName_removesTheRepeatedRoad() {
        assertEquals("به راست بپیچید", stripRoadName("به سمت کمیل، به راست بپیچید", "کمیل"))
        assertEquals("به مسیر خود ادامه دهید", stripRoadName("در امیر قلی به مسیر خود ادامه دهید", "امیر قلی"))
        assertEquals("به چپ بپیچید", stripRoadName("به چپ بپیچید و وارد کاسبی شوید", "کاسبی"))
        assertEquals("به راست برانید", stripRoadName("به راست برانید  و وارد جلال آل احمد، شرق شوید", "جلال آل احمد، شرق"))
        assertEquals("به مسیر خود ادامه دهید", stripRoadName("به مسیر خود ادامه دهید", ""))
    }

    @Test
    fun rotary_isMergedWithItsExit() {
        val guidance = buildGuidance(
            route(
                step("depart", name = "شهید عرب", bearingAfter = 353, distance = 548),
                step("rotary", "right", name = "سید عابدین نوری", exit = 2, rotaryName = "میدان بریانک", distance = 71, duration = 17),
                step("exit rotary", "slight left", instruction = "به مسیر خود ادامه دهید", exit = 2, distance = 56, duration = 14),
                step("turn", "right", name = "دعوتی", instruction = "به سمت دعوتی، به راست بپیچید"),
                step("arrive", name = "غفاری", instruction = "در مقصد قرار دارید", distance = 0),
            ),
            destinationLabel = "خودروی 114",
        )

        assertEquals(4, guidance.size)
        assertEquals("شروع از شهید عرب", guidance[0].title)
        assertEquals("به سمت شمال حرکت کنید", guidance[0].subtitle)

        val rotary = guidance[1]
        assertEquals(ManeuverKind.ROTARY, rotary.kind)
        assertEquals("میدان بریانک", rotary.title)
        assertEquals("از خروجی دوم به سید عابدین نوری", rotary.subtitle)
        assertEquals("از خروجی دوم به سید عابدین نوری بروید", rotary.bannerInstruction)
        assertEquals("میدان بریانک · خروجی دوم", rotary.chipText)
        assertEquals(127.0, rotary.distanceMeters, 0.001)
        assertEquals(31.0, rotary.durationSeconds, 0.001)

        assertEquals("دعوتی", guidance[2].title)
        assertEquals("به راست بپیچید", guidance[2].subtitle)
        assertEquals(ManeuverKind.TURN_RIGHT, guidance[2].kind)

        assertEquals("رسیدن به مقصد", guidance[3].title)
        assertEquals("خودروی 114 · غفاری", guidance[3].subtitle)
        assertEquals(1, guidance.rotaryCount())
    }

    @Test
    fun rotaryWithoutExitStep_isKept() {
        val guidance = buildGuidance(
            route(step("rotary", "right", exit = 3, rotaryName = "میدان ولیعصر", name = "ولیعصر")),
            destinationLabel = "",
        )
        assertEquals(1, guidance.size)
        assertEquals("از خروجی سوم به ولیعصر", guidance[0].subtitle)
        assertNull(guidance[0].exitLocation)
    }
}
