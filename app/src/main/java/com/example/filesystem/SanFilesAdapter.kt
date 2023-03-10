package com.example.filesystem;

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SanFilesAdapter(private val onClick: (SanFile) -> Unit) :
    ListAdapter<SanFile, SanFilesAdapter.SanFileViewHolder>(SanFileDiffCallback) {

    lateinit var tracker: SelectionTracker<String>
    var multiSelectActivated = false
    var multiSelectAnchor = ""
    var prevDocId = ""

    // it doesn't
    init {
        setHasStableIds(false)
    }

    /* ViewHolder for SanFile, takes in the inflated view and the onClick behavior. */
    // "inner": https://stackoverflow.com/questions/45418194/i-cant-reach-any-class-member-from-a-nested-class-in-kotlin
    inner class SanFileViewHolder(itemView: View, val onClick: (SanFile) -> Unit) : RecyclerView.ViewHolder(itemView) {
        // RecyclerView decoration instructions:
        // https://stackoverflow.com/questions/24618829/how-to-add-dividers-and-spaces-between-items-in-recyclerview
        private val sanFileTextView: TextView = itemView.findViewById(R.id.sanfile_text)
        private val sanFileImageView: ImageView = itemView.findViewById(R.id.sanfile_image)
        private var currentSanFile: SanFile? = null

        /* Bind data to view */
        fun bind(sanFile: SanFile) {
            currentSanFile = sanFile
            sanFileTextView.text = sanFile.name

            val context = itemView.context
            if (sanFile.ext != null) {
                val identifier = context.resources.getIdentifier(sanFile.ext, "drawable", "com.example.filesystem")
                if (identifier != 0) {
                    val icon: Drawable? = AppCompatResources.getDrawable(context, identifier)
                    sanFileImageView.setImageDrawable(icon)
                } else {
                    context.resources.getIdentifier("file", "drawable", "com.example.filesystem")
                }
            } else {
                val identifier = if (sanFile.isDir) {
                    context.resources.getIdentifier("folder", "drawable", "com.example.filesystem")
                } else {
                    context.resources.getIdentifier("file", "drawable", "com.example.filesystem")
                }
                val icon: Drawable? = AppCompatResources.getDrawable(context, identifier)
                sanFileImageView.setImageDrawable(icon)
            }

            itemView.setOnLongClickListener {
                if (!multiSelectActivated) {
                    multiSelectActivated = true
                    multiSelectAnchor = sanFile.docId
                }
                return@setOnLongClickListener true
            }

            tracker.let {
                if (it.isSelected(getItem(position).docId)) {
                    if (!multiSelectActivated && getItem(position).docId != prevDocId) {
                        tracker.deselect(prevDocId)
                    }
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.maroon))
                    sanFileTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    prevDocId = getItem(position).docId
                } else {
                    if (getItem(position).docId == multiSelectAnchor) {
                        multiSelectAnchor = ""
                        multiSelectActivated = false
                    }
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary_background))
                    sanFileTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                }
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int {
                    val x = adapterPosition
                    return x
                }
                override fun getSelectionKey(): String = currentList[adapterPosition].docId
                override fun inSelectionHotspot(e: MotionEvent): Boolean { return true }
                // this overrides above itemView.setOnClickListener()
            }
    }

    fun initTracker() {
        tracker.addObserver(
            object : SelectionTracker.SelectionObserver<String>() {
                override fun onSelectionChanged() {
                }

                override fun onItemStateChanged(key: String, selected: Boolean) {
                    if (tracker.hasSelection()) {
                    }
                    super.onItemStateChanged(key, !selected)
                }
            })
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
    // get the stable ID
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /* Creates and inflates view and return SanFileViewHolder */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SanFileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sanfile_item, parent, false)
        return SanFileViewHolder(view, onClick)
    }

    /* Gets current sanFile and uses it to bind view. */
    override fun onBindViewHolder(viewHolder: SanFileViewHolder, position: Int) {
        val sanFile = getItem(position)
        viewHolder.bind(sanFile)
    }
}

object SanFileDiffCallback : DiffUtil.ItemCallback<SanFile>() {
    override fun areItemsTheSame(oldItem: SanFile, newItem: SanFile): Boolean {
        return oldItem.docId == newItem.docId
    }

    override fun areContentsTheSame(oldItem: SanFile, newItem: SanFile): Boolean {
        return oldItem.docId == newItem.docId
    }
}