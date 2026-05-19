package com.example.uzradyab.core.network

import java.io.IOException

class ApiException(
    val code: Int,
    message: String,
) : IOException(message)
