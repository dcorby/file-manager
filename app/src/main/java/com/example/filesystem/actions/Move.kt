package com.example.filesystem.actions

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.MainReceiver
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

class Move(fragment: Fragment) {

    private lateinit var mActivity : FragmentActivity
    private lateinit var mReceiver : MainReceiver
    private lateinit var mBinding : FragmentFolderBinding
    private lateinit var mSelection : Selection<String>

    @RequiresApi(Build.VERSION_CODES.N)
    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, selection: Selection<String>, fragmentUri: Uri, fragmentDocId: String) : Boolean {
        mActivity = activity
        mReceiver = (activity as MainReceiver)
        mBinding = binding
        mSelection = selection
        if (!validate()) {
            return false
        }
        val sourceUri = mReceiver.getActionState("Move", "sourceUri")
        if (sourceUri == null) {
            val sourceDocId = selection.toList()[0]
            val sourceUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, sourceDocId)
            mReceiver.setActionState("Move","sourceUri", sourceUri.toString())
            mReceiver.setActionState("Move","sourceParentUri", fragmentUri.toString())
            mReceiver.setActionState("Move","sourceParentDocId", fragmentDocId)
            return false
        } else {
            val sourceParentUri = Utils.decode(mReceiver.getActionState("Move", "sourceParentUri")!!)
            val sourceParentDocId = Utils.decode(mReceiver.getActionState("Move", "sourceParentDocId")!!)
            val sourceParentDocUri = DocumentsContract.buildDocumentUriUsingTree(sourceParentUri.toUri(), sourceParentDocId)
            val targetParentDocUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, fragmentDocId)
            try {
                DocumentsContract.moveDocument(activity.contentResolver, sourceUri.toUri(), sourceParentDocUri, targetParentDocUri)
            } catch (e: IllegalStateException) {
                mBinding.toggleGroup.check(R.id.action_move)
                Toast.makeText(mActivity, "File already exists", Toast.LENGTH_SHORT).show()
                return false
            }
            mReceiver.setActionState("Move","sourceUri", null)
            mReceiver.setActionState("Move","sourceParentUri", null)
            mReceiver.setActionState("Move","sourceParentDocId", null)
            return true
        }
    }

    private fun validate() : Boolean {
        val sourceUri = mReceiver.getActionState("Move", "sourceUri")
        // Only need to validate if user hasn't yet selected a file to move
        if (sourceUri == null) {
            if (mSelection.size() == 0) {
                Utils.showPopup(mActivity, "Select a file to move") {
                    mBinding.toggleGroup.uncheck(R.id.action_move)
                }
                return false
            }
            if (mSelection.size() > 1) {
                Utils.showPopup(mActivity, "Multi-file move is not supported") {
                    mBinding.toggleGroup.uncheck(R.id.action_move)
                }
                return false
            }
        }
        return true
    }
}