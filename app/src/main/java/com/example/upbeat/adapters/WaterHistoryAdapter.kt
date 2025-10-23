package com.example.upbeat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upbeat.R
import com.example.upbeat.models.WaterEntry
import java.text.SimpleDateFormat
import java.util.*

class WaterHistoryAdapter(
    private val entries: List<WaterEntry>
) : RecyclerView.Adapter<WaterHistoryAdapter.WaterHistoryViewHolder>() {

    inner class WaterHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_water_history, parent, false)
        return WaterHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaterHistoryViewHolder, position: Int) {
        val entry = entries[position]

        holder.tvAmount.text = "${entry.amount} ml"

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        holder.tvTime.text = timeFormat.format(Date(entry.timestamp))

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(Date(entry.timestamp))
    }

    override fun getItemCount(): Int = entries.size
}