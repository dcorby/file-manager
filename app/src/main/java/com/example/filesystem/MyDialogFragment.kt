package com.example.filesystem

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.filesystem.actions.DummyDialogCallback


class MyDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        //arguments.
        //private val mBuilder: AlertDialog.Builder = builder
        builder.setTitle("Really?")
        builder.setMessage("Are you sure?")
        //null should be your on click listener
        builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            val callback = targetFragment as? DummyDialogCallback
            callback?.onDummyDialogClick()
        })
        builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
            val callback = targetFragment as? DummyDialogCallback
            callback?.onDummyDialogClick()
        })
        return builder.create()
    }
}