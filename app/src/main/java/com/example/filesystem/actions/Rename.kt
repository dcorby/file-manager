package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

class Rename(fragment: Fragment) {

    private val mFragment = fragment
    private lateinit var mActivity : FragmentActivity
    private lateinit var mBinding : FragmentFolderBinding
    private lateinit var mSelection : Selection<String>

    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, selection: Selection<String>, fragmentUri: Uri, callback: (() -> Unit)) {
        mActivity = activity
        mBinding = binding
        mSelection = selection
        if (!validate()) {
            return
        }
        Utils.showPrompt(mActivity, fun(editText) {
            // onSubmit()
            val filename = editText.text.trim().toString()
            val docId = mSelection.toList()[0]
            val parentUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, docId)
            val docUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, docId)
            DocumentsContract.renameDocument(mFragment.requireActivity().contentResolver, docUri, filename)

            Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_rename) })
            editText.text.clear()
            callback()
        }, fun() {
            // onDismiss()
            Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_rename) })
        })
    }

    private fun validate() : Boolean {
        if (mSelection.size() == 0) {
            Utils.showPopup(mActivity, "Select a file to rename") {
                mBinding.toggleGroup.uncheck(R.id.action_rename)
            }
            return false
        }
        if (mSelection.size() > 1) {
            Utils.showPopup(mActivity, "Only one file may be selected for rename") {
                mBinding.toggleGroup.uncheck(R.id.action_rename)
            }
            return false
        }
        return true
    }
}