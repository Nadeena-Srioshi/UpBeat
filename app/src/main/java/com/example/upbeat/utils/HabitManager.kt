package com.example.upbeat.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.upbeat.models.Habit
import org.json.JSONArray
import org.json.JSONObject

class HabitManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "habits_prefs"
        private const val KEY_HABITS = "habits"
    }

    fun saveHabit(habit: Habit) {
        val habits = getAllHabits().toMutableList()
        val existingIndex = habits.indexOfFirst { it.id == habit.id }

        if (existingIndex != -1) {
            habits[existingIndex] = habit
        } else {
            habits.add(habit)
        }

        saveAllHabits(habits)
    }

    fun getAllHabits(): List<Habit> {
        val habitsJson = sharedPreferences.getString(KEY_HABITS, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(habitsJson)
            List(jsonArray.length()) { i ->
                Habit.fromJson(jsonArray.getJSONObject(i))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteHabit(habitId: String) {
        val habits = getAllHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveAllHabits(habits)
    }

    private fun saveAllHabits(habits: List<Habit>) {
        val jsonArray = JSONArray()
        habits.forEach { habit ->
            jsonArray.put(habit.toJson())
        }

        sharedPreferences.edit()
            .putString(KEY_HABITS, jsonArray.toString())
            .apply()
    }

    fun clearAllHabits() {
        sharedPreferences.edit()
            .remove(KEY_HABITS)
            .apply()
    }
}