package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.MainReceiver
import com.example.filesystem.Utils

/*
  https://stackoverflow.com/questions/61687463/documentscontract-copydocument-always-fails
  https://stackoverflow.com/questions/13133579/android-save-a-file-from-an-existing-uri
  copydocument() does not work, well-documented bug or non-implementation

  Copying bytes has issues too, over MTP: https://issuetracker.google.com/issues/36956498
  FileManager may not refresh on the host machine

  DocumentsContract.copyDocument(requireContext().contentResolver, copyFromUri.toUri(), targetDocumentParentUri)
  This fails on most Android devices up to SDK32 with "java.lang.UnsupportedOperationException: Copy not supported"
 */

class Copy(fragment: Fragment) {

    private val mFragment = fragment
    private lateinit var mReceiver : MainReceiver
    private lateinit var mSelection : Selection<String>

    fun handle(activity: FragmentActivity, selection: Selection<String>, fragmentUri: Uri, fragmentDocId: String) : Boolean {
        mReceiver = (activity as MainReceiver)
        mSelection = selection

        val sourceUri = mReceiver.getActionState("Copy", "sourceUri")
        if (sourceUri == null) {
            // Get file info and prepare to copy
            var popupText = ""
            if (mSelection.size() == 0) {
                popupText = "Select a file to copy"
            }
            if (mSelection.size() > 1) {
                popupText = "Multi-file copy is not supported"
            }
            if (popupText != "") {
                Utils.showPopup(activity, popupText)
                return false
            }
            val sourceDocId = mSelection.toList()[0]
            val sourceUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, sourceDocId)
            mReceiver.setActionState("Copy", "sourceUri", sourceUri.toString())
            mReceiver.setActionState("Copy", "filename", Utils.getFilenameFromDocId(sourceDocId))
            return false
        } else {
            // Make the actual copy
            val filename = mReceiver.getActionState("Copy", "filename")!!
            val parentUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, fragmentDocId)
            val targetUri = DocumentsContract.createDocument(activity.contentResolver, parentUri, "text/plain", filename)
            val inputStream = activity.contentResolver.openInputStream(sourceUri.toUri())!!
            val bytes = inputStream.readBytes()
            inputStream.close()
            val outputStream = activity.contentResolver.openOutputStream(targetUri!!)!!
            outputStream.write(bytes)
            outputStream.close()
            mReceiver.setActionState("Copy", "sourceUri", null)
            mReceiver.setActionState("Copy", "filename", null)
            return true
        }
    }
}