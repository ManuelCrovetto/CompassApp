package com.macrosystems.compassapp.ui.core.interfaces

interface FragmentListener {
    fun onStarted()
    fun onSuccess(string: String?)
    fun onFailure(string: String?)
}