package com.gamecodeschool.letzfly.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey
    val id: String,
    val userEmail: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val budget: Double,
    val notes: String = "",
    val itineraryData: String = "",
    val budgetData: String = "",
    val packingData: String = "",
    val notesData: String = ""
)