package com.macrosystems.compassapp.ui.core

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import com.macrosystems.compassapp.R
import com.macrosystems.compassapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setTheme(R.style.SplashTheme)

        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_CompassApp_NoActionBar)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}