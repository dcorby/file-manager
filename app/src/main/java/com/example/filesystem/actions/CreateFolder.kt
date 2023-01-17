package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.FragmentActivity

class CreateFolder(fragmentActivity: FragmentActivity) {

    private val activity = fragmentActivity

    fun handle(uri: Uri) {
        DocumentsContract.createDocument(activity.contentResolver, uri, DocumentsContract.Document.MIME_TYPE_DIR, "foo")
    }
}