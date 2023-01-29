package com.example.filesystem.actions

import androidx.fragment.app.Fragment

class Actions(fragment: Fragment) {
    private val hashMap = hashMapOf(
        "Copy" to Copy(fragment),
        "CreateFile" to CreateFile(fragment),
        "CreateFolder" to CreateFolder(fragment),
        "Delete" to Delete(fragment),
        "Move" to Move(fragment),
        "Open" to Open(fragment),
        "Rename" to Rename(fragment)
    )

    fun get(action : String) : Any? {
        return hashMap[action]
    }
}