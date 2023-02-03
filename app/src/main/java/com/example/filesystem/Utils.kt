package com.example.filesystem

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
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

        // optional callback syntax: https://discuss.kotlinlang.org/t/optional-function-parameters/905
        fun showPopup(fragment: FolderFragment, text: String, onDismiss : (() -> Unit)? = null) {
            // https://stackoverflow.com/questions/9529504/unable-to-add-window-token-android-os-binderproxy-is-not-valid-is-your-activ
            Handler().post {
                if (!fragment.requireActivity().isFinishing) {
                    val window = fragment.getPopupWindow("popup")
                    val contentView = window.contentView
                    val popup = contentView.findViewById<ViewGroup>(R.id.popup)
                    val textView = popup.findViewById<TextView>(R.id.text_view)
                    textView.text = text
                    popup.setOnClickListener {
                        window.dismiss()
                        if (onDismiss != null) {
                            onDismiss()
                        }
                    }
                }
            }
        }

        // https://stackoverflow.com/questions/18799216/how-to-make-a-edittext-box-in-a-dialog
        // Possibly should just be using a Dialog rather than PopupWindow
        fun showPrompt(fragment: FolderFragment, onSubmit: (EditText) -> Unit, onDismiss : (() -> Unit)? = null) {
            Handler().post {
                if (!fragment.requireActivity().isFinishing) {
                    val window = fragment.getPopupWindow("prompt")
                    val contentView = window.contentView
                    val prompt = contentView.findViewById<ViewGroup>(R.id.prompt)
                    val editText = contentView.findViewById<EditText>(R.id.edit_text)

                    editText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) {
                            val imm = fragment.requireActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                            // ^ This is deprecated but the direct methods do not work on the Fire tablet
                        }
                        // Hide it in MainActivity OnDestroy()
                    }

                    val submitText = contentView.findViewById<Button>(R.id.submit_text)
                    prompt.setOnClickListener {
                        window.dismiss()
                        if (onDismiss != null) {
                            onDismiss()
                        }
                    }
                    submitText.setOnClickListener {
                        window.dismiss()
                        onSubmit(editText)
                    }
                    editText.setOnEditorActionListener { v, actionId, event ->
                        window.dismiss()
                        onSubmit(editText)
                        true
                    }
                }
            }
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

        fun showStatus(layout: LinearLayout, prefix: String, fragmentDocId: String, sourceDocId: String) : View {
            val pathParts = getPathPartsFromDocId(fragmentDocId)
            val filename = getFilenameFromDocId(sourceDocId)
            val status = "${prefix}: ${(listOf("Home") + pathParts).joinToString("/")}/${filename}"
            val text = layout.findViewById(R.id.text) as TextView
            text.text = status
            layout.visibility = View.VISIBLE
            val close = layout.findViewById(R.id.close) as View
            return close
        }

        fun cleanStatus(layout: LinearLayout) {
            val text = layout.findViewById(R.id.text) as TextView
            text.text = ""
            layout.visibility = View.GONE
        }
    }
}