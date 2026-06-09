package com.example.uzradyab.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MapIrReverseDto(
    @SerializedName("address")
    val address: String?,

    @SerializedName("formatted_address")
    val formattedAddress: String?
)