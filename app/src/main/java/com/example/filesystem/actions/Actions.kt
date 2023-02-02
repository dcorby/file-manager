package com.example.filesystem.actions

import android.net.Uri
import androidx.recyclerview.selection.Selection
import com.example.filesystem.FolderFragment
import com.example.filesystem.databinding.FragmentFolderBinding

class Actions(fragment: FolderFragment,
              binding: FragmentFolderBinding,
              selection: Selection<String>,
              fragmentUri: Uri,
              fragmentDocId: String,
              callback: (() -> Unit)) {

    var map: HashMap<String, Action> = HashMap()
    init {
        map["copy"] = Copy(fragment, binding, selection, fragmentUri, fragmentDocId, callback)
        map["createFile"] = CreateFile(fragment, binding, selection, fragmentUri, fragmentDocId, callback)
        map["createFolder"] = CreateFolder(fragment, binding, selection, fragmentUri, fragmentDocId, callback)
        map["delete"] = Delete(fragment, binding, selection, fragmentUri, fragmentDocId, callback)
        map["move"] = Move(fragment, binding, selection, fragmentUri, fragmentDocId, callback)
        map["open"] = Open(fragment, binding, selection, fragmentUri, fragmentDocId, callback)
        map["rename"] = Rename(fragment, binding, selection, fragmentUri, fragmentDocId, callback)
    }
}