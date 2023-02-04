package com.example.filesystem.actions

interface Action {
    fun handle(isClick: Boolean)
    fun finish()
}