package com.example.filesystem

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import java.io.Closeable
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
        fun getChildren(activity: Activity, uri: Uri): MutableList<SanFile> {
            val children = ArrayList<SanFile>()
            val contentResolver: ContentResolver = activity.contentResolver
            val childDocuments: Uri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
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
                            Log.d("San-File", "docId: $docId, name: $name, mime: $mime, isDir: $isDir")
                            val newNode = DocumentsContract.buildChildDocumentsUriUsingTree(uri, docId)

                            // TODO
                            //children.add(newNode) <-- GET A SAN FILE HERE
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