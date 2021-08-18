package com.macrosystems.compassapp.domain.compass

import com.macrosystems.compassapp.data.network.compass.CompassLogic
import javax.inject.Inject

class StartCompassUseCase @Inject constructor(private val compassLogic: CompassLogic) {

    operator fun invoke() = compassLogic.updateCompass()
}