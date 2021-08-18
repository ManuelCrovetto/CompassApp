package com.macrosystems.compassapp.data.network.location

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.macrosystems.compassapp.data.model.Constants
import com.macrosystems.compassapp.data.response.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import javax.inject.Inject

class GooglePlacesService @Inject constructor(@ApplicationContext private val context: Context) {

    fun initializeGooglePlaces(): Result<Boolean> {
        return try {
            Places.initialize(context, Constants.GOOGLE_PLACES_API_KEY)
            Result.OnSuccess(true)
        } catch (e: Exception){
            Result.OnSuccess(false)
        }
    }
}