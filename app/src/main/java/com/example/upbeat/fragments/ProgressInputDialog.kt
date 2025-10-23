package com.example.upbeat.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.upbeat.R
import com.example.upbeat.models.Habit
import com.google.android.material.textfield.TextInputLayout

class ProgressInputDialog(
    private val habit: Habit,
    private val onProgressSet: (Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_progress_input, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvDialogSubtitle)
        val etProgress = view.findViewById<EditText>(R.id.etProgressInput)
        val tilProgress = view.findViewById<TextInputLayout>(R.id.tilProgressInput)

        tvTitle.text = habit.name
        tvSubtitle.text = "Current: ${habit.currentValue}/${habit.goalValue} ${habit.unit}"

        etProgress.inputType = InputType.TYPE_CLASS_NUMBER
        tilProgress.helperText = "Max: ${habit.goalValue}"

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Set") { _, _ ->
                val input = etProgress.text.toString()
                if (input.isNotEmpty()) {
                    try {
                        val value = input.toInt()
                        if (value > 0 && value <= habit.goalValue) {
                            onProgressSet(value)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Please enter a value between 1 and ${habit.goalValue}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(
                            requireContext(),
                            "Please enter a valid number",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}