package com.example.filesystem

import android.net.Uri
import android.util.Log
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class ItemsKeyProvider(private val sanFilesAdapter: SanFilesAdapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String {
        //Log.v("File-san", "position=$position")
        //return recyclerView.adapter.currentList[position].docId
        //return recyclerView.adapter!!.getItem(position)
        //recyclerView.
        return sanFilesAdapter.currentList[position].docId
    }
    override fun getPosition(key: String): Int {
        //val viewHolder = recyclerView.findViewHolderForItemId(key)
        //return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
        return sanFilesAdapter.currentList.indexOfFirst {it.docId == key}
    }
}
