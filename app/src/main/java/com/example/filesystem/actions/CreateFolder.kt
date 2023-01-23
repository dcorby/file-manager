package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment

class CreateFolder(fragment: Fragment) {

    private val _fragment = fragment

    fun handle(uri: Uri) {
        DocumentsContract.createDocument(_fragment.requireActivity().contentResolver, uri, DocumentsContract.Document.MIME_TYPE_DIR, "foo")
    }
}