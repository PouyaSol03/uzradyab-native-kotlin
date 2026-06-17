package com.example.uzradyab.domain.model

data class Geofence(
    val id: Long,
    val name: String,
    val description: String?,
    val area: String,
    val centerLat: Double?,
    val centerLon: Double?,
    val radius: Double?,
    val isCircle: Boolean,
    val shape: GeofenceShape = parseArea(area)
) {
    companion object {
        fun parseArea(area: String): GeofenceShape {
            if (area.startsWith("CIRCLE")) {
                try {
                    // Traccar format: CIRCLE (lat lon, radius)
                    val content = area.substringAfter("CIRCLE (").substringBefore(")")
                    val parts = content.split(",")
                    if (parts.size == 2) {
                        val coords = parts[0].trim().split(" ")
                        if (coords.size == 2) {
                            val lat = coords[0].toDouble()
                            val lon = coords[1].toDouble()
                            val radius = parts[1].trim().toDouble()
                            return GeofenceShape.Circle(lat, lon, radius)
                        }
                    }
                } catch (e: Exception) {
                    // ignore and return Unknown
                }
            } else if (area.startsWith("POLYGON")) {
                try {
                    val content = area.substringAfter("POLYGON ((").substringBefore("))")
                    val coords = content.split(",").map { it.trim().split(" ") }
                    val points = coords.mapNotNull { 
                        if (it.size >= 2) Pair(it[0].toDouble(), it[1].toDouble()) else null 
                    }
                    if (points.isNotEmpty()) {
                        return GeofenceShape.Polygon(points)
                    }
                } catch (e: Exception) { }
            } else if (area.startsWith("LINESTRING")) {
                try {
                    val content = area.substringAfter("LINESTRING (").substringBefore(")")
                    val coords = content.split(",").map { it.trim().split(" ") }
                    val points = coords.mapNotNull { 
                        if (it.size >= 2) Pair(it[0].toDouble(), it[1].toDouble()) else null 
                    }
                    if (points.isNotEmpty()) {
                        return GeofenceShape.LineString(points)
                    }
                } catch (e: Exception) { }
            }
            return GeofenceShape.Unknown
        }
        
        fun buildCircleArea(lat: Double, lon: Double, radius: Double): String {
            return "CIRCLE ($lat $lon, $radius)"
        }
        
        fun buildPolygonArea(points: List<Pair<Double, Double>>): String {
            if (points.isEmpty()) return ""
            // Ensure polygon is closed
            val closedPoints = if (points.first() != points.last()) points + points.first() else points
            val coordString = closedPoints.joinToString(", ") { "${it.first} ${it.second}" }
            return "POLYGON (($coordString))"
        }
        
        fun buildLineStringArea(points: List<Pair<Double, Double>>): String {
            if (points.isEmpty()) return ""
            val coordString = points.joinToString(", ") { "${it.first} ${it.second}" }
            return "LINESTRING ($coordString)"
        }
    }
}

sealed class GeofenceShape {
    data class Circle(val lat: Double, val lon: Double, val radius: Double) : GeofenceShape()
    data class Polygon(val points: List<Pair<Double, Double>>) : GeofenceShape()
    data class LineString(val points: List<Pair<Double, Double>>) : GeofenceShape()
    object Unknown : GeofenceShape()
}
