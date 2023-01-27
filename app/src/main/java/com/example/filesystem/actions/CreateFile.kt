package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import com.example.filesystem.MainReceiver

class CreateFile(fragment: Fragment) {

    private val _fragment = fragment

    fun handle(receiver: MainReceiver, uri: Uri, filename: String) {
        var _ext: String? = null
        var _filename: String? = null

        val parts = filename.split(".")
        if (parts.size == 1) {
            _ext = "bin"  // maps to application/octet-stream
            _filename = filename
        } else {
            _ext = parts.last()
            _filename = parts.dropLast(1).joinToString(".")
        }
        val mimeType = receiver.getMimeType(_ext) as String
        DocumentsContract.createDocument(_fragment.requireActivity().contentResolver, uri, mimeType, _filename)
    }
}