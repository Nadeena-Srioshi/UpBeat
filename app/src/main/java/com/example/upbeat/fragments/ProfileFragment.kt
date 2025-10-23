package com.example.upbeat.fragments

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.upbeat.R
import com.example.upbeat.models.MoodEntry
import com.example.upbeat.utils.HabitManager
import com.example.upbeat.utils.MoodManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var habitManager: HabitManager
    private lateinit var moodManager: MoodManager
    private lateinit var prefs: SharedPreferences

    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var memberSince: TextView
    private lateinit var profileImage: ImageView
    private lateinit var btnEditProfile: ImageButton

    private lateinit var habitsCompletedCount: TextView
    private lateinit var moodEntriesCount: TextView
    private lateinit var habitsChart: BarChart
    private lateinit var moodChart: PieChart
    private lateinit var btnManageHabits: MaterialButton
    private lateinit var switchNotifications: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        habitManager = HabitManager(requireContext())
        moodManager = MoodManager(requireContext())
        prefs = requireContext().getSharedPreferences("WellnessAppPrefs", 0)

        initViews(view)
        setupProfile()
        setupStats()
        setupCharts()
        setupListeners()

        return view
    }

    private fun initViews(view: View) {
        profileName = view.findViewById(R.id.profileName)
        profileEmail = view.findViewById(R.id.profileEmail)
        memberSince = view.findViewById(R.id.memberSince)
        profileImage = view.findViewById(R.id.profileImage)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)

        habitsCompletedCount = view.findViewById(R.id.habitsCompletedCount)
        moodEntriesCount = view.findViewById(R.id.moodEntriesCount)
        habitsChart = view.findViewById(R.id.habitsChart)
        moodChart = view.findViewById(R.id.moodChart)
        btnManageHabits = view.findViewById(R.id.btnManageHabits)
        switchNotifications = view.findViewById(R.id.switchNotifications)
    }

    private fun setupProfile() {
        val name = prefs.getString("profile_name", "Your Name") ?: "Your Name"
        val email = prefs.getString("profile_email", "your.email@example.com") ?: "your.email@example.com"
        val createdDate = prefs.getLong("profile_created_date", System.currentTimeMillis())

        profileName.text = name
        profileEmail.text = email

        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        memberSince.text = "Member since ${dateFormat.format(Date(createdDate))}"

        // Load notification preference
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        switchNotifications.isChecked = notificationsEnabled
    }

    private fun setupStats() {
        // Get data for the last 7 days
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val weekDates = mutableListOf<String>()
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            weekDates.add(dateFormat.format(calendar.time))
        }

        // Count habits completed this week
        val habits = habitManager.getAllHabits()
        var habitsCompletedThisWeek = 0
        weekDates.forEach { date ->
            habits.forEach { habit ->
                if (habit.lastCompletedDate == date) {
                    habitsCompletedThisWeek++
                }
            }
        }
        habitsCompletedCount.text = habitsCompletedThisWeek.toString()

        // Count mood entries this week
        val allMoods = moodManager.getAllMoodEntries()
        val moodEntriesThisWeek = allMoods.count { it.date in weekDates }
        moodEntriesCount.text = moodEntriesThisWeek.toString()
    }

    private fun setupCharts() {
        setupHabitsChart()
        setupMoodChart()
    }

    private fun setupHabitsChart() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val weekDates = mutableListOf<String>()
        val dayLabels = mutableListOf<String>()

        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            weekDates.add(dateFormat.format(calendar.time))
            dayLabels.add(dayFormat.format(calendar.time))
        }

        val habits = habitManager.getAllHabits()
        val totalHabitsPerDay = habits.size.toFloat()

        val entries = mutableListOf<BarEntry>()
        weekDates.forEachIndexed { index, date ->
            var completedCount = 0f
            habits.forEach { habit ->
                if (habit.lastCompletedDate == date) {
                    completedCount++
                }
            }
            val percentage = if (totalHabitsPerDay > 0) (completedCount / totalHabitsPerDay) * 100 else 0f
            entries.add(BarEntry(index.toFloat(), percentage))
        }

        val dataSet = BarDataSet(entries, "Completion %")
        dataSet.color = Color.parseColor("#6C63FF")
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.parseColor("#2D3436")

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        habitsChart.data = barData
        habitsChart.description.isEnabled = false
        habitsChart.legend.isEnabled = false
        habitsChart.setDrawGridBackground(false)
        habitsChart.animateY(800)

        // X-axis
        val xAxis = habitsChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#636E72")

        // Y-axis
        habitsChart.axisLeft.setDrawGridLines(true)
        habitsChart.axisLeft.axisMinimum = 0f
        habitsChart.axisLeft.axisMaximum = 100f
        habitsChart.axisLeft.textColor = Color.parseColor("#636E72")
        habitsChart.axisRight.isEnabled = false

        habitsChart.invalidate()
    }

    private fun setupMoodChart() {
        val allMoods = moodManager.getAllMoodEntries()

        // Get mood counts
        val moodCounts = mutableMapOf<String, Int>()
        MoodEntry.getMoodOptions().forEach { mood ->
            moodCounts[mood] = 0
        }

        allMoods.forEach { entry ->
            moodCounts[entry.mood] = (moodCounts[entry.mood] ?: 0) + 1
        }

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        moodCounts.forEach { (mood, count) ->
            if (count > 0) {
                entries.add(PieEntry(count.toFloat(), mood))
                colors.add(Color.parseColor(MoodEntry.getMoodColor(mood)))
            }
        }

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Data"))
            colors.add(Color.parseColor("#E0E0E0"))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 2f

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(moodChart))

        moodChart.data = pieData
        moodChart.description.isEnabled = false
        moodChart.legend.isEnabled = true
        moodChart.legend.textColor = Color.parseColor("#636E72")
        moodChart.setDrawEntryLabels(false)
        moodChart.setUsePercentValues(true)
        moodChart.animateY(800)
        moodChart.invalidate()
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        btnManageHabits.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_manageHabitsFragment)
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(
                requireContext(),
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        view?.findViewById<View>(R.id.settingsClearData)?.setOnClickListener {
            showClearDataDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profile, null)

        val editName = dialogView.findViewById<EditText>(R.id.editProfileName)
        val editEmail = dialogView.findViewById<EditText>(R.id.editProfileEmail)

        editName.setText(prefs.getString("profile_name", ""))
        editEmail.setText(prefs.getString("profile_email", ""))

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = editName.text.toString().trim()
                val email = editEmail.text.toString().trim()

                if (name.isNotEmpty()) {
                    prefs.edit()
                        .putString("profile_name", name)
                        .putString("profile_email", email)
                        .apply()

                    setupProfile()
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("This will permanently delete all your habits and mood entries. This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                habitManager.clearAllHabits()
                moodManager.clearAllMoodEntries()

                setupStats()
                setupCharts()

                Toast.makeText(requireContext(), "All data cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        setupStats()
        setupCharts()
    }
}