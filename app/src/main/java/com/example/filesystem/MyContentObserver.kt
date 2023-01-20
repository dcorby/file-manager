package com.example.filesystem

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log

class MyContentObserver : ContentObserver {
    constructor(handler: Handler) : super(handler)

    override fun onChange(selfChange : Boolean) {
        super.onChange(selfChange)
        Log.v("File-san", "change")
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.v("File-san", "change2")
    }
}