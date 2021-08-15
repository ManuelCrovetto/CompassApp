package com.macrosystems.compassapp.data.network.persistence

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.macrosystems.compassapp.data.model.NavigationDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Persistence @Inject constructor(@ApplicationContext private val appContext: Context) {

    fun saveNavigationOnPersistence(navigationDetails: NavigationDetails) {
        val sharedPreferences = appContext.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(NavigationDetails(navigationDetails.destinationAddress, navigationDetails.actualLatLng, navigationDetails.destinationLatLng))
        editor.putString("navDetails", json)
        editor.apply()
    }

    fun loadNavigationDetailsFromPersistence(): NavigationDetails?{
        val sharedPreferences = appContext.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("navDetails", null)
        val type = object : TypeToken<NavigationDetails?>() {}.type
        val navigationDetails = gson.fromJson<NavigationDetails>(json, type)
        navigationDetails?.let {
            return it
        } ?: run {
            return null
        }
    }
}