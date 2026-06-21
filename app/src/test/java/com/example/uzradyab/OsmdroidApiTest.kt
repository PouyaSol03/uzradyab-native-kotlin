package com.example.uzradyab

import org.junit.Test
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.MapTileDownloader

class OsmdroidApiTest {
    @Test
    fun testApi() {
        val methods = MapTileDownloader::class.java.methods
        for (m in methods) {
            println("METHOD: " + m.name + " " + m.parameterTypes.joinToString())
        }
        val configMethods = Configuration.getInstance()::class.java.methods
        for (m in configMethods) {
            println("CONFIG_METHOD: " + m.name + " " + m.parameterTypes.joinToString())
        }
    }
}
