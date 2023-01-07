package com.example.filesystem;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.filesystem.data.MyFile


class MyFilesAdapter(private val onClick: (MyFile) -> Unit) :
    ListAdapter<MyFile, MyFilesAdapter.MyFileViewHolder>(MyFileDiffCallback) {

    /* ViewHolder for MyFile, takes in the inflated view and the onClick behavior. */
    class MyFileViewHolder(itemView: View, val onClick: (MyFile) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val myFileTextView: TextView = itemView.findViewById(R.id.myfile_text)
        private val myFileImageView: ImageView = itemView.findViewById(R.id.myfile_image)
        private var currentMyFile: MyFile? = null

        init {
            itemView.setOnClickListener {
                currentMyFile?.let {
                    onClick(it)
                }
            }
        }

        /* Bind myFile name and image. */
        fun bind(myFile: MyFile) {
            currentMyFile = myFile

            myFileTextView.text = myFile.name
            if (myFile.image != null) {
                myFileImageView.setImageResource(myFile.image)
            } else {
                //myFileImageView.setImageResource(R.drawable.rose)
            }
        }
    }

    /* Creates and inflates view and return MyFileViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.myfile_item, parent, false)
        return MyFileViewHolder(view, onClick)
    }

    /* Gets current myFile and uses it to bind view. */
    override fun onBindViewHolder(holder: MyFileViewHolder, position: Int) {
        val myFile = getItem(position)
        holder.bind(myFile)
    }
}

object MyFileDiffCallback : DiffUtil.ItemCallback<MyFile>() {
    override fun areItemsTheSame(oldItem: MyFile, newItem: MyFile): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MyFile, newItem: MyFile): Boolean {
        return oldItem.id == newItem.id
    }
}