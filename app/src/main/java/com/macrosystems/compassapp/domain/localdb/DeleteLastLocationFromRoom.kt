package com.macrosystems.compassapp.domain.localdb

import com.macrosystems.compassapp.data.local.NavigationDetailsDatabase
import javax.inject.Inject

class DeleteLastLocationFromRoom @Inject constructor(private val database: NavigationDetailsDatabase){

    suspend operator fun invoke(): Boolean = runCatching {
        database.navigationDetailsDao().deleteNavigationDetails()
    }.isSuccess

}