package com.example.filesystem

interface MainReceiver {
    fun getState(key: String): String?
    fun setState(key: String, value: String)
}