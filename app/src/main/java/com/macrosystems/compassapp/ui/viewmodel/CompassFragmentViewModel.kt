package com.macrosystems.compassapp.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.macrosystems.compassapp.data.model.Constants.Companion.TIME_OUT_FOR_GETTING_LOCATION
import com.macrosystems.compassapp.data.network.Repository

import com.macrosystems.compassapp.data.network.Result
import com.macrosystems.compassapp.ui.core.interfaces.FragmentListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class CompassFragmentViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    var listener: FragmentListener? = null

    var isLocationOnline: MutableLiveData<Boolean> = MutableLiveData()
    var location: MutableLiveData<LatLng> = MutableLiveData()
    var distance: MutableLiveData<Int> = MutableLiveData()


    fun startLocationUpdates(){
        viewModelScope.launch {

            val result = withContext(Dispatchers.IO){
                withTimeoutOrNull(TIME_OUT_FOR_GETTING_LOCATION){
                    repository.startLocationUpdates()
                }
            }
            isLocationOnline.value = false
            result?.observeForever {
                it?.let {

                    location.postValue(it)
                    isLocationOnline.value = true
                } ?: run {
                    listener?.onFailure("Oops, an error has occurred while getting your location, please try again.")
                    isLocationOnline.value = false
                }
            } ?: run {
                listener?.onFailure("Oops, an error has occurred while getting your location, please try again.")

            }
        }
    }

    fun stopLocationUpdates(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                repository.stopLocationUpdates()
            }
        }
    }


    fun initializeGooglePlaces(){
        listener?.onStarted()

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO){
                repository.initializeGooglePlaces()
            }
            when (result){
                is Result.OnSuccess->{
                    if (result.data){
                        listener?.onSuccess(null)
                    } else {
                        listener?.onFailure("Oops, an error has occurred while getting address, please try again.")
                    }
                }
                else->{}
            }
        }

    }

    fun calculateDistance(destinationLatLng: LatLng, isFirstQueryOfThisDestination: Boolean){
        if (isFirstQueryOfThisDestination){
            listener?.onStarted()
            viewModelScope.launch {
                val result = withContext(Dispatchers.IO){
                    repository.calculateDistance(destinationLatLng)
                }
                when (result){
                    is Result.OnSuccess->{
                        distance.postValue(result.data)
                        listener?.onSuccess(null)
                    }
                    else->{
                        listener?.onFailure("Error occurred, please try again.")
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val result = withContext(Dispatchers.IO){
                    repository.calculateDistance(destinationLatLng)
                }

                when (result){
                    is Result.OnSuccess->{
                        distance.postValue(result.data)
                        listener?.onSuccess(null)
                    }
                    else->{
                        listener?.onFailure("Error occurred, please try again.")
                    }
                }
            }
        }


    }

}