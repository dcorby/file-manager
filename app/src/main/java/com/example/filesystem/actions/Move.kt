package com.example.filesystem.actions

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.selection.Selection
import com.example.filesystem.*
import com.example.filesystem.databinding.FragmentFolderBinding

class Move(fragment: FolderFragment,
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun handle(isClick: Boolean) {
        if (!validate()) {
            return
        }
        mFragment.actions["copy"]?.finish()
        //mFragment.currentAction = "move"
        mReceiver.setCurrentAction("move")

        val sourceUri = mReceiver.getActionState("move", "sourceUri")
        if (sourceUri == null) {
            // Store data to begin move, and show status
            val sourceDocId = mSelection.toList()[0]
            val sourceUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, sourceDocId)
            mReceiver.setActionState("move","sourceUri", sourceUri.toString())
            mReceiver.setActionState("move","sourceDocId", sourceDocId)
            mReceiver.setActionState("move","sourceFragmentUri", mFragmentUri.toString())
            mReceiver.setActionState("move","sourceFragmentDocId", mFragmentDocId)
            UI.showStatus(mBinding.status, "Moving", mFragmentDocId, sourceDocId)
            mBinding.close.setOnClickListener { finish() }
        } else {
            if (!isClick) {
                return
            }
            // Perform the actual move
            val sourceFragmentUri = Utils.decode(mReceiver.getActionState("move", "sourceFragmentUri")!!)
            val sourceFragmentDocId = Utils.decode(mReceiver.getActionState("move", "sourceFragmentDocId")!!)
            val sourceParentDocUri = DocumentsContract.buildDocumentUriUsingTree(sourceFragmentUri.toUri(), sourceFragmentDocId)
            val targetParentDocUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, mFragmentDocId)

            /* This routine will find files without throwing an exception, but some users report
                that it won't account for recently added/deleted files??
             */
            // val sourceDocId = mReceiver.getActionState("move","sourceDocId")
            // DocumentFile.fromTreeUri(mFragment.requireContext(), mFragmentUri)?.listFiles()?.forEach {
            //     if (it.name == Utils.getFilenameFromDocId(sourceDocId!!)) {
            //     }
            // }

            try {
                DocumentsContract.moveDocument(
                    mActivity.contentResolver,
                    sourceUri.toUri(),
                    sourceParentDocUri,
                    targetParentDocUri)
            } catch (e: IllegalStateException) {
                mBinding.toggleGroup.check(R.id.action_move)
                Toast.makeText(mActivity, "File already exists", Toast.LENGTH_SHORT).show()
                return
            }
            mFragment.observeCurrent(mFragmentDocId)
            finish()
        }
    }

    private fun validate() : Boolean {
        val sourceUri = mReceiver.getActionState("move", "sourceUri")
        // Only need to validate if user hasn't yet selected a file to move
        if (sourceUri == null) {
            if (mSelection.size() == 0) {
                UI.showPopup(mFragment, "Select a file to move") {
                    mBinding.toggleGroup.uncheck(R.id.action_move)
                }
                return false
            }
            if (mSelection.size() > 1) {
                UI.showPopup(mFragment, "Multi-file move is not supported") {
                    mBinding.toggleGroup.uncheck(R.id.action_move)
                }
                return false
            }
        }
        return true
    }

    override fun finish() {
        if (mReceiver.getCurrentAction() == "move") {
            mReceiver.setCurrentAction(null)
            mBinding.toggleGroup.uncheck(R.id.action_move)
            mBinding.close.setOnClickListener(null)
            UI.cleanStatus(mBinding.status)
            mReceiver.setActionState("move", "sourceUri", null)
            mReceiver.setActionState("move", "sourceDocId", null)
            mReceiver.setActionState("move", "sourceParentUri", null)
            mReceiver.setActionState("move", "sourceParentDocId", null)
        }
    }
}