package com.macrosystems.compassapp.domain.location


import com.macrosystems.compassapp.data.network.location.LocationService
import javax.inject.Inject

class StopLocationUpdatesUseCase @Inject constructor(private val locationService: LocationService){

    suspend operator fun invoke() = locationService.stopLocationUpdates()
}