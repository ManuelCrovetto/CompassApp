package com.macrosystems.compassapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface NavigationDetailsDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertNavigationDetails(navigationDetailsEntity: NavigationDetailsEntity)

    @Query("DELETE FROM navigation_details")
    suspend fun deleteNavigationDetails()

    @Query("SELECT * FROM navigation_details ORDER BY ID DESC LIMIT 1")
    fun observeLastNavigationDetails(): LiveData<NavigationDetailsEntity>

}