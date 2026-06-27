package com.example.uzradyab.core.utils

import androidx.compose.runtime.Immutable

@Immutable
data class ImmutableListWrapper<T>(val items: List<T>) : List<T> by items

@Immutable
data class ImmutableMapWrapper<K, V>(val items: Map<K, V>) : Map<K, V> by items

fun <T> List<T>.toImmutable() = ImmutableListWrapper(this)
fun <K, V> Map<K, V>.toImmutable() = ImmutableMapWrapper(this)
fun <T> emptyImmutableList() = ImmutableListWrapper(emptyList<T>())
fun <K, V> emptyImmutableMap() = ImmutableMapWrapper(emptyMap<K, V>())
