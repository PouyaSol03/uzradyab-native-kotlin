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
        endPosition: LatLng,
        durationMs: Long = 1000L,
        onUpdate: ((fraction: Float, currentLatLng: LatLng) -> Unit)? = null
    ) {
        // Cancel any ongoing animation for this marker
        animators[symbol]?.cancel()

        val startPosition = symbol.latLng

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = durationMs
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            
            // Interpolate position
            val lat = startPosition.latitude + (endPosition.latitude - startPosition.latitude) * fraction
            val lon = startPosition.longitude + (endPosition.longitude - startPosition.longitude) * fraction
            val currentLatLng = LatLng(lat, lon)
            
            symbol.latLng = currentLatLng
            symbolManager.update(symbol)
            
            onUpdate?.invoke(fraction, currentLatLng)
        }
        
        animators[symbol] = animator
        animator.start()
    }
}
