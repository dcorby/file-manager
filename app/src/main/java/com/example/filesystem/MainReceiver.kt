package com.example.filesystem

interface MainReceiver {
    fun getActionState(action: String): HashMap<String, String>
    fun getActionState(action: String, key: String): String?
    fun setActionState(action: String, key: String, value: String?)
    fun getMimeType(key: String): String
}