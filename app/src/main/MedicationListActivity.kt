package com.medcare.pillreminder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.medcare.pillreminder.data.DataStore

class MedicationListActivity : AppCompatActivity() {

    private lateinit var dataStore: DataStore
    private lateinit var listView: ListView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_list)

        dataStore = DataStore(this)
        listView = findViewById(R.id.listViewMeds)
        tvEmpty = findViewById(R.id.tvEmpty)

        findViewById<Button>(R.id.btnAddMed).setOnClickListener {
            startActivity(Intent(this, AddMedicationActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadMedications()
    }

    private fun loadMedications() {
        val meds = dataStore.loadMedications()
        if (meds.isEmpty()) {
            tvEmpty.visibility = android.view.View.VISIBLE
            listView.visibility = android.view.View.GONE
        } else {
            tvEmpty.visibility = android.view.View.GONE
            listView.visibility = android.view.View.VISIBLE
            val items = meds.map { med ->
                val ampm = if (med.ampm == 0) "오전" else "오후"
                "${med.name} - $ampm ${med.hour}:${String.format("%02d", med.minute)}"
            }
            listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

            listView.setOnItemLongClickListener { _, _, position, _ ->
                val updatedMeds = meds.toMutableList()
                updatedMeds.removeAt(position)
                dataStore.saveMedications(updatedMeds)
                AlarmHelper(this).scheduleAllAlarms()
                loadMedications()
                true
            }
        }
    }
}
