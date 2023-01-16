package com.example.filesystem.actions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

class CreateFolder(activity: FragmentActivity) {

    private val handler: ActivityResultLauncher<Intent> = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri: Uri? = result.data?.data
        val contentResolver = activity.contentResolver
        if (result.resultCode == Activity.RESULT_OK) {
            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    //override fun handle(handler : ActivityResultLauncher<Intent>) {
    fun handle() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = DocumentsContract.Document.MIME_TYPE_DIR
        }
        handler.launch(intent)
    }
}