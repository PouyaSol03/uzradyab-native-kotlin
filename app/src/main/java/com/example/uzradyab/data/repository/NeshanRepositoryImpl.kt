package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.NeshanApi
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.NeshanRepository
import com.google.gson.JsonObject
import android.util.Log
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject
import javax.inject.Singleton

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
}
