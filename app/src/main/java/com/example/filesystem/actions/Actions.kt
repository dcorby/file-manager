package com.example.filesystem.actions

import com.example.filesystem.FolderFragment

class Actions(fragment: FolderFragment) {
    private val hashMap = hashMapOf(
        "copy" to Copy(fragment),
        "createFile" to CreateFile(fragment),
        "createFolder" to CreateFolder(fragment),
        "delete" to Delete(fragment),
        "move" to Move(fragment),
        "open" to Open(fragment),
        "rename" to Rename(fragment)
    )

    fun get(action : String) : Any? {
        return hashMap[action]
    }
}