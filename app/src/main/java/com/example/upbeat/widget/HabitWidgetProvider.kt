package com.example.upbeat.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.upbeat.MainActivity
import com.example.upbeat.R
import com.example.upbeat.utils.HabitManager
import java.text.SimpleDateFormat
import java.util.*

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, HabitWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.upbeat.ACTION_UPDATE_WIDGET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val habitManager = HabitManager(context)
            val habits = habitManager.getAllHabits()

            val currentDate = getCurrentDate()

            // Filter habits for today
            val todayHabits = habits.map { habit ->
                if (habit.lastCompletedDate != currentDate) {
                    habit.copy(currentValue = 0)
                } else {
                    habit
                }
            }

            val totalHabits = todayHabits.size
            val completedHabits = todayHabits.count { it.isCompletedToday() }

            val percentage = if (totalHabits > 0) {
                ((completedHabits.toFloat() / totalHabits.toFloat()) * 100).toInt()
            } else {
                0
            }

            // Create RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_habit_tracker)

            // Update text views
            views.setTextViewText(R.id.tvWidgetPercentage, "$percentage%")
            views.setTextViewText(R.id.tvWidgetCompleted, "$completedHabits/$totalHabits")
            views.setTextViewText(R.id.tvWidgetDate, getFormattedDate())

            // Update progress bar
            views.setProgressBar(R.id.progressWidgetCompletion, 100, percentage, false)

            // Set motivational message
            val message = when {
                percentage == 100 -> "🎉 Perfect day!"
                percentage >= 75 -> "💪 Almost there!"
                percentage >= 50 -> "👍 Keep going!"
                percentage >= 25 -> "🌱 Good start!"
                else -> "⭐ Start your day!"
            }
            views.setTextViewText(R.id.tvWidgetMessage, message)

            // Set up click intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)

            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateWidgets(context: Context) {
            val intent = Intent(context, HabitWidgetProvider::class.java)
            intent.action = ACTION_UPDATE_WIDGET
            context.sendBroadcast(intent)
        }

        private fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        private fun getFormattedDate(): String {
            val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}