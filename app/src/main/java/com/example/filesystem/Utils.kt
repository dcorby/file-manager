package com.example.filesystem

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import java.io.Closeable
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList

class Utils {

    companion object {

        private val fields = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

        // Implementation is based strongly on this example:
        // https://stackoverflow.com/questions/68789742/not-recuisive-get-list-file-type-in-android-java
        fun getChildren(activity: Activity, uri: Uri, _docId: String?): MutableList<SanFile> {
            val children = ArrayList<SanFile>()
            val contentResolver: ContentResolver = activity.contentResolver

            //contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            // A tree uri has /tree/ in it
            // https://stackoverflow.com/questions/34927748/android-5-0-documentfile-from-tree-uri
            val _uri = Uri.parse(URLDecoder.decode(uri.toString(), "UTF-8"))
            Log.v("File-san", "Getting children for treeUri=${_uri}")
            val docId = DocumentsContract.getTreeDocumentId(_uri)
            val did = _docId ?: docId
            val childDocuments: Uri = DocumentsContract.buildChildDocumentsUriUsingTree(_uri, did)
            val dirNodes = LinkedList<Uri>()
            dirNodes.add(childDocuments)
            while (!dirNodes.isEmpty()) {
                val curUri = dirNodes.removeAt(0)
                val c: Cursor? = contentResolver.query(curUri, fields, null, null, null)
                if (c != null) {
                    try {
                        while (c.moveToNext()) {

                            val docId = c.getString(0)
                            val name = c.getString(1)
                            val mime = c.getString(2)
                            val isDir = isDirectory(mime)
                            var ext: String? = null
                            if (name.contains(".")) {
                                ext = name.substring(name.lastIndexOf(".") + 1)    
                            }
                            Log.d("File-san", "docId: $docId, name: $name, mime: $mime, isDir: $isDir, ext: $ext")
                            //val newNode = DocumentsContract.buildChildDocumentsUriUsingTree(uri, docId)
                            val sanFile: SanFile = SanFile(docId=docId, directory=uri.toString(), name=name, isDir=isDir, ext=ext)
                            children.add(sanFile)
                        }
                    } finally {
                        closeQuietly(c)
                    }
                }
            }
            return children.toMutableList()
        }

        // Check if the mime type is a directory
        private fun isDirectory(mimeType: String): Boolean {
            return DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)
        }

        // Close a closeable
        private fun closeQuietly(closeable: Closeable) {
            try {
                closeable.close()
            } catch (re: RuntimeException) {
                throw re
            }
        }
    }
}