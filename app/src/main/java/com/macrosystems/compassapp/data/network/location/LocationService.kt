package com.macrosystems.compassapp.data.network.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.HandlerThread
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.macrosystems.compassapp.data.response.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(@ApplicationContext private val context: Context) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var location: MutableLiveData<LatLng?> = MutableLiveData()

    @SuppressLint("MissingPermission")
    suspend fun startLocationUpdates(): MutableLiveData<LatLng?> {
        getLocationUpdates()
        val handlerThread = HandlerThread("MyHandler")
        handlerThread.start()
        val looper: Looper = handlerThread.looper
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, looper).await()

        return location
    }

    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            locationRequest = LocationRequest.create().apply {
                interval = 100
                fastestInterval = 100
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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

    suspend fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized)
            fusedLocationClient.removeLocationUpdates(locationCallback).await()
    }

    fun calculateDistance(destinationLatLng: LatLng): Result<Int> {
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
}