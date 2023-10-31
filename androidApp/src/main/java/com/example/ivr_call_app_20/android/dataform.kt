package com.example.ivr_calling_app.android

data class patientdata(
    val engagementid : String,
    val patientid : String,
    val patientname : String,
    val patientcno : String,
    val operationtype : String,
    val language : String,
    val duedate : String,

    val calledon : String = "",
    val day7 : String = "",
    val day6 : String = "",
    val day5 : String = "",
    val day4 : String = "",
    val day3 : String = "",
    val day2 : String = "",
    val day1 : String = "",
)

