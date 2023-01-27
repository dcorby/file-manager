package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.filesystem.MainReceiver

class CreateFile(fragment: Fragment) {

    private val _fragment = fragment

    fun handle(receiver: MainReceiver, uri: Uri, filename: String) {
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
        val mimeType = receiver.getMimeType(ext) as String
        Log.v("Creating File", "mimeType=$mimeType, name=$name")
        DocumentsContract.createDocument(_fragment.requireActivity().contentResolver, uri, mimeType, name)
    }
}