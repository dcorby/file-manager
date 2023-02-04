package com.example.filesystem.actions

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
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
    override fun handle() {
        mFragment.currentAction = "move"
        if (!validate()) {
            return
        }

        val sourceUri = mReceiver.getActionState("move", "sourceUri")
        if (sourceUri == null) {
            val sourceDocId = mSelection.toList()[0]
            val sourceUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, sourceDocId)
            mReceiver.setActionState("move","sourceUri", sourceUri.toString())
            mReceiver.setActionState("move","sourceDocId", sourceDocId)
            mReceiver.setActionState("move","sourceParentUri", mFragmentUri.toString())
            mReceiver.setActionState("move","sourceParentDocId", mFragmentDocId)
            return
        } else {
            val sourceParentUri = Utils.decode(mReceiver.getActionState("move", "sourceParentUri")!!)
            val sourceParentDocId = Utils.decode(mReceiver.getActionState("move", "sourceParentDocId")!!)
            val sourceParentDocUri = DocumentsContract.buildDocumentUriUsingTree(sourceParentUri.toUri(), sourceParentDocId)
            val targetParentDocUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, mFragmentDocId)
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
            mReceiver.setActionState("move","sourceUri", null)
            mReceiver.setActionState("move","sourceDocId", null)
            mReceiver.setActionState("move","sourceParentUri", null)
            mReceiver.setActionState("move","sourceParentDocId", null)
            return
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
        mFragment.currentAction = null
        mBinding.toggleGroup.uncheck(R.id.action_move)
        mBinding.close.setOnClickListener(null)
        mReceiver.setActionState("move","sourceUri", null)
        mReceiver.setActionState("move","sourceParentUri", null)
        mReceiver.setActionState("move","sourceParentDocId", null)
    }
}