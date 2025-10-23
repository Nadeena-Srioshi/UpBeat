package com.example.upbeat.fragments

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.upbeat.R
import com.example.upbeat.activities.AllMoodsActivity
import com.example.upbeat.models.MoodEntry
import com.example.upbeat.utils.MoodManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class MoodJournalFragment : Fragment() {

    private lateinit var moodManager: MoodManager
    private lateinit var cardAddMood: MaterialCardView
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvViewAll: TextView
    private lateinit var calendarGrid: GridLayout
    private lateinit var btnPreviousMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton

    private val calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("MMM, yyyy", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_journal, container, false)

        moodManager = MoodManager(requireContext())

        initializeViews(view)
        setupCalendar()

        return view
    }

    private fun initializeViews(view: View) {
        cardAddMood = view.findViewById(R.id.cardAddMood)
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth)
        tvViewAll = view.findViewById(R.id.tvViewAll)
        calendarGrid = view.findViewById(R.id.calendarGrid)
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)

        cardAddMood.setOnClickListener {
            showMoodSelectorDialog()
        }

        tvViewAll.setOnClickListener {
            val intent = Intent(requireContext(), AllMoodsActivity::class.java)
            startActivity(intent)
        }

        btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    private fun setupCalendar() {
        updateCalendar()
    }

    private fun updateCalendar() {
        tvCurrentMonth.text = monthFormat.format(calendar.time)

        while (calendarGrid.childCount > 7) {
            calendarGrid.removeViewAt(7)
        }

        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val moodEntries = moodManager.getMoodEntriesForMonth(year, month)

        for (i in 0 until firstDayOfWeek) {
            val emptyView = TextView(requireContext())
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            emptyView.layoutParams = params
            calendarGrid.addView(emptyView)
        }

        for (day in 1..daysInMonth) {
            val dayView = createDayView(day, year, month, moodEntries)
            calendarGrid.addView(dayView)
        }
    }

    private fun createDayView(
        day: Int,
        year: Int,
        month: Int,
        moodEntries: List<MoodEntry>
    ): FrameLayout {
        val frameLayout = FrameLayout(requireContext())
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = 0
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        params.setMargins(4, 4, 4, 4)
        frameLayout.layoutParams = params

        val tempCal = Calendar.getInstance()
        tempCal.set(year, month, day)
        val dateString = dateFormat.format(tempCal.time)

        val dayMood = moodEntries.find { it.date == dateString }

        if (dayMood != null) {
            val moodImage = ImageView(requireContext())
            val imageParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageParams.setMargins(8, 0, 8, 8)
            moodImage.layoutParams = imageParams
            moodImage.scaleType = ImageView.ScaleType.FIT_CENTER

            moodImage.setImageResource(MoodEntry.getMoodDrawable(dayMood.mood))

            frameLayout.addView(moodImage)

            val dayText = TextView(requireContext())
            dayText.text = day.toString()
            dayText.textSize = 10f
            dayText.setTextColor(Color.WHITE)
            dayText.gravity = Gravity.CENTER
            dayText.setPadding(4, 4, 4, 4)

            val background = GradientDrawable()
            background.shape = GradientDrawable.RECTANGLE
            background.cornerRadius = 12f
            background.setColor(Color.parseColor("#80000000"))
            background.setSize(40, 40)
            dayText.background = background

            val textParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            textParams.gravity = Gravity.TOP or Gravity.END
            textParams.setMargins(0, 4, 4, 0)
            dayText.layoutParams = textParams
            frameLayout.addView(dayText)
        } else {
            val dayText = TextView(requireContext())
            dayText.text = day.toString()
            dayText.textSize = 18f
            dayText.setTextColor(Color.parseColor("#2D3436"))
            dayText.gravity = Gravity.CENTER
            val textParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            dayText.layoutParams = textParams
            frameLayout.addView(dayText)
        }

        return frameLayout
    }

    private fun showMoodSelectorDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_mood_selector)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Step tracking
        var selectedMood = ""
        var selectedFeeling = ""

        // Get all views
        val layoutStep1 = dialog.findViewById<LinearLayout>(R.id.layoutStep1)
        val layoutStep2 = dialog.findViewById<LinearLayout>(R.id.layoutStep2)
        val layoutStep3 = dialog.findViewById<LinearLayout>(R.id.layoutStep3)

        // Step 1 views
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val btnBack = dialog.findViewById<ImageButton>(R.id.btnBack)
        val btnMoodSelected = dialog.findViewById<MaterialButton>(R.id.btnMoodSelected)

        val layoutMoodGreat = dialog.findViewById<LinearLayout>(R.id.layoutMoodGreat)
        val layoutMoodGood = dialog.findViewById<LinearLayout>(R.id.layoutMoodGood)
        val layoutMoodOkay = dialog.findViewById<LinearLayout>(R.id.layoutMoodOkay)
        val layoutMoodNotGreat = dialog.findViewById<LinearLayout>(R.id.layoutMoodNotGreat)
        val layoutMoodBad = dialog.findViewById<LinearLayout>(R.id.layoutMoodBad)

        // Step 2 views
        val btnClose2 = dialog.findViewById<ImageButton>(R.id.btnClose2)
        val btnBack2 = dialog.findViewById<ImageButton>(R.id.btnBack2)
        val ivMoodIcon = dialog.findViewById<ImageView>(R.id.ivMoodIcon)
        val chipGroupFeelings = dialog.findViewById<ChipGroup>(R.id.chipGroupFeelings)
        val btnFeelingSelected = dialog.findViewById<MaterialButton>(R.id.btnFeelingSelected)

        // Step 3 views
        val btnClose3 = dialog.findViewById<ImageButton>(R.id.btnClose3)
        val btnBack3 = dialog.findViewById<ImageButton>(R.id.btnBack3)
        val tvReasonTitle = dialog.findViewById<TextView>(R.id.tvReasonTitle)
        val etReason = dialog.findViewById<TextInputEditText>(R.id.etReason)
        val btnReasonSubmit = dialog.findViewById<MaterialButton>(R.id.btnReasonSubmit)

        // Close buttons
        btnClose.setOnClickListener { dialog.dismiss() }
        btnBack.setOnClickListener { dialog.dismiss() }
        btnClose2.setOnClickListener { dialog.dismiss() }
        btnClose3.setOnClickListener { dialog.dismiss() }

        // Step 1: Mood selection
        val moodClickListener = View.OnClickListener { v ->
            when (v.id) {
                R.id.layoutMoodGreat -> selectedMood = "Great"
                R.id.layoutMoodGood -> selectedMood = "Good"
                R.id.layoutMoodOkay -> selectedMood = "Okay"
                R.id.layoutMoodNotGreat -> selectedMood = "Not Great"
                R.id.layoutMoodBad -> selectedMood = "Bad"
            }
            btnMoodSelected.isEnabled = true
            btnMoodSelected.text = "My mood is $selectedMood"
            btnMoodSelected.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
        }

        layoutMoodGreat.setOnClickListener(moodClickListener)
        layoutMoodGood.setOnClickListener(moodClickListener)
        layoutMoodOkay.setOnClickListener(moodClickListener)
        layoutMoodNotGreat.setOnClickListener(moodClickListener)
        layoutMoodBad.setOnClickListener(moodClickListener)

        btnMoodSelected.setOnClickListener {
            // Move to step 2
            layoutStep1.visibility = View.GONE
            layoutStep2.visibility = View.VISIBLE

            // Set background color based on mood
            val bgColor = MoodEntry.getMoodColor(selectedMood)
            layoutStep2.setBackgroundColor(Color.parseColor(bgColor))

            // Show mood drawable
            ivMoodIcon.setImageResource(MoodEntry.getMoodDrawable(selectedMood))

            // Populate feelings
            chipGroupFeelings.removeAllViews()
            val feelings = MoodEntry.getFeelingsForMood(selectedMood)
            for (feeling in feelings) {
                val chip = Chip(requireContext())
                chip.text = feeling
                chip.isCheckable = true
                chip.setChipBackgroundColorResource(android.R.color.white)
                chip.setTextColor(Color.BLACK)
                chip.chipCornerRadius = 50f
                chip.chipStrokeWidth = 2f
                chip.setChipStrokeColorResource(android.R.color.transparent)

                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedFeeling = feeling
                        btnFeelingSelected.isEnabled = true
                        btnFeelingSelected.text = "I feel $selectedFeeling"
                        btnFeelingSelected.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
                    }
                }

                chipGroupFeelings.addView(chip)
            }
        }

        btnBack2.setOnClickListener {
            layoutStep2.visibility = View.GONE
            layoutStep1.visibility = View.VISIBLE
            selectedFeeling = ""
            btnFeelingSelected.isEnabled = false
            btnFeelingSelected.text = "I feel..."
        }

        btnFeelingSelected.setOnClickListener {
            // Move to step 3
            layoutStep2.visibility = View.GONE
            layoutStep3.visibility = View.VISIBLE

            // Set background color based on mood
            val bgColor = MoodEntry.getMoodColor(selectedMood)
            layoutStep3.setBackgroundColor(Color.parseColor(bgColor))

            tvReasonTitle.text = "What makes you feel $selectedFeeling?"
        }

        btnBack3.setOnClickListener {
            layoutStep3.visibility = View.GONE
            layoutStep2.visibility = View.VISIBLE
        }

        btnReasonSubmit.setOnClickListener {
            val reason = etReason.text.toString().trim()

            // Save mood entry
            saveMoodEntry(selectedMood, selectedFeeling, reason)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveMoodEntry(mood: String, feeling: String, reason: String) {
        val moodEntry = MoodEntry(
            id = UUID.randomUUID().toString(),
            mood = mood,
            feeling = feeling,
            reason = reason,
            date = moodManager.getTodayDate(),
            time = moodManager.getCurrentTime()
        )

        moodManager.addMoodEntry(moodEntry)
        updateCalendar()

        Toast.makeText(requireContext(), "Mood logged successfully! 🎉", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateCalendar()
    }
}
