package com.example.upbeat.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upbeat.R
import com.example.upbeat.models.Habit
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox

class HabitAdapter(
    private val habits: List<Habit>,
    private val onHabitClick: (Habit) -> Unit,
    private val onProgressAdd: (Habit) -> Unit,
    private val onCheckboxChange: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardHabit: MaterialCardView = itemView.findViewById(R.id.cardHabit)
        private val viewColorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
        private val habitCard: LinearLayout = itemView.findViewById(R.id.habitCard)
        private val tvHabitIcon: TextView = itemView.findViewById(R.id.tvHabitIcon)
        private val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        private val tvHabitProgress: TextView = itemView.findViewById(R.id.tvHabitProgress)
        private val tvHabitStreak: TextView = itemView.findViewById(R.id.tvHabitStreak)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val checkboxComplete: MaterialCheckBox = itemView.findViewById(R.id.checkboxComplete)
        private val btnAdd: ImageButton = itemView.findViewById(R.id.btnAdd)
        private val ivCompletedIcon: ImageView = itemView.findViewById(R.id.ivCompletedIcon)

        fun bind(habit: Habit) {
            // Set color indicator
            try {
                viewColorIndicator.setBackgroundColor(Color.parseColor(habit.color))
                habitCard.setBackgroundColor(Color.parseColor(habit.color))
            } catch (e: Exception) {
                viewColorIndicator.setBackgroundColor(Color.parseColor("#FFB3D9"))
            }

            tvHabitIcon.text = habit.icon
            tvHabitName.text = habit.name

            when (habit.habitType) {
                Habit.HabitType.BOOLEAN -> {
                    // Boolean habit - show checkbox
                    checkboxComplete.visibility = View.VISIBLE
                    btnAdd.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    ivCompletedIcon.visibility = View.GONE
                    tvHabitProgress.text = habit.category

                    // Remove listener before setting checked state
                    checkboxComplete.setOnCheckedChangeListener(null)
                    checkboxComplete.isChecked = habit.isCompletedToday()

                    // Add listener after setting state
                    checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
                        onCheckboxChange(habit, isChecked)
                    }
                }

                Habit.HabitType.COUNT -> {
                    // Count-based habit
                    checkboxComplete.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    tvHabitProgress.text = "${habit.currentValue}/${habit.goalValue} ${habit.unit}"
                    progressBar.progress = habit.getProgressPercentage()

                    if (habit.isCompletedToday()) {
                        // Completed - show check icon
                        btnAdd.visibility = View.GONE
                        ivCompletedIcon.visibility = View.VISIBLE
                    } else {
                        // Not completed - show add button
                        btnAdd.visibility = View.VISIBLE
                        ivCompletedIcon.visibility = View.GONE
                        btnAdd.setOnClickListener {
                            onProgressAdd(habit)
                        }
                    }
                }
            }

            // Show streak if > 0
            if (habit.streak > 0) {
                tvHabitStreak.visibility = View.VISIBLE
                tvHabitStreak.text = "🔥 ${habit.streak} days"
            } else {
                tvHabitStreak.visibility = View.GONE
            }

            cardHabit.setOnClickListener {
                onHabitClick(habit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size
}