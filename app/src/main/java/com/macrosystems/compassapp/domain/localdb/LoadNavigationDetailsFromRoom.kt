package com.macrosystems.compassapp.domain.localdb

import androidx.lifecycle.LiveData
import com.macrosystems.compassapp.data.local.NavigationDetailsDatabase
import com.macrosystems.compassapp.data.local.NavigationDetailsEntity
import java.lang.Exception
import javax.inject.Inject

class LoadNavigationDetailsFromRoom @Inject constructor(private val database: NavigationDetailsDatabase) {

    operator fun invoke(): LiveData<NavigationDetailsEntity>? {
        return try {
            database.navigationDetailsDao().observeLastNavigationDetails()
        } catch (e: Exception){
            return null
        }

    }
}