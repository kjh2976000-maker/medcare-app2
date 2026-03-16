package com.medcare.pillreminder

import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.medcare.pillreminder.data.DataStore
import com.medcare.pillreminder.data.Medication

class AddMedicationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 키보드 올라올 때 화면 밀어올리기
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.activity_add_medication)

        val etName = findViewById<EditText>(R.id.etMedName)
        val etDosage = findViewById<EditText>(R.id.etMedDosage)
        val timePicker = findViewById<TimePicker>(R.id.timePickerMed)
        val btnSave = findViewById<Button>(R.id.btnSaveMed)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        timePicker.setIs24HourView(true)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "약 이름을 입력해 주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hour = timePicker.hour
            val minute = timePicker.minute

            val med = Medication(
                name = name,
                dosage = etDosage.text.toString().trim(),
                hour = hour,
                minute = minute,
                ampm = if (hour < 12) 0 else 1,
                days = listOf(0, 1, 2, 3, 4, 5, 6)
            )

            val dataStore = DataStore(this)
            val meds = dataStore.loadMedications()
            meds.add(med)
            dataStore.saveMedications(meds)

            AlarmHelper(this).scheduleAllAlarms()

            Toast.makeText(this, "${name} 알람 설정 완료 (${hour}시 ${minute}분)", Toast.LENGTH_LONG).show()
            finish()
        }

        btnCancel.setOnClickListener { finish() }
    }
}
