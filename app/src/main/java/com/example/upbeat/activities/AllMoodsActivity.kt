package com.example.upbeat.activities

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upbeat.R
import com.example.upbeat.adapters.MoodEntryAdapter
import com.example.upbeat.models.MoodEntry
import com.example.upbeat.utils.MoodManager
import com.google.android.material.appbar.MaterialToolbar

class AllMoodsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvAllMoods: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var moodManager: MoodManager
    private lateinit var adapter: MoodEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_moods)

        moodManager = MoodManager(this)

        initializeViews()
        loadMoodEntries()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        rvAllMoods = findViewById(R.id.rvAllMoods)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        rvAllMoods.layoutManager = LinearLayoutManager(this)
    }

    private fun loadMoodEntries() {
        val allEntries = moodManager.getAllMoodEntries()
            .sortedByDescending { it.timestamp }

        if (allEntries.isEmpty()) {
            rvAllMoods.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            rvAllMoods.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE

            adapter = MoodEntryAdapter(
                moodEntries = allEntries.toMutableList(),
                onDeleteClick = { moodEntry ->
                    showDeleteConfirmation(moodEntry)
                }
            )
            rvAllMoods.adapter = adapter
        }
    }

    private fun showDeleteConfirmation(moodEntry: MoodEntry) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                moodManager.deleteMoodEntry(moodEntry.id)
                loadMoodEntries()
                android.widget.Toast.makeText(
                    this,
                    "Mood entry deleted",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadMoodEntries()
    }
}