package com.example.upbeat.models

import com.example.upbeat.R
import org.json.JSONObject

data class MoodEntry(
    val id: String,
    val mood: String, // "Great", "Good", "Okay", "Not Great", "Bad"
    val feeling: String = "", // The specific feeling chosen (e.g., "Happy", "Angry")
    val reason: String = "", // What makes them feel this way
    val date: String, // Format: "yyyy-MM-dd"
    val time: String, // Format: "HH:mm"
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("mood", mood)
            put("feeling", feeling)
            put("reason", reason)
            put("date", date)
            put("time", time)
            put("timestamp", timestamp)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): MoodEntry {
            // Note: "emoji" field is ignored for backward compatibility
            return MoodEntry(
                id = json.getString("id"),
                mood = json.getString("mood"),
                feeling = json.optString("feeling", ""),
                reason = json.optString("reason", ""),
                date = json.getString("date"),
                time = json.getString("time"),
                timestamp = json.getLong("timestamp")
            )
        }

        // Mood presets matching your design
        fun getMoodOptions(): List<String> {
            return listOf(
                "Great", "Good", "Okay", "Not Great", "Bad"
            )
        }

        fun getMoodDrawable(mood: String): Int {
            return when (mood) {
                "Great" -> R.drawable.mood_face_great
                "Good" -> R.drawable.mood_face_good
                "Okay" -> R.drawable.mood_face_okay
                "Not Great" -> R.drawable.mood_face_not_great
                "Bad" -> R.drawable.mood_face_bad // Placeholder, as mood_face_bad.xml doesn't exist
                else -> R.drawable.bg_circle // Placeholder for unknown
            }
        }


        // Get feelings for each mood
        fun getFeelingsForMood(mood: String): List<String> {
            return when (mood) {
                "Great" -> listOf(
                    "Brave", "Confident", "Creative", "Excited",
                    "Grateful", "Happy", "Hopeful", "Inspired", "Loved", "Proud"
                )
                "Good" -> listOf(
                    "Calm", "Content", "Relaxed", "Peaceful",
                    "Satisfied", "Comfortable", "Cheerful", "Optimistic"
                )
                "Okay" -> listOf(
                    "Neutral", "Uncertain", "Tired", "Restless",
                    "Confused", "Indifferent", "Thoughtful", "Distracted"
                )
                "Not Great" -> listOf(
                    "Sad", "Worried", "Stressed", "Anxious",
                    "Disappointed", "Frustrated", "Overwhelmed", "Doubtful"
                )
                "Bad" -> listOf(
                    "Angry", "Disrespected", "Judged", "Lonely",
                    "Unimportant", "Hurt", "Rejected", "Miserable"
                )
                else -> emptyList()
            }
        }

        // Get color for mood
        fun getMoodColor(mood: String): String {
            return when (mood) {
                "Great" -> "#FFD93D"
                "Good" -> "#A8E6A3"
                "Okay" -> "#A8D8EA"
                "Not Great" -> "#C5B9E8"
                "Bad" -> "#FFB4B4"
                else -> "#E0E0E0"
            }
        }
    }
}
