package com.macrosystems.compassapp.domain.localdb

import com.macrosystems.compassapp.data.local.NavigationDetailsDatabase
import com.macrosystems.compassapp.data.local.NavigationDetailsEntity
import javax.inject.Inject

class SaveNavigationDetailsInToRoom @Inject constructor(private val database: NavigationDetailsDatabase) {

    suspend operator fun invoke(navigationDetails: NavigationDetailsEntity) =
        database.navigationDetailsDao()
            .insertNavigationDetails(navigationDetailsEntity = navigationDetails)
}