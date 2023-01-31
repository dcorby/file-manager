package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.FragmentActivity
import com.example.filesystem.FolderFragment
import com.example.filesystem.MainActivity
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

class CreateFolder(fragment: FolderFragment) {
    private val mFragment = fragment
    private lateinit var mActivity : FragmentActivity
    private lateinit var mBinding : FragmentFolderBinding

    fun handle(binding: FragmentFolderBinding, fragmentUri: Uri, fragmentDocId: String, callback: (() -> Unit)) {
        mBinding = binding

        Utils.showPrompt(mFragment, fun(editText) {
            // onSubmit()
            val filename = editText.text.trim().toString()
            if (filename == "") {
                Utils.showPopup(mFragment, "Folder name is empty") {
                    Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_folder) })
                }
                return
            }

            val docUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, fragmentDocId)
            DocumentsContract.createDocument(
                mFragment.requireActivity().contentResolver,
                docUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                filename)
            Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_folder) })
            editText.text.clear()
            callback()
        }, fun() {
            // onDismiss()
            Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_folder) })
        })
    }
}