package com.example.uzradyab.di

import com.example.uzradyab.core.util.StringProvider
import com.example.uzradyab.core.util.StringProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilModule {
    @Binds
    @Singleton
    abstract fun bindStringProvider(impl: StringProviderImpl): StringProvider
}
