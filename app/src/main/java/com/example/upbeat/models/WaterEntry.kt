package com.example.upbeat.models

data class WaterEntry(
    val amount: Int,
    val timestamp: Long,
    val date: String
) {
    companion object {
        fun fromString(str: String): WaterEntry? {
            return try {
                val parts = str.split("|")
                if (parts.size == 3) {
                    WaterEntry(
                        amount = parts[0].toInt(),
                        timestamp = parts[1].toLong(),
                        date = parts[2]
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun toString(): String {
        return "$amount|$timestamp|$date"
    }
}