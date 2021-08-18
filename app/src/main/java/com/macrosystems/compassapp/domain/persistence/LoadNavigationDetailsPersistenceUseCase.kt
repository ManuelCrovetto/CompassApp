package com.macrosystems.compassapp.domain.persistence

import com.macrosystems.compassapp.data.model.NavigationDetails
import com.macrosystems.compassapp.data.network.persistence.Persistence
import com.macrosystems.compassapp.data.response.Result
import java.lang.Exception
import javax.inject.Inject

class LoadNavigationDetailsPersistenceUseCase @Inject constructor(private val persistence: Persistence) {

    operator fun invoke(): Result<NavigationDetails>{
        return try {
            persistence.loadNavigationDetailsFromPersistence()?.let {
                return Result.OnSuccess(it)
            } ?: run {
                Result.OnError(null)
            }
        } catch (e: Exception){
            Result.OnError(null)
        }
    }
}