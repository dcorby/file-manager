package com.example.filesystem.actions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

// https://stackoverflow.com/questions/64476827/how-to-resolve-the-error-lifecycleowners-must-call-register-before-they-are-sta
class Actions() {
    fun get(fragment: Fragment): HashMap<String, Any> {
        var hashMap : HashMap<String, Any> = HashMap()
        hashMap["Copy"] = Copy(fragment)
        hashMap["CreateFile"] = CreateFile(fragment)
        hashMap["CreateFolder"] = CreateFolder(fragment)
        hashMap["Delete"] = Delete(fragment)
        hashMap["Move"] = Move(fragment)
        hashMap["Open"] = Open(fragment)
        hashMap["Rename"] = Rename(fragment)
        return hashMap
    }
}