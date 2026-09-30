package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.NeshanApi
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.NeshanRepository
import com.google.gson.JsonObject
import android.util.Log
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject
import javax.inject.Singleton

import com.example.uzradyab.domain.repository.GeocoderRepository

@Singleton
class NeshanRepositoryImpl @Inject constructor(
    private val api: NeshanApi
) : NeshanRepository {

    override suspend fun getSnappedRoute(rawPoints: List<Position>): Result<List<LatLng>> = runCatching {
        if (rawPoints.isEmpty()) {
            return@runCatching emptyList()
        }

        val allSnappedPoints = mutableListOf<LatLng>()
        
        // Filter out consecutive duplicate points (stationary vehicle) to prevent Neshan 404 "No Match!" errors
        val distinctPoints = mutableListOf<Position>()
        rawPoints.forEach { p ->
            val last = distinctPoints.lastOrNull()
            if (last == null || last.latitude != p.latitude || last.longitude != p.longitude) {
                distinctPoints.add(p)
            }
        }
        
        // PERFORMANCE OPTIMIZATION: 
        // If the user requests a huge amount of history (e.g. 1 month = 20,000 points), 
        // calling Neshan API for 400 chunks would take over a minute and likely hit rate limits.
        // For massive histories, we just bypass Map Matching and show the raw points instantly.
        if (distinctPoints.size > 2000) {
            return@runCatching distinctPoints.map { LatLng(it.latitude, it.longitude) }
        }

        val chunkSize = 50
        val chunks = distinctPoints.chunked(chunkSize)

        chunks.forEachIndexed { index, chunk ->
            if (chunk.size == 1) {
                allSnappedPoints.add(LatLng(chunk[0].latitude, chunk[0].longitude))
                return@forEachIndexed
            }
            
            val pathString = chunk.joinToString("|") { "${it.latitude},${it.longitude}" }
            try {
                val response = api.getMapMatching(pathString)
                val parsedCoords = parseNeshanResponse(response)
                
                if (parsedCoords.isNotEmpty()) {
                    allSnappedPoints.addAll(parsedCoords)
                } else {
                    allSnappedPoints.addAll(chunk.map { LatLng(it.latitude, it.longitude) })
                }
            } catch (e: Exception) {
                e.printStackTrace()
                allSnappedPoints.addAll(chunk.map { LatLng(it.latitude, it.longitude) })
            }
        }
        
        allSnappedPoints
    }
    
    private fun parseNeshanResponse(response: JsonObject): List<LatLng> {
        val list = mutableListOf<LatLng>()
        try {
            if (response.has("snappedPoints")) {
                val points = response.getAsJsonArray("snappedPoints")
                for (i in 0 until points.size()) {
                    val pt = points.get(i).asJsonObject
                    if (pt.has("location")) {
                        val loc = pt.getAsJsonObject("location")
                        if (loc.has("latitude") && loc.has("longitude")) {
                            list.add(LatLng(loc.get("latitude").asDouble, loc.get("longitude").asDouble))
                        }
                    }
                }
                if (list.isNotEmpty()) return list
            }
            
            // Fallback for other standard formats (e.g. snapped_waypoints with array)
            if (response.has("snapped_waypoints")) {
                val waypoints = response.getAsJsonArray("snapped_waypoints")
                for (i in 0 until waypoints.size()) {
                    val wp = waypoints.get(i).asJsonObject
                    if (wp.has("location")) {
                        val loc = wp.getAsJsonArray("location")
                        if (loc.size() >= 2) {
                            list.add(LatLng(loc.get(1).asDouble, loc.get(0).asDouble)) // lat, lng
                        }
                    }
                }
                if (list.isNotEmpty()) return list
            }
            
            // Check for GeoJSON format (geometry -> coordinates)
            if (response.has("geometry")) {
                val geom = response.getAsJsonObject("geometry")
                if (geom.has("coordinates")) {
                    val coords = geom.getAsJsonArray("coordinates")
                    for (i in 0 until coords.size()) {
                        val point = coords.get(i).asJsonArray
                        if (point.size() >= 2) {
                            list.add(LatLng(point.get(1).asDouble, point.get(0).asDouble))
                        }
                    }
                    if (list.isNotEmpty()) return list
                }
            }
            
            // Check for matchings format (OSRM Map Matching)
            if (response.has("matchings")) {
                val matchings = response.getAsJsonArray("matchings")
                for (i in 0 until matchings.size()) {
                    val match = matchings.get(i).asJsonObject
                    if (match.has("geometry")) {
                        val geom = match.get("geometry")
                        if (geom.isJsonObject && geom.asJsonObject.has("coordinates")) {
                            val coords = geom.asJsonObject.getAsJsonArray("coordinates")
                            for (j in 0 until coords.size()) {
                                val point = coords.get(j).asJsonArray
                                if (point.size() >= 2) {
                                    list.add(LatLng(point.get(1).asDouble, point.get(0).asDouble))
                                }
                            }
                        }
                    }
                }
                if (list.isNotEmpty()) return list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    override suspend fun getRoute(origin: LatLng, dest: LatLng): Result<List<com.example.uzradyab.domain.model.RouteDomainModel>> = runCatching {
        val originStr = "${origin.latitude},${origin.longitude}"
        val destStr = "${dest.latitude},${dest.longitude}"
        val response = api.getDirection(type = "car", origin = originStr, destination = destStr)
        android.util.Log.d("NavigationFlow", "Neshan Response: ${com.google.gson.Gson().toJson(response)}")

        val routes = response.routes ?: throw Exception("No route found")
        if (routes.isEmpty()) throw Exception("No route found")

        routes.map { route ->
            val polylineStr = route.overview_polyline?.points ?: throw Exception("No polyline found")
            val leg = route.legs?.firstOrNull() ?: throw Exception("No leg info found")
            val steps = leg.steps.orEmpty().mapNotNull(::toRouteStep)

            // The overview polyline is simplified and cuts across blocks; step polylines follow the road.
            val detailedPoints = mutableListOf<LatLng>()
            steps.forEach { step ->
                step.points.forEach { point ->
                    if (detailedPoints.lastOrNull() != point) detailedPoints.add(point)
                }
            }

            com.example.uzradyab.domain.model.RouteDomainModel(
                points = if (detailedPoints.size >= 2) detailedPoints else decodePolyline(polylineStr),
                distanceText = leg.distance?.text ?: "",
                durationText = leg.duration?.text ?: "",
                summaryText = leg.summary ?: "",
                distanceMeters = leg.distance?.value ?: 0,
                durationSeconds = leg.duration?.value ?: 0,
                steps = steps
            )
        }
    }

    private fun toRouteStep(step: com.example.uzradyab.data.remote.dto.NeshanStep): com.example.uzradyab.domain.model.RouteStep? {
        val points = step.polyline?.let(::decodePolyline).orEmpty()
        val start = step.start_location
            ?.takeIf { it.size >= 2 }
            ?.let { LatLng(it[1], it[0]) }
            ?: points.firstOrNull()
            ?: return null

        return com.example.uzradyab.domain.model.RouteStep(
            instruction = step.instruction.orEmpty(),
            streetName = step.name.orEmpty(),
            distanceMeters = step.distance?.value ?: 0,
            durationSeconds = step.duration?.value ?: 0,
            distanceText = step.distance?.text.orEmpty(),
            maneuver = com.example.uzradyab.domain.model.Maneuver(
                type = step.type.orEmpty(),
                modifier = step.modifier.orEmpty()
            ),
            startLocation = start,
            bearingAfter = step.bearing_after,
            rotaryExit = step.exit,
            rotaryName = step.rotaryName,
            points = points
        )
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
    }
}
