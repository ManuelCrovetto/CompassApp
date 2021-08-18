package com.macrosystems.compassapp.core.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.macrosystems.compassapp.core.delegate.weak
import javax.inject.Inject

class DialogFragmentLauncher @Inject constructor(): LifecycleObserver{
    private var activity: FragmentActivity? by weak()
    private var dialogFragment: DialogFragment? by weak()

    fun show(dialogFragment: DialogFragment, activity: FragmentActivity){
        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)){
            dialogFragment.show(activity.supportFragmentManager, null)
        } else {
            this.activity = activity
            this.dialogFragment = dialogFragment
            activity.lifecycle.addObserver(this@DialogFragmentLauncher)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onActivityResumed() {
        val activity = activity ?: return
        val dialogFragment = dialogFragment ?: return

        dialogFragment.show(activity.supportFragmentManager, null)
        activity.lifecycle.removeObserver(this)
    }

}