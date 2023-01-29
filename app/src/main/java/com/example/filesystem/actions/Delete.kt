package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

class Delete(fragment: Fragment) {

    private val mFragment = fragment
    private lateinit var mActivity : FragmentActivity
    private lateinit var mBinding : FragmentFolderBinding
    private lateinit var mSelections : Selection<String>

    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, selection: Selection<String>, fragmentUri: Uri) {
        val docId = selection.toList()[0]
        val uri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, docId)
        DocumentsContract.deleteDocument(activity.contentResolver, uri)
        //sanFilesViewModel.removeSanFile(docIdToDelete)
        Utils.withDelay{ binding.toggleGroup2.uncheck(R.id.action_delete) }
    }
}