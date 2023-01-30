package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
    private lateinit var mReceiver : MainReceiver
    private lateinit var mBinding : FragmentFolderBinding

    fun handle(activity: FragmentActivity, binding: FragmentFolderBinding, fragmentUri: Uri, fragmentDocId: String, callback: (() -> Unit)) {

        mReceiver = (activity as MainReceiver)
        mBinding = binding

        val docUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, fragmentDocId)
        Utils.showPrompt(activity, fun(editText) {
            // onSubmit()
            val filename = editText.text.trim().toString()
            if (filename == "") {
                Utils.showPopup(activity, "Filename is empty") {
                    Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_file) })
                }
                return
            }
            val (base, ext) = Utils.explodeFilename(filename)
            val mimeType = mReceiver.getMimeType(ext)
            DocumentsContract.createDocument(mFragment.requireActivity().contentResolver, docUri, mimeType, base)
            Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_file) })
            editText.text.clear()
            callback()
        }, fun() {
            // onDismiss()
            Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_file) })
        })
    }
}