package com.macrosystems.compassapp.data.local

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Moshi

@TypeConverter
fun stringToLatLng(input: String?): LatLng? =
    input?.let { Moshi.Builder().build().adapter(LatLng::class.java).fromJson(it) }

@TypeConverter
fun latLngToString(input: LatLng?): String? = Moshi.Builder().build().adapter(LatLng::class.java).toJson(input)