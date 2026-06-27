package com.example.uzradyab.core.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface StringProvider {
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}

@Singleton
class StringProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StringProvider {
    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}
