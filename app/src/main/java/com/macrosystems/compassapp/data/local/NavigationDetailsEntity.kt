package com.macrosystems.compassapp.data.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey


@Keep
@Entity(tableName = "navigation_details")
data class NavigationDetailsEntity(@PrimaryKey(autoGenerate = true) val id: Int? = null, var destinationAddress: String?, var actualLatLng: String?, var destinationLatLng: String?)