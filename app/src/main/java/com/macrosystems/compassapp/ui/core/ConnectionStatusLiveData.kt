package com.macrosystems.compassapp.ui.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

const val TAG = "C-Manager"
@Singleton
class ConnectionStatusLiveData @Inject constructor(@ApplicationContext context: Context) : LiveData<Boolean>() {
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    private fun checkValidNetworks() {
        postValue(validNetworks.size > 0)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onInactive() {
        cm.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback() = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        /*
          Called when a network is detected. If that network has internet, save it in the Set.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onAvailable(android.net.Network)
         */
        override fun onAvailable(network: Network) {
            Log.d(TAG, "onAvailable: $network")
            val networkCapabilities = cm.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            Log.d(TAG, "onAvailable: ${network}, $hasInternetCapability")
            if (hasInternetCapability == true) {
                // check if this network actually has internet
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "onAvailable: adding network. $network")
                        validNetworks.add(network)
                        checkValidNetworks()
                    }
                }
            }
        }
        /*
          If the callback was registered with registerNetworkCallback() it will be called for each network which no longer satisfies the criteria of the callback.
          Source: https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback#onLost(android.net.Network)
         */
        override fun onLost(network: Network) {
            Log.d(TAG, "onLost: $network")
            validNetworks.remove(network)
            checkValidNetworks()
        }
    }
}