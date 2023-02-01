package com.example.filesystem

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.filesystem.actions.DialogCallback

// https://lukeneedham.medium.com/listeners-in-dialogfragments-be636bd7f480
// https://stackoverflow.com/questions/64869501/how-to-replace-settargetfragment-now-that-it-is-deprecated
class MyDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm delete")
        builder.setMessage("Delete?")
        builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            val callback = targetFragment as? DialogCallback
            callback?.onDialogClickYes()
        })
        builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
            val callback = targetFragment as? DialogCallback
            callback?.onDialogClickNo()
        })
        return builder.create()
    }
}