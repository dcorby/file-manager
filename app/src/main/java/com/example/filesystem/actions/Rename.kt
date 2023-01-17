package com.example.filesystem.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import java.io.File

class Rename(fragmentActivity: FragmentActivity) {

    private val activity = fragmentActivity

    private val handler: ActivityResultLauncher<Intent> = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    }

    fun handle(context: Context, selections: Selection<String>, destination: String) {
        val docId = selections.toList()[0]
        //val docUri = DocumentsContract.buildDocumentUri(uri.authority, uri.toString())
        //val docUri = DocumentsContract.buildDocumentUriUsingTree()
        val docUri = DocumentsContract.buildDocumentUriUsingTree(destination!!.toUri(), docId)


        //Log.v("File-san", docUri.

        //val docUri = DocumentsContract.buildDocumentUri(uri.authority, uri.toString())
        //Log.v("File-san", "DocumentUri=$docUri")
        //Log.v("File-san", "DocumentUri=${dfile!!.uri}")
        DocumentsContract.renameDocument(activity.contentResolver, docUri, "bar.txt")
        //observeCurrent()
    }
}