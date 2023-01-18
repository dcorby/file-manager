package com.example.filesystem.actions

import android.net.Uri
import android.provider.DocumentsContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class CreateFile(fragment: Fragment) {

    private val _fragment = fragment

    fun handle(uri: Uri) {
        DocumentsContract.createDocument(_fragment.requireActivity().contentResolver, uri, "text/plain", "foo")
    }
}

//    ** Old implementation **
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
//        handler.launch(intent)
//    }