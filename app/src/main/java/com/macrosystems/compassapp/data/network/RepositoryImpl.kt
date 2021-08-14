package com.macrosystems.compassapp.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.HandlerThread
import android.os.Looper
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.macrosystems.compassapp.data.model.Constants.Companion.GOOGLE_PLACES_API_KEY
import com.macrosystems.compassapp.data.model.Constants.Companion.GOOGLE_PLACES_REQUEST_CODE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class RepositoryImpl @Inject constructor(@ApplicationContext private val context: Context): Repository {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    private var location: MutableLiveData<LatLng?> = MutableLiveData()
    private var destination: MutableLiveData<LatLng?> = MutableLiveData()

    @SuppressLint("MissingPermission")
    override suspend fun getLocationUpdates() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            locationRequest = LocationRequest()
            locationRequest.interval = 100
            locationRequest.fastestInterval = 100

            locationRequest.priority = PRIORITY_HIGH_ACCURACY
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
            val results = FloatArray(1)
            Location.distanceBetween(
                location.value!!.latitude,
                location.value!!.longitude,
                destinationLatLng.latitude,
                destinationLatLng.longitude,
                results
            )
            Result.OnSuccess(results[0].toInt())
        } catch (e: Exception){
            Result.OnError(null)
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