package com.example.filesystem

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.DocumentsContract
import java.io.Closeable
import java.net.URLDecoder
import java.util.*

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
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val did = _docId ?: docId
            val childDocuments: Uri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, did)
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
                            val sanFile = SanFile(docId=docId, directory=uri.toString(), name=name, isDir=isDir, ext=ext)
                            children.add(sanFile)
                        }
                    } finally {
                        closeQuietly(c)
                    }
                }
            }
            return children.sortedBy { it.name }.toMutableList()
        }

        // Check if the mime type is a directory
        fun isDirectory(mimeType: String): Boolean {
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

        fun decode(string : String) : String {
            return URLDecoder.decode(string, "UTF-8")
        }

        fun getFilenameFromDocId(docId : String) : String {
            val filename = docId.split("/").last()
            return filename
        }

        fun getPathPartsFromDocId(docId : String) : List<String> {
            val pathParts = docId.split("/").drop(1)
            return pathParts
        }

        fun withDelay(action: () -> Unit, callback : (() -> Unit)? = null) {
            val handler = Handler()
            handler.postDelayed({
                action()
                if (callback != null) {
                    callback()
                }
            }, 100)
        }

        fun readAssetsFile(context: Context, filename: String): String {
            val assetManager: AssetManager = context.getAssets()
            return assetManager.open(filename).bufferedReader().use { it.readText() }
        }

        fun explodeFilename(filename: String): Pair<String, String> {
            var base: String? = null
            var ext: String? = null
            val parts = filename.split(".")
            if (parts.size == 1) {
                base = filename
                ext = "bin"  // maps to application/octet-stream
            } else {
                base = parts.dropLast(1).joinToString(".")
                ext = parts.last()
            }
            return Pair(base, ext)
        }
    }
}