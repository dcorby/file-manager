package com.example.filesystem.actions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
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

    override fun handle(isClick: Boolean) {
        mReceiver.setCurrentAction("open")
        mBinding.toggleGroup.check(R.id.action_open)
        if (!validate()) {
            return
        }

        val docId = mSelection.toList()[0]
        val docUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, docId)
        val docTreeUri = DocumentsContract.buildTreeDocumentUri(docUri.authority, docId)
        val isDir = mActivity.contentResolver.getType(docUri) == DocumentsContract.Document.MIME_TYPE_DIR
        if (isDir) {
            // Folder
            val navController =
                Navigation.findNavController(mFragment.requireActivity(), R.id.nav_host_fragment_content_main)
            val bundle = Bundle()
            bundle.putString("fragmentUri", Utils.decode(docTreeUri.toString()))
            bundle.putString("fragmentDocId", docId)
            finish()
            navController.navigate(R.id.action_FolderFragment_to_FolderFragment, bundle)
        } else {
            // File
            val intent: Intent = Intent().apply {
                action = Intent.ACTION_EDIT
                setDataAndType(docUri, "text/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            mFragment.startActivityForResult(Intent.createChooser(intent, null), mFragment.OPEN_DOCUMENT_REQUEST_CODE)
            finish()
        }
    }

    private fun validate() : Boolean {
        if (mSelection.size() == 0) {
            UI.showPopup(mFragment, "Select a file to open") {
                mBinding.toggleGroup.uncheck(R.id.action_open)
                finish()
            }
            return false
        }
        if (mSelection.size() > 1) {
            UI.showPopup(mFragment, "Only one file may be selected for open") {
                mBinding.toggleGroup.uncheck(R.id.action_open)
                finish()
            }
            return false
        }
        return true
    }

    override fun finish() {
        mReceiver.setCurrentAction(null)
        when {
            mReceiver.getActionState("copy", "sourceUri") != null -> mReceiver.setCurrentAction("copy")
            mReceiver.getActionState("move", "sourceUri") != null -> mReceiver.setCurrentAction("move")
        }
    }
}