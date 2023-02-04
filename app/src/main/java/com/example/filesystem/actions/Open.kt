package com.example.filesystem.actions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.Navigation
import androidx.recyclerview.selection.Selection
import com.example.filesystem.*
import com.example.filesystem.databinding.FragmentFolderBinding

class Open(fragment: FolderFragment,
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

    // Support persistent copy and move actions
    private var mPreviousAction: String? = null

    override fun handle(isClick: Boolean) {
        if (!validate()) {
            return
        }
        mPreviousAction = mFragment.currentAction
        mFragment.currentAction = "open"

        val docId = mSelection.toList()[0]
        val docUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, docId)
        val docTreeUri = DocumentsContract.buildTreeDocumentUri(docUri.authority, docId)
        val isDir = mActivity.contentResolver.getType(docUri) == DocumentsContract.Document.MIME_TYPE_DIR
        if (isDir) {
            // Folder
            val navController = Navigation.findNavController(mFragment.requireActivity(), R.id.nav_host_fragment_content_main)
            val bundle = Bundle()
            bundle.putString("fragmentUri", Utils.decode(docTreeUri.toString()))
            bundle.putString("fragmentDocId", docId)
            +++++++++++++++++++++++++++++++++++
            + NEED TO SEND CURRENTACTION HERE +
            +++++++++++++++++++++++++++++++++++
            navController.navigate(R.id.action_FolderFragment_to_FolderFragment, bundle)
        } else {
            // File
            val intent: Intent = Intent().apply {
                action = Intent.ACTION_EDIT
                setDataAndType(docUri, "text/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivityForResult(mActivity, Intent.createChooser(intent, null), 1, null)
            // Could launch onActivityResult() per: https://stackoverflow.com/questions/38198264/is-there-an-event-when-user-cancels-intent-chooser
        }
        finish()
    }

    private fun validate() : Boolean {
        if (mSelection.size() == 0) {
            UI.showPopup(mFragment, "Select a file to open") {
                mBinding.toggleGroup.uncheck(R.id.action_open)
            }
            return false
        }
        if (mSelection.size() > 1) {
            UI.showPopup(mFragment, "Only one file may be selected for open") {
                mBinding.toggleGroup.uncheck(R.id.action_open)
            }
            return false
        }
        return true
    }

    override fun finish() {
        mBinding.toggleGroup.uncheck(R.id.action_open)
        mFragment.currentAction = mPreviousAction
    }
}