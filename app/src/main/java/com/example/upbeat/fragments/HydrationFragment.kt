package com.example.upbeat.fragments

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upbeat.R
import com.example.upbeat.adapters.WaterHistoryAdapter
import com.example.upbeat.models.WaterEntry
import com.example.upbeat.utils.HydrationPreferences
import com.example.upbeat.utils.NotificationPermissionHelper
import com.example.upbeat.utils.WaterReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

class HydrationFragment : Fragment() {

    private lateinit var tvCurrentIntake: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvAverageDailyHydration: TextView
    private lateinit var tvComplianceDays: TextView
    private lateinit var tvNumberOfDrinks: TextView
    private lateinit var tvWeeklyIntake: TextView
    private lateinit var btnAddWater: Button
    private lateinit var btnSettings: ImageButton
    private lateinit var btnHistory: ImageButton
    private lateinit var rvWaterHistory: RecyclerView

    private lateinit var hydrationPrefs: HydrationPreferences
    private lateinit var waterHistoryAdapter: WaterHistoryAdapter
    private var waterEntries = mutableListOf<WaterEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        hydrationPrefs = HydrationPreferences(requireContext())

        createNotificationChannel()
        setupRecyclerView()
        loadData()
        updateUI()
        setupClickListeners()

        // Request notification permission for Android 13+
        NotificationPermissionHelper.requestNotificationPermission(requireActivity())

        if (hydrationPrefs.isRemindersEnabled()) {
            scheduleWaterReminders()
        }
    }

    private fun initViews(view: View) {
        tvCurrentIntake = view.findViewById(R.id.tvCurrentIntake)
        tvGoal = view.findViewById(R.id.tvGoal)
        tvAverageDailyHydration = view.findViewById(R.id.tvAverageDailyHydration)
        tvComplianceDays = view.findViewById(R.id.tvComplianceDays)
        tvNumberOfDrinks = view.findViewById(R.id.tvNumberOfDrinks)
        tvWeeklyIntake = view.findViewById(R.id.tvWeeklyIntake)
        btnAddWater = view.findViewById(R.id.btnAddWater)
        btnSettings = view.findViewById(R.id.btnSettings)
        btnHistory = view.findViewById(R.id.btnHistory)
        rvWaterHistory = view.findViewById(R.id.rvWaterHistory)
    }

    private fun setupRecyclerView() {
        waterHistoryAdapter = WaterHistoryAdapter(waterEntries)
        rvWaterHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = waterHistoryAdapter
        }
    }

    private fun setupClickListeners() {
        btnAddWater.setOnClickListener {
            showAddWaterDialog()
        }

        btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        btnHistory.setOnClickListener {
            toggleHistoryVisibility()
        }
    }

    private fun showAddWaterDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_water, null)

        val btn12oz = dialogView.findViewById<View>(R.id.btn12oz)
        val btn16oz = dialogView.findViewById<View>(R.id.btn16oz)
        val btn20oz = dialogView.findViewById<View>(R.id.btn20oz)
        val btn24oz = dialogView.findViewById<View>(R.id.btn24oz)
        val btn30oz = dialogView.findViewById<View>(R.id.btn30oz)

        val alertDialog = dialog.setView(dialogView)
            .setCancelable(true)
            .create()

        val onAmountClick = View.OnClickListener { v ->
            val amount = when (v.id) {
                R.id.btn12oz -> 12
                R.id.btn16oz -> 16
                R.id.btn20oz -> 20
                R.id.btn24oz -> 24
                R.id.btn30oz -> 30
                else -> 0
            }
            addWaterIntake(amount)
            alertDialog.dismiss()
        }

        btn12oz.setOnClickListener(onAmountClick)
        btn16oz.setOnClickListener(onAmountClick)
        btn20oz.setOnClickListener(onAmountClick)
        btn24oz.setOnClickListener(onAmountClick)
        btn30oz.setOnClickListener(onAmountClick)

        alertDialog.show()
    }

    private fun showSettingsDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_hydration_settings, null)

        val btnReminders = dialogView.findViewById<View>(R.id.btnReminders)
        val btnReminderTime = dialogView.findViewById<View>(R.id.btnReminderTime)
        val btnGoal = dialogView.findViewById<View>(R.id.btnGoal)
        val tvRemindersStatus = dialogView.findViewById<TextView>(R.id.tvRemindersStatus)
        val tvReminderTime = dialogView.findViewById<TextView>(R.id.tvReminderTime)
        val tvCurrentGoal = dialogView.findViewById<TextView>(R.id.tvCurrentGoal)

        tvRemindersStatus.text = if (hydrationPrefs.isRemindersEnabled()) "On" else "Off"
        tvCurrentGoal.text = "${hydrationPrefs.getDailyGoal()} ml"
        updateReminderTimeDisplay(tvReminderTime)

        val alertDialog = dialog.setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (hydrationPrefs.isRemindersEnabled()) {
                    scheduleWaterReminders()
                } else {
                    cancelWaterReminders()
                }
                updateUI()
            }
            .setNegativeButton("Cancel", null)
            .create()

        btnReminders.setOnClickListener {
            val enabled = !hydrationPrefs.isRemindersEnabled()
            hydrationPrefs.setRemindersEnabled(enabled)
            tvRemindersStatus.text = if (enabled) "On" else "Off"
        }

        btnReminderTime.setOnClickListener {
            showReminderTimeDialog(tvReminderTime)
        }

        btnGoal.setOnClickListener {
            showGoalDialog(tvCurrentGoal)
        }

        alertDialog.show()
    }

    private fun showReminderTimeDialog(tvReminderTime: TextView) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_reminder_time, null)

        val hoursPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.hoursPicker)
        val minutesPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.minutesPicker)
        val tvPreview = dialogView.findViewById<TextView>(R.id.tvReminderPreview)

        // Configure hours picker (0-23)
        hoursPicker.minValue = 0
        hoursPicker.maxValue = 23
        hoursPicker.value = hydrationPrefs.getReminderHours()
        hoursPicker.wrapSelectorWheel = true

        // Configure minutes picker (0, 5, 10, ..., 55)
        val minuteValues = arrayOf("0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55")
        minutesPicker.minValue = 0
        minutesPicker.maxValue = minuteValues.size - 1
        minutesPicker.displayedValues = minuteValues
        minutesPicker.value = hydrationPrefs.getReminderMinutes() / 5
        minutesPicker.wrapSelectorWheel = true

        // Update preview when values change
        val updatePreview = {
            val hours = hoursPicker.value
            val minutes = minuteValues[minutesPicker.value].toInt()
            val previewText = buildReminderPreviewText(hours, minutes)
            tvPreview.text = previewText
        }

        hoursPicker.setOnValueChangedListener { _, _, _ -> updatePreview() }
        minutesPicker.setOnValueChangedListener { _, _, _ -> updatePreview() }

        // Initial preview
        updatePreview()

        dialog.setView(dialogView)
            .setPositiveButton("Set") { _, _ ->
                val hours = hoursPicker.value
                val minutes = minuteValues[minutesPicker.value].toInt()

                // Validate: must be at least 5 minutes
                if (hours == 0 && minutes < 5) {
                    Toast.makeText(
                        requireContext(),
                        "Minimum interval is 5 minutes",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                hydrationPrefs.setReminderHours(hours)
                hydrationPrefs.setReminderMinutes(minutes)
                updateReminderTimeDisplay(tvReminderTime)

                // Reschedule reminders if enabled
                if (hydrationPrefs.isRemindersEnabled()) {
                    cancelWaterReminders()
                    scheduleWaterReminders()
                }

                Toast.makeText(
                    requireContext(),
                    "Reminder interval updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateReminderTimeDisplay(textView: TextView) {
        val hours = hydrationPrefs.getReminderHours()
        val minutes = hydrationPrefs.getReminderMinutes()

        textView.text = when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "Not set"
        }
    }

    private fun buildReminderPreviewText(hours: Int, minutes: Int): String {
        return when {
            hours == 0 && minutes == 0 -> "Please select a valid interval"
            hours == 0 && minutes < 5 -> "Minimum interval is 5 minutes"
            hours > 0 && minutes > 0 -> "You will be reminded every $hours hour${if (hours != 1) "s" else ""} and $minutes minute${if (minutes != 1) "s" else ""}"
            hours > 0 -> "You will be reminded every $hours hour${if (hours != 1) "s" else ""}"
            else -> "You will be reminded every $minutes minute${if (minutes != 1) "s" else ""}"
        }
    }

    private fun showGoalDialog(tvCurrentGoal: TextView) {
        val goals = arrayOf("64 ml", "72 ml", "82 ml", "96 ml", "128 ml")
        val goalValues = arrayOf(64, 72, 82, 96, 128)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Daily Goal")
            .setItems(goals) { _, which ->
                hydrationPrefs.setDailyGoal(goalValues[which])
                tvCurrentGoal.text = goals[which]
                updateUI()
            }
            .show()
    }

    private fun addWaterIntake(amount: Int) {
        val currentIntake = hydrationPrefs.getTodayIntake()
        hydrationPrefs.setTodayIntake(currentIntake + amount)

        val entry = WaterEntry(
            amount = amount,
            timestamp = System.currentTimeMillis(),
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        hydrationPrefs.addWaterEntry(entry)

        waterEntries.add(0, entry)
        waterHistoryAdapter.notifyItemInserted(0)

        updateUI()
        Toast.makeText(requireContext(), "Added $amount ml", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        waterEntries.clear()
        waterEntries.addAll(hydrationPrefs.getWaterEntries())
        waterHistoryAdapter.notifyDataSetChanged()
    }

    private fun updateUI() {
        val currentIntake = hydrationPrefs.getTodayIntake()
        val goal = hydrationPrefs.getDailyGoal()

        tvCurrentIntake.text = currentIntake.toString()
        tvGoal.text = "$goal ml"

        // Calculate statistics
        val entries = hydrationPrefs.getWaterEntries()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Average daily hydration (last 7 days)
        val last7Days = entries.filter {
            val entryDate = Date(it.timestamp)
            val daysDiff = (System.currentTimeMillis() - entryDate.time) / (1000 * 60 * 60 * 24)
            daysDiff < 7
        }

        val dailyTotals = last7Days.groupBy { it.date }
            .mapValues { it.value.sumOf { entry -> entry.amount } }

        val avgDaily = if (dailyTotals.isNotEmpty()) {
            dailyTotals.values.average().toInt()
        } else 0

        tvAverageDailyHydration.text = "$avgDaily ml"

        // Compliance days (days goal was met in last 7 days)
        val complianceDays = dailyTotals.count { it.value >= goal }
        tvComplianceDays.text = "$complianceDays days"

        // Number of drinks today
        val todayDrinks = entries.count { it.date == today }
        tvNumberOfDrinks.text = "$todayDrinks times"

        // Weekly intake
        val weeklyIntake = dailyTotals.values.sum()
        tvWeeklyIntake.text = "$weeklyIntake ml"
    }

    private fun toggleHistoryVisibility() {
        rvWaterHistory.visibility = if (rvWaterHistory.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun scheduleWaterReminders() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get custom interval from preferences
        val intervalMillis = hydrationPrefs.getReminderIntervalMillis()
        val triggerTime = System.currentTimeMillis() + intervalMillis

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intervalMillis,
            pendingIntent
        )

        val hours = hydrationPrefs.getReminderHours()
        val minutes = hydrationPrefs.getReminderMinutes()
        val timeText = when {
            hours > 0 && minutes > 0 -> "$hours hour${if (hours != 1) "s" else ""} $minutes minute${if (minutes != 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours != 1) "s" else ""}"
            else -> "$minutes minute${if (minutes != 1) "s" else ""}"
        }

        Toast.makeText(
            requireContext(),
            "Reminders set for every $timeText",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun cancelWaterReminders() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Toast.makeText(requireContext(), "Water reminders disabled", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "water_reminder_channel",
                "Water Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water"
            }

            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}