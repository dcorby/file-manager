package com.example.filesystem.actions

import android.net.Uri
import android.os.Handler
import android.provider.DocumentsContract
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.selection.Selection
import com.example.filesystem.FolderFragment
import com.example.filesystem.MyDialogFragment
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

interface DialogCallback {
    fun onDialogClickYes()
    fun onDialogClickNo()
}

class Delete(fragment: FolderFragment) : DialogCallback {
    private val mFragment = fragment
    private lateinit var mBinding : FragmentFolderBinding
    private lateinit var mSelection : Selection<String>
    private lateinit var mFinish : (Boolean) -> Unit
    private lateinit var mUri : Uri

    fun handle(activity: FragmentActivity,
               binding: FragmentFolderBinding,
               selection: Selection<String>,
               fragmentUri: Uri,
               finish: ((Boolean) -> Unit)) {

        mBinding = binding
        mSelection = selection
        mFinish = finish

        if (!validate()) {
            return
        }

        val docId = mSelection.toList()[0]
        mUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, docId)
        Handler().post {
            if (!mFragment.requireActivity().isFinishing) {
                // https://lukeneedham.medium.com/listeners-in-dialogfragments-be636bd7f480
                // https://stackoverflow.com/questions/64869501/how-to-replace-settargetfragment-now-that-it-is-deprecated

                val fragmentManager: FragmentManager = mFragment.parentFragmentManager
                var dialogFragment: DialogFragment? = fragmentManager.findFragmentByTag("dialog") as DialogFragment?
                // dialogFragment?.dismiss()
                // ^ this is in FolderFragment onResume(). It's a smoother UI effect there
                dialogFragment = MyDialogFragment()
                dialogFragment.setTargetFragment(mFragment, 1)
                dialogFragment.show(mFragment.requireFragmentManager(), "dialog")
            }
        }
    }

    private fun validate() : Boolean {
        if (mSelection.size() == 0) {
            Utils.showPopup(mFragment, "Select a file to delete") {
                mBinding.toggleGroup.uncheck(R.id.action_delete)
                mFinish(false)
            }
            return false
        }
        if (mSelection.size() > 1) {
            Utils.showPopup(mFragment, "Multi-file delete is not supported") {
                mBinding.toggleGroup.uncheck(R.id.action_delete)
                mFinish(false)
            }
            return false
        }
        return true
    }

    override fun onDialogClickYes() {
        DocumentsContract.deleteDocument(mFragment.requireActivity().contentResolver, mUri)
        Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_delete) })
        mFinish(true)
    }

    override fun onDialogClickNo() {
        Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_delete) })
        mFinish(false)
    }
}