package com.example.filesystem.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.selection.Selection
import com.example.filesystem.R
import com.example.filesystem.Utils

class Open(fragment: Fragment) {

    private val _fragment = fragment

    fun handle(context: Context, selections: Selection<String>, destinationUri: Uri) {
        val docId = selections.toList()[0]
        val docUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, docId)
        val docTreeUri = DocumentsContract.buildTreeDocumentUri(docUri.authority, docId)
        val isDir = context.contentResolver.getType(docUri) == DocumentsContract.Document.MIME_TYPE_DIR

        if (isDir) {
            // Handle folder
            val navController = Navigation.findNavController(_fragment.requireActivity(), R.id.nav_host_fragment_content_main)
            val bundle = Bundle()
            bundle.putString("destination", Utils.decode(docTreeUri.toString()))
            bundle.putString("docid", docId)
            navController.navigate(R.id.action_FolderFragment_to_FolderFragment, bundle)
        } else {
            // Handle file
            val intent: Intent = Intent().apply {
                action = Intent.ACTION_EDIT
                setDataAndType(docUri, "text/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(context, Intent.createChooser(intent, null), null)
        }
    }
}