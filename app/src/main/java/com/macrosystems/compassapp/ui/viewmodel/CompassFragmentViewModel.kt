package com.macrosystems.compassapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.macrosystems.compassapp.data.model.Constants.Companion.TIME_OUT_FOR_GETTING_LOCATION
import com.macrosystems.compassapp.data.network.Repository
import com.macrosystems.compassapp.data.network.Result
import com.macrosystems.compassapp.ui.view.CompassViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class CompassFragmentViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    private val _location: MutableLiveData<LatLng> = MutableLiveData()
    val location: LiveData<LatLng>
        get() = _location

    private val _distance: MutableLiveData<Int> = MutableLiveData()
    val distance: LiveData<Int>
        get() = _distance

    private val _viewState = MutableStateFlow(CompassViewState())
    val viewState: StateFlow<CompassViewState>
        get() = _viewState

    init {
        initializeGooglePlaces()
    }

    fun startLocationUpdates(){
        viewModelScope.launch {

            val result = withContext(Dispatchers.IO){
                withTimeoutOrNull(TIME_OUT_FOR_GETTING_LOCATION){
                    repository.startLocationUpdates()
                }
            }
            _viewState.value = CompassViewState(isLoading = true)
            result?.observeForever {
                it?.let {
                    _location.postValue(it)
                    _viewState.value = CompassViewState(isLoading = false, onSuccess = true)
                } ?: run {
                    _viewState.value = CompassViewState(isLoading = false, locationError = true, errorMessage = "Oops, an error has occurred while getting your location, please try again.")
                }
            } ?: run {
                _viewState.value = CompassViewState(isLoading = false, locationError = true, errorMessage = "Oops, an error has occurred while getting your location, please try again.")

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
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO){
                repository.initializeGooglePlaces()
            }
            _viewState.value = CompassViewState(isLoading = true)
            when (result){
                is Result.OnSuccess->{
                    if (result.data){
                        _viewState.value = CompassViewState(isLoading = false)
                    } else {
                        _viewState.value = CompassViewState(isLoading = false, googlePlacesError = true)
                    }
                }
                else->{}
            }
        }

    }

    fun calculateDistance(destinationLatLng: LatLng, isFirstQueryOfThisDestination: Boolean){
        if (isFirstQueryOfThisDestination){

            viewModelScope.launch {
                val result = withContext(Dispatchers.IO){
                    repository.calculateDistance(destinationLatLng)
                }
                _viewState.value = CompassViewState(isLoading = true)
                when (result){
                    is Result.OnSuccess->{
                        _distance.postValue(result.data)
                        _viewState.value = CompassViewState(isLoading = false, onSuccess = true)
                    }
                    else->{
                        _viewState.value = CompassViewState(isLoading = false, onFailure = true)
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
                        _distance.postValue(result.data)
                        _viewState.value = CompassViewState(isLoading = false, onSuccess = true)
                    }
                    else->{
                        _viewState.value = CompassViewState(isLoading = false, onFailure = true)
                    }
                }
            }
        }


    }

}