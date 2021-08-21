package com.macrosystems.compassapp.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.macrosystems.compassapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class NavigationDetailsDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NavigationDetailsDatabase
    private lateinit var dao: NavigationDetailsDao

    @Before
    fun setup(){
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NavigationDetailsDatabase::class.java).allowMainThreadQueries().build()
        dao = database.navigationDetailsDao()
    }

    @After
    fun teardown(){
        database.close()
    }

    @Test
    fun insertNavDetails() = runBlockingTest{
        val destinationLatLng = LatLng(39.570883, 2.643775)
        val actualLatLng = LatLng(39.570883, 2.643775)
        val navDetails = NavigationDetailsEntity(id = 1, destinationLatLng = latLngToString(destinationLatLng), actualLatLng = latLngToString(actualLatLng), destinationAddress = "Calle golondrina 1")
        dao.insertNavigationDetails(navDetails)

        val allNavDetails = dao.observeLastNavigationDetails().getOrAwaitValue()

        assertThat(allNavDetails).isEqualTo(navDetails)
    }

    @Test
    fun deleteNavDetails() = runBlockingTest{
        val destinationLatLng = LatLng(39.570883, 2.643775)
        val actualLatLng = LatLng(39.570883, 2.643775)
        val navDetails = NavigationDetailsEntity(id = 1, destinationLatLng = latLngToString(destinationLatLng), actualLatLng = latLngToString(actualLatLng), destinationAddress = "Calle golondrina 1")
        dao.insertNavigationDetails(navDetails)
        dao.deleteNavigationDetails()

        val allNavDetails = dao.observeLastNavigationDetails().getOrAwaitValue()

        assertThat(allNavDetails).isNotEqualTo(navDetails)
    }


}