package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment

class Rename(fragment: Fragment) {

    private val _fragment = fragment

    fun handle(docId: String, destinationUri: Uri) {
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, docId)
        val docUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, docId)
        DocumentsContract.renameDocument(_fragment.requireActivity().contentResolver, docUri, "bar.txt")
    }
}