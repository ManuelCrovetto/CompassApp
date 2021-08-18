package com.macrosystems.compassapp.ui.view.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import com.macrosystems.compassapp.databinding.ErrorDialogBinding

class ErrorDialog: DialogFragment() {
    private var title: String = ""
    private var textMessage: String = ""
    private var isDialogCancelable: Boolean = true
    private var positiveAction: Action = Action.Empty
    private var negativeAction: Action = Action.Empty

    companion object {
        fun create(
            title: String = "",
            textMessage: String = "",
            isDialogCancelable: Boolean = true,
            positiveAction: Action = Action.Empty,
            negativeAction: Action = Action.Empty
        ) : ErrorDialog = ErrorDialog().apply {
            this.title = title
            this.textMessage = textMessage
            this.isDialogCancelable = isDialogCancelable
            this.positiveAction = positiveAction
            this.negativeAction = negativeAction
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return
        window.setLayout(MATCH_PARENT, WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = ErrorDialogBinding.inflate(requireActivity().layoutInflater)

        binding.tvErrorMessage.text = textMessage
        if (positiveAction == Action.Empty){
            binding.btnOkErrorDialog.isGone
        } else {
            binding.btnOkErrorDialog.text = positiveAction.text
            binding.btnOkErrorDialog.setOnClickListener { positiveAction.onClickListener(this) }
        }

        isCancelable = isDialogCancelable

        return AlertDialog.Builder(requireActivity()).setView(binding.root).setCancelable(isDialogCancelable).create()
    }

    data class Action(val text: String, val onClickListener: (ErrorDialog) -> Unit) {
        companion object {
            val Empty = Action("") {}
        }
    }

}

