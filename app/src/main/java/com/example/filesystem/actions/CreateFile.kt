package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.recyclerview.selection.Selection
import com.example.filesystem.*
import com.example.filesystem.databinding.FragmentFolderBinding

/*
  Get the docUri from the destination vars
  It might make sense to build docUri on the fly, e.g.
  var docUri = DocumentFile.fromTreeUri(requireContext(), destinationUri)!!.uri
  However, this *always* yields the root docUri. Is this a bug?
  This thread suggests so: https://stackoverflow.com/questions/62375696/unexpected-behavior-when-documentfile-fromtreeuri-is-called-on-uri-of-subdirec
*/

class CreateFile(fragment: FolderFragment,
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
        mReceiver.setCurrentAction("createFile")

        val docUri = DocumentsContract.buildDocumentUriUsingTree(mFragmentUri, mFragmentDocId)
        UI.showPrompt(mFragment,
            fun(editText) {
                // onSubmit()
                val filename = editText.text.trim().toString()
                if (filename == "") {
                    UI.showPopup(mFragment, "Filename is empty") {
                        Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_file) })
                    }
                    finish()
                    return
                } else {
                    val (base, ext) = Utils.explodeFilename(filename)
                    val mimeType = mReceiver.getMimeType(ext)
                    DocumentsContract.createDocument(
                        mFragment.requireActivity().contentResolver,
                        docUri,
                        mimeType,
                        base
                    )
                    Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_file) })
                    editText.text.clear()
                    mFragment.observeCurrent(mFragmentDocId)
                    finish()
                }
            },
            fun() {
                // onDismiss()
                Utils.withDelay({ mBinding.toggleGroup.uncheck(R.id.action_create_file) }) {
                    finish()
                }
        })
    }

    override fun finish() {
        mReceiver.setCurrentAction(null)
        mFragment.savedText = ""
    }
}