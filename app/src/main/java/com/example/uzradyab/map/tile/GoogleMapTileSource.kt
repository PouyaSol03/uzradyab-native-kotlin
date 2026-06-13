package com.example.uzradyab.map.tile

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

object GoogleMapTileSource : OnlineTileSourceBase(
    "GoogleRoad",
    3,
    20,
    256,
    ".png",
    arrayOf("https://mt0.google.com/vt/lyrs=m&hl=fa&x=", "https://mt1.google.com/vt/lyrs=m&hl=fa&x=", "https://mt2.google.com/vt/lyrs=m&hl=fa&x=", "https://mt3.google.com/vt/lyrs=m&hl=fa&x=")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z=" + MapTileIndex.getZoom(pMapTileIndex)
    }
}
