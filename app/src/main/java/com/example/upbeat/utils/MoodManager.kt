package com.example.upbeat.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.upbeat.models.MoodEntry
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class MoodManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("WellnessAppPrefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Get all mood entries
    fun getAllMoodEntries(): MutableList<MoodEntry> {
        val moodsJson = prefs.getString("mood_entries", "[]") ?: "[]"
        val jsonArray = JSONArray(moodsJson)
        val moods = mutableListOf<MoodEntry>()

        for (i in 0 until jsonArray.length()) {
            try {
                val moodJson = jsonArray.getJSONObject(i)
                moods.add(MoodEntry.fromJson(moodJson))
            } catch (e: Exception) {
                // Skip corrupted entries
                e.printStackTrace()
            }
        }

        // Sort by timestamp descending (newest first)
        return moods.sortedByDescending { it.timestamp }.toMutableList()
    }

    // Save all mood entries
    private fun saveMoodEntries(moods: List<MoodEntry>) {
        val jsonArray = JSONArray()
        moods.forEach { mood ->
            jsonArray.put(mood.toJson())
        }
        prefs.edit().putString("mood_entries", jsonArray.toString()).apply()
    }

    // Add a new mood entry
    fun addMoodEntry(moodEntry: MoodEntry) {
        val moods = getAllMoodEntries()

        // Remove any existing entry for the same date (optional - allows only one mood per day)
        // moods.removeIf { it.date == moodEntry.date }

        moods.add(moodEntry)
        saveMoodEntries(moods)
    }

    // Update an existing mood entry
    fun updateMoodEntry(moodEntry: MoodEntry) {
        val moods = getAllMoodEntries()
        val index = moods.indexOfFirst { it.id == moodEntry.id }
        if (index != -1) {
            moods[index] = moodEntry
            saveMoodEntries(moods)
        }
    }

    // Get mood entries for a specific date
    fun getMoodEntriesForDate(date: String): List<MoodEntry> {
        return getAllMoodEntries().filter { it.date == date }
    }

    // Get mood entries for a specific month
    fun getMoodEntriesForMonth(year: Int, month: Int): List<MoodEntry> {
        val moods = getAllMoodEntries()
        return moods.filter {
            try {
                val entryDate = dateFormat.parse(it.date)
                if (entryDate != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = entryDate
                    calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    // Delete a mood entry
    fun deleteMoodEntry(moodId: String) {
        val moods = getAllMoodEntries()
        moods.removeIf { it.id == moodId }
        saveMoodEntries(moods)
    }

    // Get today's date formatted
    fun getTodayDate(): String {
        return dateFormat.format(Date())
    }

    // Get current time formatted
    fun getCurrentTime(): String {
        return timeFormat.format(Date())
    }

    // Check if there's a mood entry for today
    fun hasMoodToday(): Boolean {
        val today = getTodayDate()
        return getMoodEntriesForDate(today).isNotEmpty()
    }

    // Get mood statistics (useful for insights)
    fun getMoodStats(): Map<String, Int> {
        val moods = getAllMoodEntries()
        return moods.groupBy { it.mood }
            .mapValues { it.value.size }
    }

    // Get most common feeling
    fun getMostCommonFeeling(): String? {
        val moods = getAllMoodEntries()
        return moods
            .filter { it.feeling.isNotEmpty() }
            .groupBy { it.feeling }
            .maxByOrNull { it.value.size }
            ?.key
    }

    // Clear all mood entries (useful for testing or reset)
    fun clearAllMoodEntries() {
        prefs.edit().remove("mood_entries").apply()
    }
}