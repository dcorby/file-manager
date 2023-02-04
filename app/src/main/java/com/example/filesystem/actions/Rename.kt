package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.recyclerview.selection.Selection
import com.example.filesystem.*
import com.example.filesystem.databinding.FragmentFolderBinding

class Rename(fragment: FolderFragment,
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

    override fun handle() {
        mFragment.currentAction = "rename"
        if (!validate()) {
            return
        }

        UI.showPrompt(mFragment,
            fun(editText) {
                // onSubmit()
                val filename = editText.text.trim().toString()
                val docId = mSelection.toList()[0]
                val parentUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, docId)
                val docUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, docId)
                DocumentsContract.renameDocument(mFragment.requireActivity().contentResolver, docUri, filename)
                Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_rename) })
                editText.text.clear()
                mFragment.observeCurrent(mFragmentDocId)
                finish()
            },
            fun() {
                // onDismiss()
                Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_rename) }) {
                    finish()
                }
            })
    }

    private fun validate() : Boolean {
        if (mSelection.size() == 0) {
            UI.showPopup(mFragment, "Select a file to rename") {
                mBinding.toggleGroup.uncheck(R.id.action_rename)
            }
            return false
        }
        if (mSelection.size() > 1) {
            UI.showPopup(mFragment, "Only one file may be selected for rename") {
                mBinding.toggleGroup.uncheck(R.id.action_rename)
            }
            return false
        }
        return true
    }

    override fun finish() {
        mFragment.currentAction = null
    }
}