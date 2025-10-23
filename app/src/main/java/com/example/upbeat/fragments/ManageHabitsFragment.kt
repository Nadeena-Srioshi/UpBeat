package com.example.upbeat.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upbeat.R
import com.example.upbeat.adapters.ManageHabitsAdapter
import com.example.upbeat.models.Habit
import com.example.upbeat.utils.HabitManager

class ManageHabitsFragment : Fragment() {

    private lateinit var habitManager: HabitManager
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: ManageHabitsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_habits, container, false)

        habitManager = HabitManager(requireContext())

        habitsRecyclerView = view.findViewById(R.id.habitsRecyclerView)
        emptyState = view.findViewById(R.id.emptyState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        loadHabits()

        return view
    }

    private fun setupRecyclerView() {
        adapter = ManageHabitsAdapter(
            onEditClick = { habit -> showEditHabitDialog(habit) },
            onDeleteClick = { habit -> showDeleteConfirmation(habit) }
        )

        habitsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        habitsRecyclerView.adapter = adapter

        // Setup swipe gestures
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Reset the swipe
                adapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.7f // Requires 70% swipe to trigger
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(habitsRecyclerView)
    }

    private fun loadHabits() {
        val habits = habitManager.getAllHabits()

        if (habits.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            habitsRecyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            habitsRecyclerView.visibility = View.VISIBLE
            adapter.submitList(habits)
        }
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_habit, null)

        val editHabitName = dialogView.findViewById<android.widget.EditText>(R.id.editHabitName)
        val spinnerCategory = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategory)
        val selectedIcon = dialogView.findViewById<android.widget.TextView>(R.id.selectedIcon)
        val editGoalValue = dialogView.findViewById<android.widget.EditText>(R.id.editGoalValue)
        val editGoalUnit = dialogView.findViewById<android.widget.EditText>(R.id.editGoalUnit)

        // Populate with current habit data
        editHabitName.setText(habit.name)
        selectedIcon.text = habit.icon
        editGoalValue.setText(habit.goalValue.toString())
        editGoalUnit.setText(habit.unit)

        // Setup category spinner
        val categories = arrayOf("Health", "Fitness", "Mental Health", "Productivity", "Other")
        val categoryAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        val categoryIndex = categories.indexOf(habit.category)
        if (categoryIndex >= 0) {
            spinnerCategory.setSelection(categoryIndex)
        }

        // Icon picker (simplified - you can expand this)
        selectedIcon.setOnClickListener {
            showIconPickerDialog { icon ->
                selectedIcon.text = icon
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = editHabitName.text.toString().trim()
                val category = spinnerCategory.selectedItem.toString()
                val icon = selectedIcon.text.toString()
                val goalValue = editGoalValue.text.toString().toIntOrNull() ?: 1
                val unit = editGoalUnit.text.toString().trim()

                if (name.isNotEmpty()) {
                    val updatedHabit = habit.copy(
                        name = name,
                        category = category,
                        icon = icon,
                        goalValue = goalValue,
                        unit = unit
                    )

                    habitManager.saveHabit(updatedHabit)
                    Toast.makeText(requireContext(), "Habit updated", Toast.LENGTH_SHORT).show()
                    loadHabits()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showIconPickerDialog(onIconSelected: (String) -> Unit) {
        val icons = arrayOf("💪", "🏃", "📚", "💧", "🧘", "🎯", "✍️", "🎨", "🎵", "🌱",
            "☀️", "🌙", "⏰", "📝", "🍎", "🥗", "🏋️", "🚴", "🧠", "❤️")

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Icon")
            .setItems(icons) { _, which ->
                onIconSelected(icons[which])
            }
            .show()
    }

    private fun showDeleteConfirmation(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                habitManager.deleteHabit(habit.id)
                Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
                loadHabits()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}