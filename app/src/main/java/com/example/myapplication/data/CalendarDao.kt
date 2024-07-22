package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_table")
    fun getAllCalendar() : Flow<List<Calendar>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendar(vararg calendar: Calendar)

    @Query("UPDATE calendar_table SET text = :newText WHERE _id = :id")
    suspend fun updateCalendarById(id: String, newText: String)

    @Query("SELECT * FROM calendar_table WHERE _id = :id")
    suspend fun getCalendarById(id: String) : Calendar

    @Query("SELECT * FROM calendar_table WHERE date = :date")
    suspend fun getCalendarByDate(date: String) : Calendar

    @Update
    suspend fun updateCalendar(calendar: Calendar)

    @Delete
    suspend fun deleteCalendar(calendar: Calendar) : Int

}