package com.macrosystems.compassapp.core.ex

import android.app.Activity
import android.widget.Toast


fun Activity.shortToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Activity.longToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}