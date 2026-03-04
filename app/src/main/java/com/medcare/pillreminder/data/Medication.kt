package com.medcare.pillreminder.data

data class Medication(
    val id: String = System.currentTimeMillis().toString(),
    var name: String = "",
    var dosage: String = "",
    var hour: Int = 8,
    var minute: Int = 0,
    var ampm: Int = 0,       // 0=오전, 1=오후
    var days: List<Int> = listOf(0,1,2,3,4,5,6), // 0=일 ~ 6=토
    var stock: Int? = null,
    var isChecked: Boolean = false
)
