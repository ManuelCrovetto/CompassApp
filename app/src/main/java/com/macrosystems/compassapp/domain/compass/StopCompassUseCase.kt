package com.macrosystems.compassapp.domain.compass

import com.macrosystems.compassapp.data.network.compass.CompassLogic
import javax.inject.Inject

class StopCompassUseCase @Inject constructor(private val compassLogic: CompassLogic) {

    operator fun invoke() = compassLogic.stopCompass()
}