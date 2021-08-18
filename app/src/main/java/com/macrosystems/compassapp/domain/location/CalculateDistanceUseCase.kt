package com.macrosystems.compassapp.domain.location

import com.google.android.gms.maps.model.LatLng
import com.macrosystems.compassapp.data.network.location.LocationService
import javax.inject.Inject

class CalculateDistanceUseCase @Inject constructor(private val locationService: LocationService) {
    operator fun invoke(destinationLatLng: LatLng) = locationService.calculateDistance(destinationLatLng = destinationLatLng)
}