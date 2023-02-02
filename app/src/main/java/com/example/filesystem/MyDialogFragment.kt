package com.example.filesystem

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment

// https://lukeneedham.medium.com/listeners-in-dialogfragments-be636bd7f480
// https://stackoverflow.com/questions/64869501/how-to-replace-settargetfragment-now-that-it-is-deprecated
class MyDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val uriStr = requireArguments().getString("uri")
        val uri = Uri.parse(uriStr)

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm delete")
        builder.setMessage("Delete?")
        builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            val callback = targetFragment as? DialogCallback
            callback?.onDialogClickYes(uri)
        })
        builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
            val callback = targetFragment as? DialogCallback
            callback?.onDialogClickNo()
        })
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val callback = targetFragment as? DialogCallback
        callback?.onDismiss()
    }
}