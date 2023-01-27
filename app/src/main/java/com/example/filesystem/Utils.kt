package com.example.filesystem

import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.DocumentsContract
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
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

        fun decode(string : String) : String {
            return URLDecoder.decode(string, "UTF-8")
        }

        fun getNameFromDocId(docId : String) : String {
            val name = docId.split("/").last()
            return name
        }

        fun getPathPartsFromDocId(docId : String) : List<String> {
            val pathParts = docId.split("/").drop(1)
            return pathParts
        }

        fun showPopup(layoutInflater: LayoutInflater, activity: Activity, text: String) {
            val layout = layoutInflater.inflate(R.layout.popup, null)
            val popup = layout.findViewById<ViewGroup>(R.id.popup)
            val textView = popup.findViewById<TextView>(R.id.text_view)
            textView.text = text
            val window = PopupWindow(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true)
            window.showAtLocation(layout, Gravity.CENTER, 0, 0)
            popup.setOnClickListener {
                window.dismiss()
            }
        }

        fun withDelay(action: () -> Unit) {
            val handler = Handler()
            handler.postDelayed({
                action()
            }, 100)
        }

        fun readAssetsFile(context: Context, fileName: String): String {
            val assetManager: AssetManager = context.getAssets()
            return assetManager.open(fileName).bufferedReader().use { it.readText() }
        }
    }
}