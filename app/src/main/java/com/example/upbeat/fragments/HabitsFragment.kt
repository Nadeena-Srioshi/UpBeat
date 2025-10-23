package com.example.upbeat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upbeat.R
import com.example.upbeat.adapters.HabitAdapter
import com.example.upbeat.models.Habit
import com.example.upbeat.utils.HabitManager
import com.example.upbeat.widget.HabitWidgetProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class HabitsFragment : Fragment() {

    private lateinit var rvHabits: RecyclerView
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var tvCompletionPercentage: TextView
    private lateinit var tvCurrentDate: TextView
    private lateinit var progressBarCompletion: ProgressBar
    private lateinit var spinnerCategory: Spinner

    private lateinit var habitAdapter: HabitAdapter
    private lateinit var habitManager: HabitManager
    private val allHabits = mutableListOf<Habit>()
    private val filteredHabits = mutableListOf<Habit>()
    private var selectedCategory = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        rvHabits = view.findViewById(R.id.rvHabits)
        fabAddHabit = view.findViewById(R.id.fabAddHabit)
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount)
        tvTotalCount = view.findViewById(R.id.tvTotalCount)
        tvCompletionPercentage = view.findViewById(R.id.tvCompletionPercentage)
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate)
        progressBarCompletion = view.findViewById(R.id.progressBarCompletion)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        habitManager = HabitManager(requireContext())
        setupCategoryFilter()
        setupRecyclerView()
        loadHabits()
        updateStats()
        updateCurrentDate()

        fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun setupCategoryFilter() {
        val categories = listOf("All", "Health", "Fitness", "Mental", "Productivity", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                filterHabits()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun filterHabits() {
        val oldList = ArrayList(filteredHabits)
        filteredHabits.clear()

        if (selectedCategory == "All") {
            filteredHabits.addAll(allHabits)
        } else {
            filteredHabits.addAll(allHabits.filter {
                it.category.equals(selectedCategory, ignoreCase = true)
            })
        }

        val diffCallback = HabitDiffCallback(oldList, filteredHabits)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(habitAdapter)

        updateStats()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habits = filteredHabits,
            onHabitClick = { habit ->
                // Handle habit click (e.g., show details)
            },
            onProgressAdd = { habit ->
                if (habit.habitType == Habit.HabitType.COUNT) {
                    showProgressInputDialog(habit)
                }
            },
            onCheckboxChange = { habit, isChecked ->
                if (habit.habitType == Habit.HabitType.BOOLEAN) {
                    toggleBooleanHabit(habit, isChecked)
                }
            }
        )

        rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }
    }

    private fun loadHabits() {
        val currentDate = getCurrentDate()
        val oldList = ArrayList(filteredHabits)
        val loadedHabits = habitManager.getAllHabits()

        // Reset progress if it's a new day
        allHabits.clear()
        allHabits.addAll(loadedHabits.map { habit ->
            if (habit.lastCompletedDate != currentDate) {
                habit.copy(currentValue = 0)
            } else {
                habit
            }
        })

        filterHabits()
    }

    private fun showProgressInputDialog(habit: Habit) {
        val dialog = ProgressInputDialog(habit) { newValue ->
            updateCountHabitProgress(habit, newValue)
        }
        dialog.show(parentFragmentManager, "ProgressInputDialog")
    }

    private fun updateCountHabitProgress(habit: Habit, newValue: Int) {
        val index = allHabits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            val wasCompleted = habit.isCompletedToday()
            val updatedHabit = habit.copy(
                currentValue = newValue.coerceAtMost(habit.goalValue),
                lastCompletedDate = getCurrentDate()
            )

            val isNowCompleted = updatedHabit.isCompletedToday()
            val streak = if (isNowCompleted && !wasCompleted) {
                calculateStreak(habit)
            } else if (!isNowCompleted) {
                0
            } else {
                habit.streak
            }

            allHabits[index] = updatedHabit.copy(streak = streak)
            habitManager.saveHabit(allHabits[index])

            filterHabits()

            val filteredIndex = filteredHabits.indexOfFirst { it.id == habit.id }
            if (filteredIndex != -1) {
                habitAdapter.notifyItemChanged(filteredIndex)
            }

            // Update widget
            HabitWidgetProvider.updateWidgets(requireContext())
        }
    }

    private fun toggleBooleanHabit(habit: Habit, isCompleted: Boolean) {
        val currentDate = getCurrentDate()

        // Only allow changes for today
        if (habit.lastCompletedDate.isNotEmpty() && habit.lastCompletedDate != currentDate) {
            return
        }

        val index = allHabits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            val updatedHabit = habit.copy(
                currentValue = if (isCompleted) habit.goalValue else 0,
                lastCompletedDate = currentDate
            )

            val streak = if (isCompleted) {
                calculateStreak(habit)
            } else {
                0
            }

            allHabits[index] = updatedHabit.copy(streak = streak)
            habitManager.saveHabit(allHabits[index])

            filterHabits()

            val filteredIndex = filteredHabits.indexOfFirst { it.id == habit.id }
            if (filteredIndex != -1) {
                habitAdapter.notifyItemChanged(filteredIndex)
            }

            // Update widget
            HabitWidgetProvider.updateWidgets(requireContext())
        }
    }

    private fun calculateStreak(habit: Habit): Int {
        val currentDate = getCurrentDate()
        val yesterday = getYesterdayDate()
        return when (habit.lastCompletedDate) {
            currentDate, yesterday -> habit.streak + 1
            else -> 1
        }
    }

    private fun updateStats() {
        val completedCount = filteredHabits.count { it.isCompletedToday() }
        val totalCount = filteredHabits.size

        tvCompletedCount.text = completedCount.toString()
        tvTotalCount.text = totalCount.toString()

        val percentage = if (totalCount > 0) {
            ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt()
        } else {
            0
        }

        tvCompletionPercentage.text = "$percentage%"
        progressBarCompletion.progress = percentage
    }

    private fun updateCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        tvCurrentDate.text = dateFormat.format(Date())
    }

    private fun showAddHabitDialog() {
        val dialog = AddHabitDialog { habit ->
            habitManager.saveHabit(habit)
            loadHabits()
            // Update widget when new habit is added
            HabitWidgetProvider.updateWidgets(requireContext())
        }
        dialog.show(parentFragmentManager, "AddHabitDialog")
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getYesterdayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return sdf.format(calendar.time)
    }

    private class HabitDiffCallback(
        private val oldList: List<Habit>,
        private val newList: List<Habit>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}