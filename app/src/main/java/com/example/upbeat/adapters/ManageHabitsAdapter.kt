package com.example.upbeat.adapters

import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.upbeat.R
import com.example.upbeat.models.Habit

class ManageHabitsAdapter(
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : ListAdapter<Habit, ManageHabitsAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_habit, parent, false)
        return HabitViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HabitViewHolder(
        itemView: View,
        private val onEditClick: (Habit) -> Unit,
        private val onDeleteClick: (Habit) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val foregroundCard: CardView = itemView.findViewById(R.id.foregroundCard)
        private val habitIcon: TextView = itemView.findViewById(R.id.habitIcon)
        private val habitName: TextView = itemView.findViewById(R.id.habitName)
        private val habitCategory: TextView = itemView.findViewById(R.id.habitCategory)
        private val habitStreak: TextView = itemView.findViewById(R.id.habitStreak)
        private val btnEdit: View = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: View = itemView.findViewById(R.id.btnDelete)

        private var dX = 0f
        private var initialX = 0f
        private val maxSwipeDistance = 164f // 80dp * 2 + 4dp margin (converted to pixels)

        fun bind(habit: Habit) {
            habitIcon.text = habit.icon
            habitName.text = habit.name
            habitCategory.text = habit.category

            val streakText = if (habit.streak == 1) "1 day streak" else "${habit.streak} days streak"
            habitStreak.text = streakText

            // Set background color based on habit color
            try {
                habitIcon.setBackgroundColor(Color.parseColor(habit.color))
            } catch (e: Exception) {
                habitIcon.setBackgroundColor(Color.parseColor("#FFB3D9"))
            }

            // Reset position
            foregroundCard.translationX = 0f

            // Setup swipe gesture
            setupSwipeGesture(habit)

            // Setup button clicks
            btnEdit.setOnClickListener {
                onEditClick(habit)
                resetPosition()
            }

            btnDelete.setOnClickListener {
                onDeleteClick(habit)
            }
        }

        private fun setupSwipeGesture(habit: Habit) {
            foregroundCard.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        initialX = view.x
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newX = event.rawX + dX

                        // Only allow swiping to the left (revealing buttons on the right)
                        if (newX <= 0 && newX >= -maxSwipeDistance * view.context.resources.displayMetrics.density) {
                            view.animate()
                                .x(newX)
                                .setDuration(0)
                                .start()
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val movedDistance = initialX - view.x
                        val density = view.context.resources.displayMetrics.density

                        // If swiped more than 80dp, keep it open, otherwise close it
                        if (movedDistance > 80 * density) {
                            // Snap to open position
                            animateToPosition(view, -maxSwipeDistance * density)
                        } else {
                            // Snap back to closed position
                            resetPosition()
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        private fun animateToPosition(view: View, position: Float) {
            ObjectAnimator.ofFloat(view, "translationX", position).apply {
                duration = 200
                start()
            }
        }

        private fun resetPosition() {
            animateToPosition(foregroundCard, 0f)
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}