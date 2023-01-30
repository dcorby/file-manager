package com.example.filesystem.actions

import android.R.attr.mimeType
import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.MainReceiver
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

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

    private lateinit var mActivity : FragmentActivity
    private lateinit var mReceiver : MainReceiver
    private lateinit var mBinding : FragmentFolderBinding
    private lateinit var mSelection : Selection<String>

    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, selection: Selection<String>, fragmentUri: Uri, fragmentDocId: String) : Boolean {
        mActivity = activity
        mReceiver = (activity as MainReceiver)
        mBinding = binding
        mSelection = selection
        if (!validate()) {
            return false
        }
        val sourceUri = mReceiver.getActionState("Copy", "sourceUri")
        if (sourceUri == null) {
            val sourceDocId = mSelection.toList()[0]
            val sourceUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, sourceDocId)
            val mimeType = mActivity.contentResolver.getType(sourceUri)
            if (mimeType != null && Utils.isDirectory(mimeType)) {
                Utils.showPopup(mActivity, "Folder copy not supported") {
                    mBinding.toggleGroup.uncheck(R.id.action_copy)
                }
                return false
            }
            mReceiver.setActionState("Copy", "sourceUri", sourceUri.toString())
            mReceiver.setActionState("Copy", "filename", Utils.getFilenameFromDocId(sourceDocId))
            return false
        } else {
            var targetUri: Uri? = null
            var isError = false
            try {
                // Make the actual copy
                val filename = mReceiver.getActionState("Copy", "filename")!!
                val parentUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, fragmentDocId)
                val (_, ext) = Utils.explodeFilename(filename)
                val mimeType = mReceiver.getMimeType(ext)
                targetUri = DocumentsContract.createDocument(mActivity.contentResolver, parentUri, mimeType, filename)
                val inputStream = mActivity.contentResolver.openInputStream(sourceUri.toUri())!!
                val bytes = inputStream.readBytes()
                inputStream.close()
                val outputStream = mActivity.contentResolver.openOutputStream(targetUri!!)!!
                outputStream.write(bytes)
                outputStream.close()
                mReceiver.setActionState("Copy", "sourceUri", null)
                mReceiver.setActionState("Copy", "filename", null)
                return true
            } catch(e: Exception) {
                Utils.showPopup(mActivity, "Error copying file") {
                    mBinding.toggleGroup.uncheck(R.id.action_copy)
                    mReceiver.setActionState("Copy", "sourceUri", null)
                    mReceiver.setActionState("Copy", "filename", null)
                }
                isError = true
                return false
            } finally {
                if (isError && targetUri != null) {
                    DocumentsContract.deleteDocument(mActivity.contentResolver, targetUri)
                }
            }
        }
    }

    private fun validate() : Boolean {
        val sourceUri = mReceiver.getActionState("Copy", "sourceUri")
        // Only need to validate if user hasn't yet selected a file to copy
        if (sourceUri == null) {
            if (mSelection.size() == 0) {
                Utils.showPopup(mActivity, "Select a file to copy") {
                    mBinding.toggleGroup.uncheck(R.id.action_copy)
                }
                return false
            }
            if (mSelection.size() > 1) {
                Utils.showPopup(mActivity, "Multi-file copy is not supported") {
                    mBinding.toggleGroup.uncheck(R.id.action_copy)
                }
                return false
            }
        }
        return true
    }
}