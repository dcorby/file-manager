package com.example.filesystem.actions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.example.filesystem.SanFile

class CreateFile(activity: FragmentActivity) {

    // "The launcher activity does not return a result."
    // https://stackoverflow.com/questions/72985346/activityresultlauncherintent-returns-result-code-before-returning-to-activity
    private val handler: ActivityResultLauncher<Intent> = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri: Uri? = result.data?.data
        val contentResolver = activity.contentResolver
        if (result.resultCode == Activity.RESULT_OK) {
            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    fun handle(): SanFile {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            //type = "text/plain"
            //putExtra(Intent.EXTRA_TITLE, "testing.txt")
        }
        val result = handler.launch(intent)
        val sanFile: SanFile = SanFile(directory = "", docId = "", name="", isDir = false, ext = "")
        return sanFile
    }
}