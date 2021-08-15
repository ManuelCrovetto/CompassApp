package com.macrosystems.compassapp.data.network

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

interface Repository {
    suspend fun initializeGooglePlaces(): Result<Boolean>
    suspend fun getLocationUpdates()
    suspend fun stopLocationUpdates()
    suspend fun startLocationUpdates(): MutableLiveData<LatLng?>
    suspend fun calculateDistance(destinationLatLng: LatLng): Result<Int>
}