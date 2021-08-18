package com.macrosystems.compassapp.domain.location

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.macrosystems.compassapp.data.network.location.LocationService
import javax.inject.Inject

class StartLocationUpdatesUseCase @Inject constructor(private val locationService: LocationService) {

    suspend operator fun invoke(): MutableLiveData<LatLng?> = locationService.startLocationUpdates()
}