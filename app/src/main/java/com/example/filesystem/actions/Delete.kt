package com.example.filesystem.actions

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class Delete(fragment: Fragment) {

    private val _fragment = fragment

    private val handler: ActivityResultLauncher<Intent> = _fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    }

    fun handle() {
    }
}