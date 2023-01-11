package com.example.filesystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.filesystem.R

/* A list always displaying one element: the number of sanfiles. */

class HeaderAdapter: RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {
    private var sanFileCount: Int = 0

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val sanFileNumberTextView: TextView = itemView.findViewById(R.id.sanfile_number_text)

        fun bind(sanFileCount: Int) {
            sanFileNumberTextView.text = sanFileCount.toString()
        }
    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.header_item, parent, false)
        return HeaderViewHolder(view)
    }

    /* Binds number of sanfiles to the header. */
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(sanFileCount)
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return 1
    }

    /* Updates header to display number of sanfiles when a sanfile is added or subtracted. */
    fun updateSanFileCount(updatedSanFileCount: Int) {
        sanFileCount = updatedSanFileCount
        notifyDataSetChanged()
    }
}