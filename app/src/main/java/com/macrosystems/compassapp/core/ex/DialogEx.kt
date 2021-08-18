package com.macrosystems.compassapp.core.ex

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.macrosystems.compassapp.core.utils.DialogFragmentLauncher

fun DialogFragment.showDialog(launcher: DialogFragmentLauncher, activity: FragmentActivity){
    launcher.show(this, activity)
}