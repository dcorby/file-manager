package com.example.filesystem

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.filesystem.databinding.FragmentFolderBinding

class UI {
    companion object {
        fun handleActiveCopy(receiver: MainReceiver, binding: FragmentFolderBinding) {
            if (receiver.getActionState("copy", "sourceUri") != null) {
                binding.toggleGroup.check(R.id.action_copy)
                val sourceFragmentDocId = receiver.getActionState("copy", "sourceFragmentDocId")!!
                val sourceDocId = receiver.getActionState("copy", "sourceDocId")!!
                showStatus(binding.status, "Copying", sourceFragmentDocId, sourceDocId)
            } else {
                binding.toggleGroup.uncheck(R.id.action_copy)
            }
        }

        fun handleActiveMove(receiver: MainReceiver, binding: FragmentFolderBinding) {
            if (receiver.getActionState("move", "sourceUri") != null) {
                binding.toggleGroup.check(R.id.action_move)
                val sourceFragmentDocId = receiver.getActionState("move", "sourceFragmentDocId")!!
                val sourceDocId = receiver.getActionState("move", "sourceDocId")!!
                showStatus(binding.status, "Moving", sourceFragmentDocId, sourceDocId)
            } else {
                binding.toggleGroup.uncheck(R.id.action_move)
            }
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

                    // As user types, save result in fragment.receiver and then use onSaveInstanceState
                    editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                        if (hasFocus) {
                            val imm = fragment.requireActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                            // ^ This is deprecated but the direct methods (showSoftInput, e.g.) do not work on the Fire tablet
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

        fun showStatus(layout: LinearLayout, prefix: String, fragmentDocId: String, sourceDocId: String) {
            cleanStatus(layout)
            val pathParts = Utils.getPathPartsFromDocId(fragmentDocId)
            val filename = Utils.getFilenameFromDocId(sourceDocId)
            val status = "${prefix}: ${(listOf("Home") + pathParts).joinToString("/")}/${filename}"
            val text = layout.findViewById(R.id.text) as TextView
            text.text = status
            layout.visibility = View.VISIBLE
        }

        fun cleanStatus(layout: LinearLayout) {
            val text = layout.findViewById(R.id.text) as TextView
            text.text = ""
            layout.visibility = View.GONE
        }
    }
}