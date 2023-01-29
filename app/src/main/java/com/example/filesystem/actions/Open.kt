package com.example.filesystem.actions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.recyclerview.selection.Selection
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

class Open(fragment: Fragment) {

    private val mFragment = fragment
    private lateinit var mActivity : FragmentActivity
    private lateinit var mBinding : FragmentFolderBinding
    private lateinit var mSelections : Selection<String>

    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, selections: Selection<String>, destinationUri: Uri) {
        mActivity = activity
        mBinding = binding
        mSelections = selections
        if (!validate()) {
            return
        }
        Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_open) }, {
            val docId = selections.toList()[0]
            val docUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, docId)
            val docTreeUri = DocumentsContract.buildTreeDocumentUri(docUri.authority, docId)
            val isDir = activity.contentResolver.getType(docUri) == DocumentsContract.Document.MIME_TYPE_DIR
            if (isDir) {
                // Folder
                val navController =
                    Navigation.findNavController(mFragment.requireActivity(), R.id.nav_host_fragment_content_main)
                val bundle = Bundle()
                bundle.putString("fragmentUri", Utils.decode(docTreeUri.toString()))
                bundle.putString("fragmentDocId", docId)
                navController.navigate(R.id.action_FolderFragment_to_FolderFragment, bundle)
            } else {
                // File
                val intent: Intent = Intent().apply {
                    action = Intent.ACTION_EDIT
                    setDataAndType(docUri, "text/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(activity, Intent.createChooser(intent, null), null)
            }
        })
    }

    private fun validate() : Boolean {
        if (mSelections.size() == 0) {
            Utils.showPopup(mActivity, "Select a file to open") {
                mBinding.toggleGroup.uncheck(R.id.action_open)
            }
            return false
        }
        if (mSelections.size() > 1) {
            Utils.showPopup(mActivity, "Only one file may be selected for open") {
                mBinding.toggleGroup.uncheck(R.id.action_open)
            }
            return false
        }
        return true
    }
}