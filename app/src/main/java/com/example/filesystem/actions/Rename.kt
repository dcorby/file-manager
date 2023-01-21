package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.Selection

class Rename(fragment: Fragment) {

    private val _fragment = fragment

    fun handle(selections: Selection<String>, destinationUri: Uri) {
        val docId = selections.toList()[0]
        val docUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, docId)
        DocumentsContract.renameDocument(_fragment.requireActivity().contentResolver, docUri, "bar.txt")
    }
}