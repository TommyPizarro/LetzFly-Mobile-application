package com.gamecodeschool.letzfly.repository

import com.gamecodeschool.letzfly.data.Trip
import com.gamecodeschool.letzfly.data.TripDao
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {
    fun getTripsForUser(email: String): Flow<List<Trip>> = tripDao.getTripsForUser(email)

    suspend fun insert(trip: Trip) {
        tripDao.insertTrip(trip)
    }

    suspend fun delete(tripId: String) {
        tripDao.deleteTrip(tripId)
    }
}