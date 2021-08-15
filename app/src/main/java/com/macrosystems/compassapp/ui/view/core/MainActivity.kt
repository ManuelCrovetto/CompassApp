package com.macrosystems.compassapp.ui.view.core

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import com.macrosystems.compassapp.R
import com.macrosystems.compassapp.databinding.ActivityMainBinding
import com.macrosystems.compassapp.ui.core.ConnectionStatusLiveData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var connectionStatusLiveData: ConnectionStatusLiveData
    private var isConnected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setTheme(R.style.SplashTheme)

        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_CompassApp_NoActionBar)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectionStatusLiveData.observe(this, { status->
            if (!status){
                isConnected = false
                val sb = Snackbar.make(binding.root, getString(R.string.lost_connection_default_text), Snackbar.LENGTH_INDEFINITE)
                val sbView=sb.view
                val params: FrameLayout.LayoutParams = sbView.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.TOP
                sbView.layoutParams = params
                sbView.setBackgroundColor(Color.RED)
                sb.show()
            } else {
                if (!isConnected) {
                    val sb = Snackbar.make(binding.root, getString(R.string.connected_successfully_default_text), Snackbar.LENGTH_LONG)
                    val sbView = sb.view
                    val params: FrameLayout.LayoutParams = sbView.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    sbView.layoutParams = params
                    sbView.setBackgroundColor(Color.GREEN)
                    val tv: TextView = sbView.findViewById(com.google.android.material.R.id.snackbar_text)
                    tv.setTextColor(Color.BLACK)
                    sb.show()
                    isConnected = true
                }
            }
        })

    }
}