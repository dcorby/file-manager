package com.example.filesystem.data

import androidx.annotation.DrawableRes

data class SanFile(
    val id: Long,
    val name: String,
    @DrawableRes
    val image: Int?,
    val description: String
)
