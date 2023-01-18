package com.example.filesystem.actions

import android.content.Context
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection


class Rename(fragment: Fragment) {

    private val _fragment = fragment

    fun handle(context: Context, selections: Selection<String>, destination: String) {
        val docId = selections.toList()[0]
        val docUri = DocumentsContract.buildDocumentUriUsingTree(destination!!.toUri(), docId)
        DocumentsContract.renameDocument(_fragment.requireActivity().contentResolver, docUri, "bar.txt")
    }
}