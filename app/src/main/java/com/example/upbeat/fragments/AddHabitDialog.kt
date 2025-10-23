package com.example.upbeat.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.upbeat.R
import com.example.upbeat.models.Habit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class AddHabitDialog(
    private val onHabitCreated: (Habit) -> Unit
) : DialogFragment() {

    // UI references
    private lateinit var etHabitName: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var llColorContainer: LinearLayout
    private lateinit var llIconContainer: LinearLayout
    private lateinit var rgHabitType: RadioGroup
    private lateinit var rbCountBased: RadioButton
    private lateinit var rbYesNo: RadioButton
    private lateinit var llCountOptions: LinearLayout
    private lateinit var etGoalValue: TextInputEditText
    private lateinit var etUnit: TextInputEditText
    private lateinit var btnCancel: Button
    private lateinit var btnCreate: Button
    private var dialogView: View? = null

    private var selectedColor: String = "#FFB3D9"
    private var selectedIcon: String = "☀️"

    private val habitColors = listOf(
        "#FFB3D9", "#FFD4A3", "#FFF59D", "#C5E1A5",
        "#B3D9FF", "#B2DFDB", "#D1C4E9"
    )

    private val habitIcons = listOf(
        "☀️", "💧", "🏃", "📚", "💊", "🧘", "🥗",
        "😴", "💪", "🎯", "✍️", "🎵", "🌱", "🧠"
    )

    private val categories = listOf(
        "Health", "Fitness", "Mind", "Medication", "Productivity", "Other"
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_habit, null)
        dialogView = view
        initViews(view)

        setupColorPicker()
        setupIconPicker()
        setupCategorySpinner()
        setupHabitTypeToggle()
        setupButtons()

        return MaterialAlertDialogBuilder(requireContext(), R.style.RoundedDialog)
            .setView(view)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        updateDialogBackground()
    }

    private fun initViews(view: View) {
        etHabitName = view.findViewById(R.id.etHabitName)
        etDescription = view.findViewById(R.id.etDescription)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        llColorContainer = view.findViewById(R.id.llColorContainer)
        llIconContainer = view.findViewById(R.id.llIconContainer)
        rgHabitType = view.findViewById(R.id.rgHabitType)
        rbCountBased = view.findViewById(R.id.rbCountBased)
        rbYesNo = view.findViewById(R.id.rbYesNo)
        llCountOptions = view.findViewById(R.id.llCountOptions)
        etGoalValue = view.findViewById(R.id.etGoalValue)
        etUnit = view.findViewById(R.id.etUnit)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnCreate = view.findViewById(R.id.btnCreate)
    }

    private fun setupColorPicker() {
        llColorContainer.removeAllViews()
        val sizeInPx = (40 * requireContext().resources.displayMetrics.density).toInt()
        val marginInPx = (8 * requireContext().resources.displayMetrics.density).toInt()
        habitColors.forEachIndexed { index, color ->
            val colorView = View(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(sizeInPx, sizeInPx).apply {
                    setMargins(marginInPx, 0, marginInPx, 0)
                }
                background = createColorCircle(color, index == 0)
                tag = color
                setOnClickListener {
                    selectColor(color)
                }
            }
            llColorContainer.addView(colorView)
        }
    }

    private fun setupIconPicker() {
        llIconContainer.removeAllViews()
        val sizeInPx = (48 * requireContext().resources.displayMetrics.density).toInt()
        val marginInPx = (4 * requireContext().resources.displayMetrics.density).toInt()
        habitIcons.forEachIndexed { index, icon ->
            val iconView = TextView(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(sizeInPx, sizeInPx).apply {
                    setMargins(marginInPx, 0, marginInPx, 0)
                }
                text = icon
                textSize = 28f
                gravity = android.view.Gravity.CENTER
                background = if (index == 0) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.icon_selected_background)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle)
                }
                setOnClickListener {
                    selectIcon(icon)
                }
            }
            llIconContainer.addView(iconView)
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCategory.adapter = adapter
    }

    private fun setupHabitTypeToggle() {
        rgHabitType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCountBased) {
                llCountOptions.visibility = View.VISIBLE
            } else {
                llCountOptions.visibility = View.GONE
            }
        }
    }

    private fun setupButtons() {
        btnCancel.setOnClickListener { dismiss() }
        btnCreate.setOnClickListener { createHabit() }
    }

    private fun selectColor(color: String) {
        selectedColor = color

        for (i in 0 until llColorContainer.childCount) {
            val child = llColorContainer.getChildAt(i)
            val childColor = child.tag as String
            child.background = createColorCircle(childColor, childColor == color)
        }

        updateDialogBackground()
    }

    private fun selectIcon(icon: String) {
        selectedIcon = icon

        for (i in 0 until llIconContainer.childCount) {
            val child = llIconContainer.getChildAt(i) as TextView
            child.background = if (child.text == icon) {
                ContextCompat.getDrawable(requireContext(), R.drawable.icon_selected_background)
            } else {
                ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle)
            }
        }
    }

    private fun createColorCircle(color: String, isSelected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(color))
            if (isSelected) {
                setStroke(6, ContextCompat.getColor(requireContext(), R.color.primary))
            }
        }
    }

    private fun updateDialogBackground() {
        try {
            val color = Color.parseColor(selectedColor)
            dialogView?.setBackgroundColor(color)
        } catch (_: Exception) { }
    }

    private fun createHabit() {
        val name = etHabitName.text.toString().trim()
        if (name.isEmpty()) {
            etHabitName.error = "Please enter habit name"
            return
        }

        val category = spinnerCategory.selectedItem.toString()
        val isCountBased = rbCountBased.isChecked

        // Determine habit type based on radio button selection
        val habitType = if (isCountBased) {
            Habit.HabitType.COUNT
        } else {
            Habit.HabitType.BOOLEAN
        }

        val goalValue = if (isCountBased) {
            etGoalValue.text.toString().toIntOrNull() ?: 1
        } else 1

        val unit = if (isCountBased) {
            etUnit.text.toString().trim().ifEmpty { "times" }
        } else "times"

        val habit = Habit(
            id = UUID.randomUUID().toString(),
            name = name,
            icon = selectedIcon,
            category = category,
            color = selectedColor,
            goalValue = goalValue,
            currentValue = 0,
            unit = unit,
            createdDate = System.currentTimeMillis(),
            lastCompletedDate = "",
            streak = 0,
            habitType = habitType  // Add habitType parameter
        )

        onHabitCreated(habit)
        dismiss()
    }
}