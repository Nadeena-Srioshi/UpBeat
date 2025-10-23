package com.example.upbeat.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.upbeat.models.WaterEntry
import java.text.SimpleDateFormat
import java.util.*

class HydrationPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "hydration_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_REMINDER_HOURS = "reminder_hours"
        private const val KEY_REMINDER_MINUTES = "reminder_minutes"
        private const val KEY_TODAY_INTAKE = "today_intake"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_WATER_ENTRIES = "water_entries"
        private const val DEFAULT_GOAL = 82
        private const val DEFAULT_REMINDER_HOURS = 2
        private const val DEFAULT_REMINDER_MINUTES = 0
    }

    fun getDailyGoal(): Int {
        return prefs.getInt(KEY_DAILY_GOAL, DEFAULT_GOAL)
    }

    fun setDailyGoal(goal: Int) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goal).apply()
    }

    fun isRemindersEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDERS_ENABLED, false)
    }

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()
    }

    fun getReminderHours(): Int {
        return prefs.getInt(KEY_REMINDER_HOURS, DEFAULT_REMINDER_HOURS)
    }

    fun setReminderHours(hours: Int) {
        prefs.edit().putInt(KEY_REMINDER_HOURS, hours).apply()
    }

    fun getReminderMinutes(): Int {
        return prefs.getInt(KEY_REMINDER_MINUTES, DEFAULT_REMINDER_MINUTES)
    }

    fun setReminderMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_REMINDER_MINUTES, minutes).apply()
    }

    fun getReminderIntervalMillis(): Long {
        val hours = getReminderHours()
        val minutes = getReminderMinutes()
        return (hours * 60 * 60 * 1000L) + (minutes * 60 * 1000L)
    }

    fun getTodayIntake(): Int {
        checkAndResetDaily()
        return prefs.getInt(KEY_TODAY_INTAKE, 0)
    }

    fun setTodayIntake(amount: Int) {
        checkAndResetDaily()
        prefs.edit().putInt(KEY_TODAY_INTAKE, amount).apply()
    }

    private fun checkAndResetDaily() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "")

        if (lastResetDate != today) {
            prefs.edit()
                .putInt(KEY_TODAY_INTAKE, 0)
                .putString(KEY_LAST_RESET_DATE, today)
                .apply()
        }
    }

    fun addWaterEntry(entry: WaterEntry) {
        val entries = getWaterEntries().toMutableList()
        entries.add(entry)

        // Keep only last 100 entries to avoid excessive storage
        if (entries.size > 100) {
            entries.removeAt(0)
        }

        val entriesString = entries.joinToString(";") { it.toString() }
        prefs.edit().putString(KEY_WATER_ENTRIES, entriesString).apply()
    }

    fun getWaterEntries(): List<WaterEntry> {
        val entriesString = prefs.getString(KEY_WATER_ENTRIES, "") ?: ""

        if (entriesString.isEmpty()) return emptyList()

        return entriesString.split(";")
            .mapNotNull { WaterEntry.fromString(it) }
            .sortedByDescending { it.timestamp }
    }

    fun clearOldEntries(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        val entries = getWaterEntries().filter { it.timestamp >= cutoffTime }

        val entriesString = entries.joinToString(";") { it.toString() }
        prefs.edit().putString(KEY_WATER_ENTRIES, entriesString).apply()
    }
}