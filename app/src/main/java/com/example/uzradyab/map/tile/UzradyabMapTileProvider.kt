package com.example.uzradyab.map.tile

import android.content.Context
import org.osmdroid.tileprovider.IRegisterReceiver
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck
import org.osmdroid.tileprovider.modules.MapTileApproximater
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver

/**
 * A custom MapTileProvider that perfectly replicates Osmdroid's standard MapTileProviderBasic
 * but uses our highly optimized OkHttpTileModuleProvider for network requests.
 *
 * The provider chain:
 * 1. Assets (Checks if tile is packaged with app)
 * 2. SqlCache (Checks local SQLite database cache for offline viewing)
 * 3. FileArchive (Checks local zip/sqlite archives)
 * 4. OkHttp Module (Fetches from network if not found locally, and saves to SqlCache)
 */
class UzradyabMapTileProvider(
    context: Context,
    tileSource: ITileSource = TileSourceFactory.DEFAULT_TILE_SOURCE,
    registerReceiver: IRegisterReceiver = SimpleRegisterReceiver(context)
) : MapTileProviderArray(tileSource, registerReceiver, arrayOf()) {

    // A dedicated writer for the OkHttp provider so we can cleanly detach it
    private val okHttpSqlWriter = org.osmdroid.tileprovider.modules.SqlTileWriter()

    init {
        // 1. Assets Provider (Extremely fast, local app assets)
        val assetsProvider = MapTileAssetsProvider(registerReceiver, context.assets, tileSource)
        mTileProviderList.add(assetsProvider)

        // 2. Sqlite Cache Provider (Local offline cache)
        val sqlCacheProvider = MapTileSqlCacheProvider(registerReceiver, tileSource)
        mTileProviderList.add(sqlCacheProvider)

        // 3. File Archive Provider (Zip/Sqlite files placed in osmdroid path)
        val archiveProvider = MapTileFileArchiveProvider(registerReceiver, tileSource)
        mTileProviderList.add(archiveProvider)

        // 4. OkHttp Network Provider (Only hit if tile is not in local cache)
        val networkAvailabilityCheck = NetworkAvailabliltyCheck(context)
        val okHttpProvider = OkHttpTileModuleProvider(
            tileSource = tileSource,
            filesystemCache = okHttpSqlWriter,
            networkAvailabilityCheck = networkAvailabilityCheck
        )
        mTileProviderList.add(okHttpProvider)
        
        // 5. Approximater (Stretches low-res tiles to instantly fill chess squares while disk/network loads)
        val approximationProvider = MapTileApproximater()
        mTileProviderList.add(approximationProvider)
        approximationProvider.addProvider(assetsProvider)
        approximationProvider.addProvider(sqlCacheProvider)
        approximationProvider.addProvider(archiveProvider)
    }

    override fun detach() {
        // DO NOTHING! 
        // In Jetpack Compose, navigating between screens can cause the old MapView to call onDetach()
        // *after* the new MapView has already started. If we detach here, we close the global SQLite
        // database and kill the tile threads, causing the new MapView to render a permanent white screen.
        // We intentionally leave the tile provider and its SQLite connection alive for the app's lifetime.
    }
}
