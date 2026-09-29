package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Position
import org.maplibre.android.geometry.LatLng

interface NeshanRepository {
    suspend fun getSnappedRoute(rawPoints: List<Position>): Result<List<LatLng>>
}
