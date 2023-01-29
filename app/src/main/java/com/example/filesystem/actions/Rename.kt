package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.Selection

class Rename(fragment: Fragment) {

    private val mFragment = fragment

    fun handle(selection: Selection<String>, fragmentUri: Uri) {
        val docId = selection.toList()[0]
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, docId)
        val docUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, docId)
        DocumentsContract.renameDocument(mFragment.requireActivity().contentResolver, docUri, "bar.txt")
    }
}