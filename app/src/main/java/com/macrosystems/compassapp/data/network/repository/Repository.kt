package com.macrosystems.compassapp.data.network.repository

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.macrosystems.compassapp.data.model.NavigationDetails
import com.macrosystems.compassapp.data.network.Result

interface Repository {
    suspend fun initializeGooglePlaces(): Result<Boolean>
    suspend fun getLocationUpdates()
    suspend fun stopLocationUpdates()
    suspend fun startLocationUpdates(): MutableLiveData<LatLng?>
    suspend fun calculateDistance(destinationLatLng: LatLng): Result<Int>
    suspend fun saveNavigationDetailsInPersistence(navigationDetails: NavigationDetails)
    suspend fun loadNavigationDetails(): Result<NavigationDetails>

}