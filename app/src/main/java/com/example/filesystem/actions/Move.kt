package com.example.filesystem.actions

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.MainReceiver
import com.example.filesystem.Utils

class Move(fragment: Fragment) {

    private val mFragment = fragment
    private lateinit var mReceiver : MainReceiver
    private lateinit var mSelection : Selection<String>

    @RequiresApi(Build.VERSION_CODES.N)
    fun handle(activity: FragmentActivity, selection: Selection<String>, fragmentUri: Uri, fragmentDocId: String) {
        mReceiver = (activity as MainReceiver)
        mSelection = selection

        val sourceUri = mReceiver.getActionState("Move", "sourceUri")
        if (sourceUri == null) {
            val sourceDocId = selection.toList()[0]
            val sourceUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, sourceDocId)
            mReceiver.setActionState("Move","sourceUri", sourceUri.toString())
            mReceiver.setActionState("Move","sourceParentUri", fragmentUri.toString())
            mReceiver.setActionState("Move","sourceParentDocId", fragmentDocId)
        } else {
            val sourceParentUri = Utils.decode(mReceiver.getActionState("Move", "sourceParentUri")!!)
            val sourceParentDocId = Utils.decode(mReceiver.getActionState("Move", "sourceParentDocId")!!)
            val sourceParentDocUri = DocumentsContract.buildDocumentUriUsingTree(sourceParentUri.toUri(), sourceParentDocId)
            val targetParentDocUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, fragmentDocId)
            DocumentsContract.moveDocument(activity.contentResolver, sourceUri.toUri(), sourceParentDocUri, targetParentDocUri)
        }
    }
}