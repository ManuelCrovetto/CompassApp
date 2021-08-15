package com.macrosystems.compassapp.ui.view.dialogs

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.macrosystems.compassapp.databinding.ErrorDialogBinding

class ErrorDialog (context: Context, private val personalizedErrorMessage: String?, val listener: () -> Unit): AppCompatDialog(context) {

    private lateinit var binding: ErrorDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ErrorDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        personalizedErrorMessage?.let {
            binding.tvErrorMessage.text = it
        }

        binding.btnOkErrorDialog.setOnClickListener {
            listener()
            this.cancel()
        }
    }
}