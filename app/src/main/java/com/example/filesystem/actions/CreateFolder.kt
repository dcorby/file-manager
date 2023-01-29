package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

class CreateFolder(fragment: Fragment) {

    private val mFragment = fragment
    private lateinit var mBinding : FragmentFolderBinding

    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, fragmentUri: Uri, fragmentDocId: String) {

        mBinding = binding

        //val filename = mBinding.filename.text.trim().toString()
        val filename = "foo"
        if (filename == "") {
            Utils.showPopup(activity, "Folder name is empty") {
                Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_folder) })
            }
            return
        }

        val docUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, fragmentDocId)
        DocumentsContract.createDocument(mFragment.requireActivity().contentResolver, docUri, DocumentsContract.Document.MIME_TYPE_DIR, filename)
        Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_folder) })
    }
}