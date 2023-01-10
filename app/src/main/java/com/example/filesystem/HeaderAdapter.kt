package com.example.filesystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.filesystem.R

/* A list always displaying one element: the number of sanfiles. */

class HeaderAdapter: RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {
    private var myFileCount: Int = 0

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val myFileNumberTextView: TextView = itemView.findViewById(R.id.myfile_number_text)

        fun bind(myFileCount: Int) {
            myFileNumberTextView.text = myFileCount.toString()
        }
    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.header_item, parent, false)
        return HeaderViewHolder(view)
    }

    /* Binds number of myfiles to the header. */
    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(myFileCount)
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return 1
    }

    /* Updates header to display number of myfiles when a myfile is added or subtracted. */
    fun updateMyFileCount(updatedMyFileCount: Int) {
        myFileCount = updatedMyFileCount
        notifyDataSetChanged()
    }
}