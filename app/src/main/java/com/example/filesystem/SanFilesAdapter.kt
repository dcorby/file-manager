package com.example.filesystem;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SanFilesAdapter(private val onClick: (SanFile) -> Unit) :
    ListAdapter<SanFile, SanFilesAdapter.SanFileViewHolder>(SanFileDiffCallback) {

    /* ViewHolder for SanFile, takes in the inflated view and the onClick behavior. */
    class SanFileViewHolder(itemView: View, val onClick: (SanFile) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
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
            //if (sanFile.image != null) {
            //    sanFileImageView.setImageResource(sanFile.image)
            //} else {
            //    //sanFileImageView.setImageResource(R.drawable.rose)
            //}
        }
    }

    /* Creates and inflates view and return SanFileViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SanFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sanfile_item, parent, false)
        return SanFileViewHolder(view, onClick)
    }

    /* Gets current sanFile and uses it to bind view. */
    override fun onBindViewHolder(holder: SanFileViewHolder, position: Int) {
        val sanFile = getItem(position)
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