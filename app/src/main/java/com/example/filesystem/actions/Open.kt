package com.example.filesystem.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection

class Open(activity: FragmentActivity) {

    fun handle(context: Context, selections: Selection<String>) {
        // https://developer.android.com/training/basics/intents/sending
        // https://developer.android.com/training/sharing/send
        // https://stackoverflow.com/questions/31621419/android-how-do-i-open-a-file-in-another-app-via-intent
        val uris: ArrayList<Uri> = arrayListOf()
        for (selection in selections) {
            uris.add(Uri.parse(selection))
        }

        val intent: Intent = Intent().apply {
            action = Intent.ACTION_EDIT
            setDataAndType(uris[0], "text/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(context, Intent.createChooser(intent, null), null)
    }
}