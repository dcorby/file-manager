package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.recyclerview.selection.Selection
import com.example.filesystem.*
import com.example.filesystem.databinding.FragmentFolderBinding

class CreateFolder(fragment: FolderFragment,
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
        mReceiver.setCurrentAction("createFolder")

        UI.showPrompt(mFragment,
            fun(editText) {
                // onSubmit()
                val filename = editText.text.trim().toString()
                if (filename == "") {
                    UI.showPopup(mFragment, "Folder name is empty") {
                        Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_folder) })
                    }
                    return
                }

                val docUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, mFragmentDocId)
                DocumentsContract.createDocument(
                    mFragment.requireActivity().contentResolver,
                    docUri,
                    DocumentsContract.Document.MIME_TYPE_DIR,
                    filename)
                Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_folder) })
                editText.text.clear()
                mFragment.observeCurrent(mFragmentDocId)
                finish()
            },
            fun() {
                // onDismiss()
                Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_folder) }) {
                    finish()
                }
            })
    }

    override fun finish() {
        mReceiver.setCurrentAction(null)
        mFragment.savedText = ""
    }
}