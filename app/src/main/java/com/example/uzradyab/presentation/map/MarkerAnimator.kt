package com.example.uzradyab.presentation.map

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import java.util.WeakHashMap

object MarkerAnimator {
    private val animators = WeakHashMap<Symbol, ValueAnimator>()

    fun animateMarker(
        symbol: Symbol,
        symbolManager: SymbolManager,
        mapView: MapLibreMap,
        endPosition: LatLng,
        endCourse: Float,
        rotateMap: Boolean,
        durationMs: Long = 1000L
    ) {
        // Cancel any ongoing animation for this marker
        animators[symbol]?.cancel()

        val startPosition = symbol.latLng
        val startOrientation = mapView.cameraPosition.bearing.toFloat()
        val targetOrientation = (360f - endCourse) % 360f

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = durationMs
        animator.interpolator = LinearInterpolator()

        var deltaRotation = (targetOrientation - startOrientation) % 360f
        if (deltaRotation > 180f) deltaRotation -= 360f
        if (deltaRotation < -180f) deltaRotation += 360f

        animator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            
            // Interpolate position
            val lat = startPosition.latitude + (endPosition.latitude - startPosition.latitude) * fraction
            val lon = startPosition.longitude + (endPosition.longitude - startPosition.longitude) * fraction
            val currentLatLng = LatLng(lat, lon)
            
            symbol.latLng = currentLatLng
            symbolManager.update(symbol)

            if (rotateMap) {
                // Interpolate map orientation based on course
                val bearing = startOrientation + deltaRotation * fraction
                val cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                    .target(currentLatLng)
                    .bearing(bearing.toDouble())
                    .build()
                mapView.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            } else {
                mapView.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
            }
        }
        
        animators[symbol] = animator
        animator.start()
    }
}
