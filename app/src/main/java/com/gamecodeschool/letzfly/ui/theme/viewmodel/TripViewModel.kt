package com.gamecodeschool.letzfly.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamecodeschool.letzfly.data.LetzFlyDatabase
import com.gamecodeschool.letzfly.data.Trip
import com.gamecodeschool.letzfly.repository.TripRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TripViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TripRepository

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail

    val userTrips: StateFlow<List<Trip>> = _currentUserEmail
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList())
            else repository.getTripsForUser(email)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        val tripDao = LetzFlyDatabase.getDatabase(application).tripDao()
        repository = TripRepository(tripDao)

        // AUTO-SEED GOOGLE PLAY REVIEW TEST ACCOUNT
        // This ensures testuser@test.com is recognized immediately upon app launch
        val context = application.applicationContext
        val testPrefs = context.getSharedPreferences("user_prefs_testuser@test.com", Context.MODE_PRIVATE)
        if (!testPrefs.contains("user_full_name")) {
            testPrefs.edit()
                .putString("user_full_name", "Demo User Account")
                .putString("member_since", "January 01, 2026")
                .apply()
        }
    }

    fun setCurrentUser(email: String) {
        _currentUserEmail.value = email.lowercase().trim()
    }

    /**
     * Checks if a user account already exists across SharedPreferences & memory.
     */
    fun doesUserExist(email: String): Boolean {
        val cleanEmail = email.lowercase().trim()
        if (cleanEmail.isBlank()) return false
        val context = getApplication<Application>().applicationContext
        val prefs = context.getSharedPreferences("user_prefs_$cleanEmail", Context.MODE_PRIVATE)
        // Check if full name or member since date was previously saved for this email
        return prefs.contains("user_full_name") || prefs.contains("member_since")
    }

    /**
     * Registers a new user, saves their full name, and sets them as the current user.
     */
    fun registerNewUser(email: String, fullName: String) {
        val cleanEmail = email.lowercase().trim()
        val context = getApplication<Application>().applicationContext
        val prefs = context.getSharedPreferences("user_prefs_$cleanEmail", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user_full_name", fullName.trim())
            .putString("member_since", java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date()))
            .apply()
        _currentUserEmail.value = cleanEmail
    }

    fun addTrip(trip: Trip) = viewModelScope.launch {
        repository.insert(trip.copy(userEmail = _currentUserEmail.value))
    }

    fun updateTrip(updatedTrip: Trip) = viewModelScope.launch {
        repository.insert(updatedTrip)
    }

    fun deleteTrip(tripId: String) = viewModelScope.launch {
        repository.delete(tripId)
    }
}