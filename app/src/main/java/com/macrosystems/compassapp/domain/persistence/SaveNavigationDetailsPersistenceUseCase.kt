package com.macrosystems.compassapp.domain.persistence

import com.macrosystems.compassapp.data.model.NavigationDetails
import com.macrosystems.compassapp.data.network.persistence.Persistence
import javax.inject.Inject

class SaveNavigationDetailsPersistenceUseCase @Inject constructor(private val persistence: Persistence) {

    operator fun invoke(navigationDetails: NavigationDetails) = persistence.saveNavigationOnPersistence(navigationDetails = navigationDetails)
}