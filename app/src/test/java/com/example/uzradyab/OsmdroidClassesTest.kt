package com.example.uzradyab

import org.junit.Test
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider

class OsmdroidClassesTest {
    @Test
    fun testClasses() {
        println("--- MapTileSqlCacheProvider Methods ---")
        MapTileSqlCacheProvider::class.java.methods.forEach {
            println("${it.name}(${it.parameterTypes.joinToString()}) -> ${it.returnType.name}")
        }
        println("--- MapTileSqlCacheProvider Fields ---")
        MapTileSqlCacheProvider::class.java.declaredFields.forEach {
            println("${it.name} -> ${it.type.name}")
        }
    }
}
