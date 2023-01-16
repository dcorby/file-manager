package com.example.filesystem.actions

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

class Delete(activity: FragmentActivity) {

    private val handler: ActivityResultLauncher<Intent> = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    }

    fun handle() {

    }
}