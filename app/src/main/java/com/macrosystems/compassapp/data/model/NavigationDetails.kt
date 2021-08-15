package com.macrosystems.compassapp.data.model

import androidx.annotation.Keep
import com.google.android.gms.maps.model.LatLng

@Keep
data class NavigationDetails (val destinationAddress: String, val actualLatLng: LatLng, val destinationLatLng: LatLng)