package com.example.filesystem.actions

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity

class CreateFile(activity: FragmentActivity) {

    private val _activity = activity

//    // "The launcher activity does not return a result."
//    // https://stackoverflow.com/questions/72985346/activityresultlauncherintent-returns-result-code-before-returning-to-activity
//    // This is asynchronous, so when we observeCurrent() when we return to the fragment, this is not guaranteed to finish
//    // This person suggests implementing a callback
//    private val handler: ActivityResultLauncher<Intent> = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        val uri: Uri? = result.data?.data
//        val contentResolver = activity.contentResolver
//        if (result.resultCode == Activity.RESULT_OK) {
//            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//        }
//    }
//
//    fun handle() {
//        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//            addCategory(Intent.CATEGORY_OPENABLE)
//            type = "*/*"
//        }
//        Log.v("File-san", handler.toString())
//        handler.launch(intent)
//    }

    fun handle(uri: Uri) {


        //_activity.contentResolver.
        DocumentsContract.createDocument(_activity.contentResolver, uri, "text/plain", "foo")
    }
}