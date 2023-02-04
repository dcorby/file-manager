package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.net.toUri
import androidx.recyclerview.selection.Selection
import com.example.filesystem.*
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

class Copy(fragment: FolderFragment,
           binding: FragmentFolderBinding,
           selection: Selection<String>,
           fragmentUri: Uri,
           fragmentDocId: String,
           callback: (() -> Unit)) : Action {

    private val mFragment = fragment
    private var mActivity = fragment.requireActivity()
    private var mReceiver = fragment.requireActivity() as MainReceiver
    private var mBinding = binding
    private var mSelection = selection
    private var mFragmentUri = fragmentUri
    private var mFragmentDocId = fragmentDocId
    private var mCallback = callback

    override fun handle(isClick: Boolean) {
        if (!validate()) {
            return
        }
        mFragment.actions["move"]?.finish()
        mFragment.currentAction = "copy"

        val sourceUri = mReceiver.getActionState("copy", "sourceUri")
        if (sourceUri == null) {
            // Store data to begin copy, and show status
            val sourceDocId = mSelection.toList()[0]
            val sourceUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, sourceDocId)
            val mimeType = mActivity.contentResolver.getType(sourceUri)
            if (mimeType != null && Utils.isDirectory(mimeType)) {
                UI.showPopup(mFragment, "Folder copy not supported") {
                    mBinding.toggleGroup.uncheck(R.id.action_copy)
                }
                return
            }
            mReceiver.setActionState("copy", "sourceUri", sourceUri.toString())
            mReceiver.setActionState("copy", "sourceDocId", sourceDocId)
            mReceiver.setActionState("copy", "sourceFragmentDocId", mFragmentDocId)
            UI.showStatus(mBinding.status, "Copying", mFragmentDocId, sourceDocId)
            mBinding.close.setOnClickListener { finish() }
        } else {
            if (!isClick) {
                return
            }
            var targetUri: Uri? = null
            var isError = false
            try {
                // Perform the actual copy
                val filename = Utils.getFilenameFromDocId(mReceiver.getActionState("copy", "sourceDocId")!!)
                val parentUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, mFragmentDocId)
                val (_, ext) = Utils.explodeFilename(filename)
                val mimeType = mReceiver.getMimeType(ext)
                targetUri = DocumentsContract.createDocument(mActivity.contentResolver, parentUri, mimeType, filename)
                val inputStream = mActivity.contentResolver.openInputStream(sourceUri.toUri())!!
                val bytes = inputStream.readBytes()
                inputStream.close()
                val outputStream = mActivity.contentResolver.openOutputStream(targetUri!!)!!
                outputStream.write(bytes)
                outputStream.close()
                finish()
                mFragment.observeCurrent(mFragmentDocId)
                return
            } catch(e: Exception) {
                UI.showPopup(mFragment, "Error copying file") {
                    finish()
                }
                isError = true
            } finally {
                if (isError && targetUri != null) {
                    DocumentsContract.deleteDocument(mActivity.contentResolver, targetUri)
                }
            }
        }
    }

    private fun validate() : Boolean {
        val sourceUri = mReceiver.getActionState("copy", "sourceUri")
        // Only need to validate if user hasn't yet selected a file to copy
        if (sourceUri == null) {
            if (mSelection.size() == 0) {
                UI.showPopup(mFragment, "Select a file to copy") {
                    mBinding.toggleGroup.uncheck(R.id.action_copy)
                }
                return false
            }
            if (mSelection.size() > 1) {
                UI.showPopup(mFragment, "Multi-file copy is not supported") {
                    mBinding.toggleGroup.uncheck(R.id.action_copy)
                }
                return false
            }
        }
        return true
    }

    override fun finish() {
        if (mFragment.currentAction == "copy") {
            mFragment.currentAction = null
            mBinding.toggleGroup.uncheck(R.id.action_copy)
            mBinding.close.setOnClickListener(null)
            UI.cleanStatus(mBinding.status)
            mReceiver.setActionState("copy", "sourceUri", null)
            mReceiver.setActionState("copy", "sourceDocId", null)
            mReceiver.setActionState("copy", "sourceFragmentDocId", null)
        }
    }
}