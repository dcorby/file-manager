package com.example.filesystem.actions

import com.example.filesystem.MainActivity

open class Action {

}

class Actions constructor(mainActivity: MainActivity) {
    private val activity = mainActivity

    fun getAction(type: String): Action {
        return when (type) {
            "Copy" -> Copy(activity)
            "CreateFile" -> CreateFile(activity)
            "CreateFolder" -> CreateFolder(activity)
            "Delete" -> Delete(activity)
            "Move" -> Move(activity)
            "Open" -> Open(activity)
            "Rename" -> Rename(activity)
            else -> {
                throw Exception("Action not found")
            }
        }
    }
}