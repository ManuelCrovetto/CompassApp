package com.macrosystems.compassapp.data.network.location

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.macrosystems.compassapp.R
import com.macrosystems.compassapp.data.response.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import javax.inject.Inject

class GooglePlacesService @Inject constructor(@ApplicationContext private val context: Context) {

    fun initializeGooglePlaces(): Result<Boolean> {

        return try {
            Places.initialize(context, context.getString(R.string.google_maps_key))
            Result.OnSuccess(true)
        } catch (e: Exception){
            Result.OnSuccess(false)
        }
    }
}