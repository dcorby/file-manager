package com.example.filesystem

import android.net.Uri
import android.util.Log
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class ItemsKeyProvider(private val sanFilesAdapter: SanFilesAdapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String {
        return sanFilesAdapter.currentList[position].docId
    }
    override fun getPosition(key: String): Int {
        return sanFilesAdapter.currentList.indexOfFirst { it.docId == key }
    }
}
