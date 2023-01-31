package com.example.filesystem.actions

import com.example.filesystem.FolderFragment

class Actions(fragment: FolderFragment) {
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