package com.example.filesystem

import android.os.Build
import android.os.FileObserver
import androidx.annotation.RequiresApi
import java.io.File

class DirectoryObserver : FileObserver {

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(file: File, mask: Int, function: () -> Unit) : super(file, mask) {
    }

    override fun onEvent(event: Int, pathString: String?) {
        when (event) {
            DELETE_SELF -> {}
            CREATE, DELETE -> {}
        }
    }
}