package com.example.filesystem.actions

import android.content.Intent
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import com.example.filesystem.MainActivity

class CreateFolder(activity: MainActivity) : Action() {

    private val resultHandler = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

    }

    fun handle() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = DocumentsContract.Document.MIME_TYPE_DIR
        }
        resultHandler.launch(intent)
    }
}