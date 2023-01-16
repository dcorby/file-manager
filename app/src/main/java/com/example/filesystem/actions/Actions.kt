package com.example.filesystem.actions

import androidx.fragment.app.FragmentActivity

// https://stackoverflow.com/questions/64476827/how-to-resolve-the-error-lifecycleowners-must-call-register-before-they-are-sta
class Actions() {
    companion object {
        fun get(fragmentActivity: FragmentActivity): HashMap<String, Any> {
            var hashMap : HashMap<String, Any> = HashMap()
            hashMap["Copy"] = Copy(fragmentActivity)
            hashMap["CreateFile"] = CreateFile(fragmentActivity)
            hashMap["CreateFolder"] = CreateFolder(fragmentActivity)
            hashMap["Delete"] = Delete(fragmentActivity)
            hashMap["Move"] = Move(fragmentActivity)
            hashMap["Open"] = Open(fragmentActivity)
            hashMap["Rename"] = Rename(fragmentActivity)
            return hashMap
        }
    }
}