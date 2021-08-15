package com.macrosystems.compassapp.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.HandlerThread
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.macrosystems.compassapp.data.model.Constants.Companion.GOOGLE_PLACES_API_KEY
import com.macrosystems.compassapp.data.model.NavigationDetails
import com.macrosystems.compassapp.data.network.persistence.Persistence
import com.macrosystems.compassapp.data.network.repository.Repository

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class RepositoryImpl @Inject constructor(@ApplicationContext private val context: Context, private val persistence: Persistence):
    Repository {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    private var location: MutableLiveData<LatLng?> = MutableLiveData()

    @SuppressLint("MissingPermission")
    override suspend fun getLocationUpdates() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            locationRequest = LocationRequest.create().apply {
                interval = 100
                fastestInterval = 100
                priority = PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    if (locationResult.locations.isNotEmpty()){
                        location.postValue(LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude))
                    }
                }
            }

        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override suspend fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override suspend fun startLocationUpdates(): MutableLiveData<LatLng?> {
        getLocationUpdates()
        val handlerThread = HandlerThread("MyHandler")
        handlerThread.start()
        val looper: Looper = handlerThread.looper
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, looper).await()

        return location
    }

    override suspend fun calculateDistance(destinationLatLng: LatLng): Result<Int> {
        return try {
            location.value?.let { latLng->
                val results = FloatArray(1)
                Location.distanceBetween(
                    latLng.latitude,
                    latLng.longitude,
                    destinationLatLng.latitude,
                    destinationLatLng.longitude,
                    results
                )
                Result.OnSuccess(results[0].toInt())

            } ?: run {
                Result.OnError(null)
            }

        } catch (e: Exception){
            Result.OnError(null)
        }

    }

    override suspend fun saveNavigationDetailsInPersistence(navigationDetails: NavigationDetails) {
        try {
            persistence.saveNavigationOnPersistence(navigationDetails)
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override suspend fun loadNavigationDetails(): Result<NavigationDetails> {
        try {
            persistence.loadNavigationDetailsFromPersistence()?.let {
                return Result.OnSuccess(it)
            } ?: run {
                return Result.OnError(null)
            }
        } catch (e: Exception){
            return Result.OnError(null)
        }
    }

    override suspend fun initializeGooglePlaces(): Result<Boolean> {
        return try {
            Places.initialize(context, GOOGLE_PLACES_API_KEY)
            Result.OnSuccess(true)
        } catch (e: Exception){
            Result.OnSuccess(false)
        }
    }
}