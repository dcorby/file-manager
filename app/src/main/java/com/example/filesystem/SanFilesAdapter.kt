package com.example.filesystem;

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
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

    //private val itemCount = getItem

    var tracker: SelectionTracker<String>? = null
    init {
        setHasStableIds(true)
    }


    /* ViewHolder for SanFile, takes in the inflated view and the onClick behavior. */
    // "inner": https://stackoverflow.com/questions/45418194/i-cant-reach-any-class-member-from-a-nested-class-in-kotlin
    inner class SanFileViewHolder(itemView: View, val onClick: (SanFile) -> Unit) : RecyclerView.ViewHolder(itemView) {
        // RecyclerView decoration instructions:
        // https://stackoverflow.com/questions/24618829/how-to-add-dividers-and-spaces-between-items-in-recyclerview
        private val sanFileTextView: TextView = itemView.findViewById(R.id.sanfile_text)
        private val sanFileImageView: ImageView = itemView.findViewById(R.id.sanfile_image)
        private var currentSanFile: SanFile? = null

        init {
            itemView.setOnClickListener {
                currentSanFile?.let {
                    onClick(it)
                }
            }
        }

        /* Bind sanFile name and image. */
        fun bind(sanFile: SanFile) {
            currentSanFile = sanFile
            sanFileTextView.text = sanFile.name

            // getPosition() and getAdapterPosition() have been deprecated for
            // getAbsoluteAdapterPosition() and getBindingAdapterPosition()
            // e.g. if you wanted to identify last row and change its style
            val pos = absoluteAdapterPosition
            val count = itemCount
            // Log.v("File-san", "Binding pos=$pos of count=$count")

            // Get context from any view object:
            // https://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
            val context = itemView.context
            // https://stackoverflow.com/questions/4539630/how-do-i-check-to-see-if-a-resource-exists-in-android
            val identifier = context.resources.getIdentifier(sanFile.ext, "drawable", "com.example.filesystem")
            if (sanFile.ext != null && identifier != 0) {
                val icon: Drawable? = AppCompatResources.getDrawable(context, identifier)
                sanFileImageView.setImageDrawable(icon)
                // sanFileImageView.setImageResource(sanFile.image)
                // sanFileImageView.setImageResource(R.drawable.rose)
            }

            itemView.setOnClickListener {
                itemView.isActivated = true
                //onClick(sanFile)
            }

            tracker?.let {
                //if (position >= currentList.size) {
                //    return
                //}
                if (it.isSelected(getItem(position).docId)) {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.pink))
                } else {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int {
                    val x = adapterPosition
                    Log.v("File-san", "position=$x")
                    return x
                }
                override fun getSelectionKey(): String = currentList[adapterPosition].docId
            }
    }

    override fun getItemCount(): Int = currentList.size
    override fun getItemId(position: Int): Long = position.toLong()

    /* Creates and inflates view and return SanFileViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SanFileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sanfile_item, parent, false)
        return SanFileViewHolder(view, onClick)
    }

    /* Gets current sanFile and uses it to bind view. */
    override fun onBindViewHolder(holder: SanFileViewHolder, position: Int) {
        val sanFile = getItem(position)
        Log.v("File-san", "position=$position")
        holder.bind(sanFile)
    }
}

object SanFileDiffCallback : DiffUtil.ItemCallback<SanFile>() {
    override fun areItemsTheSame(oldItem: SanFile, newItem: SanFile): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: SanFile, newItem: SanFile): Boolean {
        return oldItem.docId == newItem.docId
    }
}