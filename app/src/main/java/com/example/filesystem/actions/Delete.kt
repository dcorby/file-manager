package com.example.filesystem.actions

import android.app.AlertDialog
import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

class Delete(fragment: Fragment) {

    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, selection: Selection<String>, fragmentUri: Uri, callback: (() -> Unit)) {

        val docId = selection.toList()[0]
        val uri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, docId)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage("Are you sure?")
        builder.setPositiveButton("Yes") { _, _ ->
            DocumentsContract.deleteDocument(activity.contentResolver, uri)
            Utils.withDelay({ binding.toggleGroup.uncheck(R.id.action_delete) })
            callback()
        }
        builder.setNegativeButton("No") { _, _ ->
            Utils.withDelay({ binding.toggleGroup.uncheck(R.id.action_delete) })
            callback()
        }
        builder.show()
    }
}