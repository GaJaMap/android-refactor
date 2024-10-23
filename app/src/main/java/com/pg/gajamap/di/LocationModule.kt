package com.pg.gajamap.di

import com.pg.gajamap.data.manager.LocationManager
import com.pg.gajamap.data.manager.LocationTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindsLocationTracker(locationManager: LocationManager): LocationTracker
}