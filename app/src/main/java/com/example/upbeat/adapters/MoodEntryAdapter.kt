package com.example.upbeat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upbeat.R
import com.example.upbeat.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodEntryAdapter(
    private val moodEntries: MutableList<MoodEntry>,
    private val onDeleteClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodEntryAdapter.MoodEntryViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    inner class MoodEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMoodIcon: ImageView = itemView.findViewById(R.id.ivMoodIcon)
        val tvMoodFeeling: TextView = itemView.findViewById(R.id.tvMoodFeeling)
        val tvReason: TextView = itemView.findViewById(R.id.tvReason)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodEntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        val entry = moodEntries[position]

        // Set mood icon
        val drawableRes = when (entry.mood) {
            "Great" -> R.drawable.mood_face_great
            "Good" -> R.drawable.mood_face_good
            "Okay" -> R.drawable.mood_face_okay
            "Not Great" -> R.drawable.mood_face_not_great
            "Bad" -> R.drawable.mood_face_bad
            else -> R.drawable.mood_face_okay
        }
        holder.ivMoodIcon.setImageResource(drawableRes)

        // Set mood and feeling
        val moodFeelingText = if (entry.feeling.isNotEmpty()) {
            "${entry.mood} • ${entry.feeling}"
        } else {
            entry.mood
        }
        holder.tvMoodFeeling.text = moodFeelingText

        // Set reason
        if (entry.reason.isNotEmpty()) {
            holder.tvReason.text = entry.reason
            holder.tvReason.visibility = View.VISIBLE
        } else {
            holder.tvReason.visibility = View.GONE
        }

        // Set date and time
        try {
            val calendar = Calendar.getInstance()
            val dateParts = entry.date.split("-")
            val timeParts = entry.time.split(":")

            calendar.set(
                dateParts[0].toInt(),
                dateParts[1].toInt() - 1,
                dateParts[2].toInt(),
                timeParts[0].toInt(),
                timeParts[1].toInt()
            )

            val formattedDate = dateFormat.format(calendar.time)
            val formattedTime = timeFormat.format(calendar.time)
            holder.tvDateTime.text = "$formattedDate • $formattedTime"
        } catch (e: Exception) {
            holder.tvDateTime.text = "${entry.date} • ${entry.time}"
        }

        // Delete button
        holder.btnDelete.setOnClickListener {
            onDeleteClick(entry)
        }
    }

    override fun getItemCount(): Int = moodEntries.size

    fun removeItem(moodEntry: MoodEntry) {
        val position = moodEntries.indexOf(moodEntry)
        if (position != -1) {
            moodEntries.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}