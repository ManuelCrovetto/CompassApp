package com.macrosystems.compassapp.data.model


import androidx.annotation.Keep
import javax.inject.Inject

@Keep
class Constants @Inject constructor() {
    companion object {
        const val TIME_OUT_FOR_GETTING_LOCATION = 10000L
    }
}