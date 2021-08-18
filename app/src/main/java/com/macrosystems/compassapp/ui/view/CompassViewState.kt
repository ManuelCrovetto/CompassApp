package com.macrosystems.compassapp.ui.view

data class CompassViewState(
    val isLoading: Boolean = false,
    val onSuccess: Boolean = false,
    val onFailure: Boolean = false,
    val locationError: Boolean = false,
    val googlePlacesError: Boolean = false,
    val compassSensorError: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null)