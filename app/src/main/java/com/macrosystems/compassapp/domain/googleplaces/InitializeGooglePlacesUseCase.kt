package com.macrosystems.compassapp.domain.googleplaces

import com.macrosystems.compassapp.data.network.location.GooglePlacesService
import com.macrosystems.compassapp.data.response.Result
import javax.inject.Inject

class InitializeGooglePlacesUseCase @Inject constructor(private val googlePlacesService: GooglePlacesService){

    operator fun invoke(): Result<Boolean> = googlePlacesService.initializeGooglePlaces()
}