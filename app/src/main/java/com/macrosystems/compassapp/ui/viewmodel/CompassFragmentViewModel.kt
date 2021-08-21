package com.macrosystems.compassapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.macrosystems.compassapp.data.local.NavigationDetailsEntity
import com.macrosystems.compassapp.data.local.latLngToString
import com.macrosystems.compassapp.data.local.stringToLatLng
import com.macrosystems.compassapp.data.model.CompassData
import com.macrosystems.compassapp.data.model.Constants.Companion.TIME_OUT_FOR_GETTING_LOCATION
import com.macrosystems.compassapp.data.model.NavigationDetails
import com.macrosystems.compassapp.data.response.Result
import com.macrosystems.compassapp.domain.compass.StartCompassUseCase
import com.macrosystems.compassapp.domain.compass.StopCompassUseCase
import com.macrosystems.compassapp.domain.googleplaces.InitializeGooglePlacesUseCase
import com.macrosystems.compassapp.domain.localdb.DeleteLastLocationFromRoom
import com.macrosystems.compassapp.domain.location.CalculateDistanceUseCase
import com.macrosystems.compassapp.domain.location.StartLocationUpdatesUseCase
import com.macrosystems.compassapp.domain.location.StopLocationUpdatesUseCase
import com.macrosystems.compassapp.domain.localdb.LoadNavigationDetailsFromRoom
import com.macrosystems.compassapp.domain.localdb.SaveNavigationDetailsInToRoom
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
class CompassFragmentViewModel @Inject constructor(
    private val startLocationUpdatesUseCase: StartLocationUpdatesUseCase,
    private val stopLocationUpdatesUseCase: StopLocationUpdatesUseCase,
    private val calculateDistanceUseCase: CalculateDistanceUseCase,
    private val googlePlacesUseCase: InitializeGooglePlacesUseCase,
    private val saveNavigationDetailsInToRoom: SaveNavigationDetailsInToRoom,
    private val loadNavigationDetailsFromRoom: LoadNavigationDetailsFromRoom,
    private val deleteLastLocationFromRoom: DeleteLastLocationFromRoom,
    private val startCompassUseCase: StartCompassUseCase,
    private val stopCompassUseCase: StopCompassUseCase
) : ViewModel() {

    private val _savedNavigationDetails: MutableLiveData<NavigationDetails> = MutableLiveData()
    val savedNavigationDetails: LiveData<NavigationDetails>
        get() = _savedNavigationDetails

    private val _location: MutableLiveData<LatLng> = MutableLiveData()
    val location: LiveData<LatLng>
        get() = _location

    private val _distance: MutableLiveData<Int> = MutableLiveData()
    val distance: LiveData<Int>
        get() = _distance

    private val _viewState = MutableStateFlow(CompassViewState())
    val viewState: StateFlow<CompassViewState>
        get() = _viewState

    private val _compassData: MutableLiveData<CompassData> = MutableLiveData()
    val compassData: LiveData<CompassData>
        get() = _compassData

    init {
        initializeGooglePlaces()
        loadNavigationDetailsFromLocalDB()
        startCompass()
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            val result: MutableLiveData<LatLng?>? = withContext(Dispatchers.IO) {
                withTimeoutOrNull(TIME_OUT_FOR_GETTING_LOCATION) {
                    startLocationUpdatesUseCase()
                }
            }
            _viewState.value = CompassViewState(isLoading = true)
            result?.observeForever {
                it?.let {
                    _location.postValue(it)
                    _viewState.value = CompassViewState(isLoading = false, onSuccess = true)
                } ?: run {
                    _viewState.value = CompassViewState(
                        isLoading = false,
                        locationError = true,
                        errorMessage = "Oops, an error has occurred while getting your location, please try again."
                    )
                }
            } ?: run {
                _viewState.value = CompassViewState(
                    isLoading = false,
                    locationError = true,
                    errorMessage = "Oops, an error has occurred while getting your location, please try again."
                )
            }
        }
    }

    fun stopLocationUpdates() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                stopLocationUpdatesUseCase()
            }
        }
    }

    fun initializeGooglePlaces() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                googlePlacesUseCase()
            }
            _viewState.value = CompassViewState(isLoading = true)
            when (result) {
                is Result.OnSuccess -> {
                    if (result.data) {
                        _viewState.value = CompassViewState(isLoading = false)
                    } else {
                        _viewState.value =
                            CompassViewState(isLoading = false, googlePlacesError = true)
                    }
                }
                else -> {
                }
            }
        }

    }

    fun calculateDistance(destinationLatLng: LatLng, isFirstQueryOfThisDestination: Boolean) {
        if (isFirstQueryOfThisDestination) {
            viewModelScope.launch {
                val result = withContext(Dispatchers.IO) {
                    calculateDistanceUseCase(destinationLatLng = destinationLatLng)
                }
                _viewState.value = CompassViewState(isLoading = true)
                when (result) {
                    is Result.OnSuccess -> {
                        _distance.postValue(result.data)
                        _viewState.value = CompassViewState(isLoading = false, onSuccess = true)
                    }
                    else -> {
                        _viewState.value = CompassViewState(isLoading = false, onFailure = true)
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val result = withContext(Dispatchers.IO) {
                    calculateDistanceUseCase(destinationLatLng = destinationLatLng)
                }

                when (result) {
                    is Result.OnSuccess -> {
                        _distance.postValue(result.data)
                        _viewState.value = CompassViewState(isLoading = false, onSuccess = true)
                    }
                    else -> {
                        _viewState.value = CompassViewState(isLoading = false, onFailure = true)
                    }
                }
            }
        }
    }

    fun startCompass() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.Main) {
                startCompassUseCase()
            }
            result.observeForever { compassData ->
                if (compassData.error) {
                    _viewState.value = CompassViewState(compassSensorError = true)
                } else {
                    _compassData.value = compassData
                }
            }
        }
    }

    fun stopCompass() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                stopCompassUseCase()
            }
        }
    }

    fun deleteLastLocation() {
        viewModelScope.launch {
            deleteLastLocationFromRoom()
        }
    }

    fun saveNavigationDetailsToLocalDB(navigationDetails: NavigationDetails) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                saveNavigationDetailsInToRoom(
                    navigationDetails = NavigationDetailsEntity(
                        destinationAddress = navigationDetails.destinationAddress,
                        destinationLatLng = latLngToString(navigationDetails.destinationLatLng),
                        actualLatLng = latLngToString(navigationDetails.actualLatLng)
                    )
                )
            }

        }
    }

    private fun loadNavigationDetailsFromLocalDB() {
        viewModelScope.launch {
            val result: LiveData<NavigationDetailsEntity>? = loadNavigationDetailsFromRoom()
            result?.observeForever {
                it?.let {
                    _savedNavigationDetails.value = NavigationDetails(
                        destinationAddress = it.destinationAddress!!,
                        actualLatLng = stringToLatLng(it.actualLatLng)!!,
                        destinationLatLng = stringToLatLng(it.destinationLatLng)!!
                    )
                }
            }
        }
    }


}