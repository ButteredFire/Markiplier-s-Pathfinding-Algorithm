package com.example.interactivetracker

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var textView: TextView
    init {
        textView = view.findViewById(R.id.TV_nodeName)
    }
}