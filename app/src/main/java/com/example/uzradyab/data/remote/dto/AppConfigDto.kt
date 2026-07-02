package com.example.uzradyab.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AppConfigDto(
    @SerializedName("new_release")
    val newRelease: Boolean = false,
    
    @SerializedName("new_release_code")
    val newReleaseCode: String? = null,
    
    @SerializedName("new_release_description")
    val newReleaseDescription: List<ReleaseDescriptionDto> = emptyList(),
    
    @SerializedName("alternative_map")
    val alternativeMap: Boolean = false,
    
    @SerializedName("alternative_map_url")
    val alternativeMapUrl: String? = null
)

data class ReleaseDescriptionDto(
    @SerializedName("key")
    val key: String,
    
    @SerializedName("value")
    val value: String
)
