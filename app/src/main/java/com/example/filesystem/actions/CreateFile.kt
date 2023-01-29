package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.MainReceiver
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

/*
  Get the docUri from the destination vars
  It might make sense to build docUri on the fly, e.g.
  var docUri = DocumentFile.fromTreeUri(requireContext(), destinationUri)!!.uri
  However, this *always* yields the root docUri. Is this a bug?
  This thread suggests so: https://stackoverflow.com/questions/62375696/unexpected-behavior-when-documentfile-fromtreeuri-is-called-on-uri-of-subdirec
*/

class CreateFile(fragment: Fragment) {

    private val mFragment = fragment
    private lateinit var mActivity : FragmentActivity
    private lateinit var mReceiver : MainReceiver
    private lateinit var mBinding : FragmentFolderBinding
    private lateinit var mSelections : Selection<String>

    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, fragmentUri: Uri, fragmentDocId: String) {

        mReceiver = (activity as MainReceiver)
        mBinding = binding

        val docUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, fragmentDocId)
        val filename = mBinding.filename.text.trim().toString()
        if (filename == "") {
            Utils.showPopup(activity, "Filename is empty")
            return
        }

        var ext: String? = null
        var name: String? = null
        val parts = filename.split(".")
        if (parts.size == 1) {
            ext = "bin"  // maps to application/octet-stream
            name = filename
        } else {
            ext = parts.last()
            name = parts.dropLast(1).joinToString(".")
        }
        val mimeType = mReceiver.getMimeType(ext) as String
        DocumentsContract.createDocument(mFragment.requireActivity().contentResolver, docUri, mimeType, name)

        Utils.withDelay{ mBinding.toggleGroup1.uncheck(R.id.action_create_file) }
        mBinding.filename.text.clear()
    }
}