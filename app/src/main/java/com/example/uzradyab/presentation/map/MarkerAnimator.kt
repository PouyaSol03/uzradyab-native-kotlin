package com.example.uzradyab.presentation.map

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.WeakHashMap

object MarkerAnimator {
    private val animators = WeakHashMap<Marker, ValueAnimator>()

    fun animateMarker(
        marker: Marker,
        mapView: MapView,
        endPosition: GeoPoint,
        endCourse: Float,
        rotateMap: Boolean,
        durationMs: Long = 1000L
    ) {
        // Cancel any ongoing animation for this marker
        animators[marker]?.cancel()

        val startPosition = GeoPoint(marker.position.latitude, marker.position.longitude)
        val startOrientation = mapView.mapOrientation
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
            marker.position = GeoPoint(lat, lon)
            
            // Marker should always stay vertical
            marker.rotation = 0f

            if (rotateMap) {
                // Interpolate map orientation based on course
                mapView.mapOrientation = startOrientation + deltaRotation * fraction
            }
            
            // Track the marker exactly frame-by-frame
            mapView.controller.setCenter(GeoPoint(lat, lon))
            
            mapView.invalidate()
        }
        
        animators[marker] = animator
        animator.start()
    }
}
