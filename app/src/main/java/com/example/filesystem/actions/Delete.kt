package com.example.filesystem.actions

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import androidx.recyclerview.selection.Selection
import com.example.filesystem.*
import com.example.filesystem.databinding.FragmentFolderBinding

class Delete(fragment: FolderFragment,
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
        if (!validate()) {
            return
        }
        //mFragment.currentAction = "delete"
        mReceiver.setCurrentAction("delete")

        val docId = mSelection.toList()[0]
        val docUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, docId)
        Handler().post {
            if (!mFragment.requireActivity().isFinishing) {
                // https://lukeneedham.medium.com/listeners-in-dialogfragments-be636bd7f480
                // https://stackoverflow.com/questions/64869501/how-to-replace-settargetfragment-now-that-it-is-deprecated

                // val fragmentManager: FragmentManager = mFragment.parentFragmentManager
                // var dialogFragment: DialogFragment? = fragmentManager.findFragmentByTag("dialog") as DialogFragment?
                // dialogFragment?.dismiss()
                // ^ this is in FolderFragment onResume(). It's a smoother UI effect there
                val dialogFragment = MyDialogFragment()
                val bundle = Bundle()
                bundle.putString("uri", docUri.toString())
                dialogFragment.arguments = bundle
                dialogFragment.setTargetFragment(mFragment, 1)
                dialogFragment.show(mFragment.requireFragmentManager(), "dialog")
            }
        }
    }

    private fun validate() : Boolean {
        if (mSelection.size() == 0) {
            UI.showPopup(mFragment, "Select a file to delete") {
                mBinding.toggleGroup.uncheck(R.id.action_delete)
            }
            return false
        }
        if (mSelection.size() > 1) {
            UI.showPopup(mFragment, "Multi-file delete is not supported") {
                mBinding.toggleGroup.uncheck(R.id.action_delete)
            }
            return false
        }
        return true
    }

    override fun finish() {

    }
}