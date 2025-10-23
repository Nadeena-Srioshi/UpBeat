package com.example.upbeat.models

import org.json.JSONObject

data class Habit(
    val id: String,
    val name: String,
    val icon: String,
    val category: String,
    val color: String = "#FFB3D9",
    val goalValue: Int = 1,
    val currentValue: Int = 0,
    val unit: String = "times",
    val createdDate: Long = System.currentTimeMillis(),
    var lastCompletedDate: String = "",
    var streak: Int = 0,
    val habitType: HabitType = HabitType.BOOLEAN // New attribute
) {
    enum class HabitType {
        BOOLEAN,  // Yes/No checkbox habits
        COUNT     // Count-based habits with progress
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("icon", icon)
            put("category", category)
            put("color", color)
            put("goalValue", goalValue)
            put("currentValue", currentValue)
            put("unit", unit)
            put("createdDate", createdDate)
            put("lastCompletedDate", lastCompletedDate)
            put("streak", streak)
            put("habitType", habitType.name)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Habit {
            return Habit(
                id = json.getString("id"),
                name = json.getString("name"),
                icon = json.getString("icon"),
                category = json.getString("category"),
                color = json.optString("color", "#FFB3D9"),
                goalValue = json.getInt("goalValue"),
                currentValue = json.getInt("currentValue"),
                unit = json.getString("unit"),
                createdDate = json.getLong("createdDate"),
                lastCompletedDate = json.optString("lastCompletedDate", ""),
                streak = json.optInt("streak", 0),
                habitType = try {
                    HabitType.valueOf(json.optString("habitType", "BOOLEAN"))
                } catch (e: Exception) {
                    HabitType.BOOLEAN
                }
            )
        }
    }

    fun isCompletedToday(): Boolean {
        return currentValue >= goalValue
    }

    fun getProgressPercentage(): Int {
        return ((currentValue.toFloat() / goalValue.toFloat()) * 100).toInt().coerceIn(0, 100)
    }
}