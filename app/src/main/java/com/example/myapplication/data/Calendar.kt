package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date

@Entity(tableName = "calendar_table")
data class Calendar(
    @PrimaryKey
    val _id: String,

    val date: String?,

    val text: String?
)
