package com.example.filesystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/* A list always displaying one element: the destination. */

class HeaderAdapter: RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {
    private var sanFileDestination: String = ""

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val sanFileDestinationTextView: TextView = itemView.findViewById(R.id.destination)

        fun bind(sanFileDestination: String) {
            sanFileDestinationTextView.text = sanFileDestination
        }
    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.header_item, parent, false)
        return HeaderViewHolder(view)
    }

    /* Binds destination to the header. */
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(sanFileDestination)
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return 1
    }

    /* Updates header to display current destination. */
    fun updateSanFileDestination(updatedSanFileDestination: String) {
        sanFileDestination = updatedSanFileDestination
        notifyDataSetChanged()
    }
}