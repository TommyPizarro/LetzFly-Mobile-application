package com.gamecodeschool.letzfly.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamecodeschool.letzfly.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

data class ItineraryItem(val id: String, val day: Int, val time: String, val activity: String)
data class BudgetItem(val id: String, val day: Int, val category: String, val name: String, val amount: Double)
data class PackingItem(val id: String, val name: String, var isChecked: Boolean)
data class NoteItem(val id: String, val title: String, val content: String, val colorHex: Long = 0xFFFFF59D)

val comprehensiveAirportsList = listOf(
    "ATL - Hartsfield-Jackson Atlanta Intl, GA", "LAX - Los Angeles Intl, CA",
    "ORD - Chicago O'Hare Intl, IL", "DFW - Dallas/Fort Worth Intl, TX",
    "DEN - Denver Intl, CO", "JFK - John F. Kennedy Intl, NY",
    "SFO - San Francisco Intl, CA", "SEA - Seattle-Tacoma Intl, WA",
    "LAS - Harry Reid Intl, Las Vegas, NV", "MCO - Orlando Intl, FL",
    "HNL - Daniel K. Inouye Intl, Honolulu, HI", "SLC - Salt Lake City Intl, UT",
    "YYZ - Toronto Pearson Intl, Canada", "LHR - London Heathrow, UK",
    "CDG - Paris Charles de Gaulle, France", "HND - Tokyo Haneda, Japan",
    "NRT - Tokyo Narita, Japan", "ICN - Seoul Incheon Intl, South Korea",
    "SIN - Singapore Changi Airport", "DXB - Dubai Intl, UAE", "SYD - Sydney Kingsford Smith, Australia"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagingTripScreen(
    tripId: String,
    viewModel: TripViewModel,
    onBack: () -> Unit
) {
    val trips by viewModel.userTrips.collectAsState()
    val trip = trips.find { it.id == tripId }

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableIntStateOf(1) }
    val tabs = listOf("Itinerary", "Budget", "Packing", "Notes")

    val totalDays = remember(trip) {
        if (trip != null) calculateTripDays(trip.startDate, trip.endDate) else 1
    }

    var addBudgetEnabled by remember { mutableStateOf(true) }
    var overallBudgetInput by remember { mutableStateOf(trip?.budget?.toString() ?: "1500") }

    var showFlightDialog by remember { mutableStateOf(false) }
    var departingAirport by remember { mutableStateOf("") }
    var depHour by remember { mutableStateOf("08") }
    var depMin by remember { mutableStateOf("00") }
    var depAmPm by remember { mutableStateOf("AM") }

    var arrivingAirport by remember { mutableStateOf("") }
    var arrHour by remember { mutableStateOf("04") }
    var arrMin by remember { mutableStateOf("00") }
    var arrAmPm by remember { mutableStateOf("PM") }

    val itineraryList = remember { mutableStateListOf<ItineraryItem>() }
    val deletedDefaultDays = remember { mutableStateMapOf<Int, MutableSet<String>>() }
    val budgetItems = remember { mutableStateListOf<BudgetItem>() }
    val packingList = remember { mutableStateListOf<PackingItem>() }
    val notesList = remember { mutableStateListOf<NoteItem>() }

    LaunchedEffect(trip) {
        if (trip != null) {
            overallBudgetInput = trip.budget.toString()

            if (trip.itineraryData.isNotBlank() && itineraryList.isEmpty()) {
                trip.itineraryData.split(";;").forEach { row ->
                    val parts = row.split("##")
                    if (parts.size >= 4) {
                        itineraryList.add(ItineraryItem(parts[0], parts[1].toIntOrNull() ?: 1, parts[2], parts[3]))
                    } else if (parts.size >= 3 && parts[0] == "DEL_DEF") {
                        val day = parts[1].toIntOrNull() ?: 1
                        val time = parts[2]
                        deletedDefaultDays.getOrPut(day) { mutableSetOf() }.add(time)
                    }
                }
            }

            if (itineraryList.isEmpty() && trip.notes.contains("Depart:")) {
                val flightParts = trip.notes.split(" | ")
                flightParts.forEach { part ->
                    if (part.startsWith("Depart:")) {
                        val details = part.removePrefix("Depart:").trim()
                        val atIndex = details.lastIndexOf(" at ")
                        if (atIndex != -1) {
                            val port = details.substring(0, atIndex).trim()
                            val time = details.substring(atIndex + 4).trim()
                            itineraryList.add(ItineraryItem(UUID.randomUUID().toString(), 1, time, "Departing Flight: $port"))
                        }
                    } else if (part.startsWith("Arrive:")) {
                        val details = part.removePrefix("Arrive:").trim()
                        val atIndex = details.lastIndexOf(" at ")
                        if (atIndex != -1) {
                            val port = details.substring(0, atIndex).trim()
                            val time = details.substring(atIndex + 4).trim()
                            itineraryList.add(ItineraryItem(UUID.randomUUID().toString(), 1, time, "Arriving Flight: $port"))
                        }
                    }
                }
            }

            if (trip.budgetData.isNotBlank() && budgetItems.isEmpty()) {
                trip.budgetData.split(";;").forEach { row ->
                    val parts = row.split("##")
                    if (parts.size >= 5) {
                        budgetItems.add(BudgetItem(parts[0], parts[1].toIntOrNull() ?: 1, parts[2], parts[3], parts[4].toDoubleOrNull() ?: 0.0))
                    }
                }
            }
            if (trip.packingData.isNotBlank() && packingList.isEmpty()) {
                trip.packingData.split(";;").forEach { row ->
                    val parts = row.split("##")
                    if (parts.size >= 3) {
                        packingList.add(PackingItem(parts[0], parts[1], parts[2].toBoolean()))
                    }
                }
            }
            if (trip.notesData.isNotBlank() && notesList.isEmpty()) {
                trip.notesData.split(";;").forEach { row ->
                    val parts = row.split("##")
                    if (parts.size >= 4) {
                        notesList.add(NoteItem(parts[0], parts[1], parts[2], parts[3].toLongOrNull() ?: 0xFFFFF59D))
                    }
                }
            } else if (notesList.isEmpty() && trip.notes.isNotBlank()) {
                notesList.add(NoteItem(UUID.randomUUID().toString(), "Trip Overview", trip.notes))
            }
        }
    }

    fun saveTripData() {
        if (trip != null) {
            val itinStr = itineraryList.joinToString(";;") { "${it.id}##${it.day}##${it.time}##${it.activity}" }
            val delDefaultsStr = deletedDefaultDays.flatMap { (day, times) -> times.map { time -> "DEL_DEF##$day##$time" } }
                .let { if (it.isNotEmpty()) ";;" + it.joinToString(";;") else "" }
            val finalItinData = itinStr + delDefaultsStr

            val budgStr = budgetItems.joinToString(";;") { "${it.id}##${it.day}##${it.category}##${it.name}##${it.amount}" }
            val packStr = packingList.joinToString(";;") { "${it.id}##${it.name}##${it.isChecked}" }
            val noteStr = notesList.joinToString(";;") { "${it.id}##${it.title}##${it.content}##${it.colorHex}" }

            viewModel.updateTrip(
                trip.copy(
                    budget = overallBudgetInput.toDoubleOrNull() ?: trip.budget,
                    notes = notesList.firstOrNull()?.content ?: trip.notes,
                    itineraryData = finalItinData,
                    budgetData = budgStr,
                    packingData = packStr,
                    notesData = noteStr
                )
            )
        }
    }

    val filteredDepAirports = remember(departingAirport) {
        if (departingAirport.isBlank() || departingAirport.length < 2) emptyList()
        else comprehensiveAirportsList.filter { it.contains(departingAirport, ignoreCase = true) && it != departingAirport }
    }

    val filteredArrAirports = remember(arrivingAirport) {
        if (arrivingAirport.isBlank() || arrivingAirport.length < 2) emptyList()
        else comprehensiveAirportsList.filter { it.contains(arrivingAirport, ignoreCase = true) && it != arrivingAirport }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.destination ?: "Trip Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        saveTripData()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (trip != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(trip.destination, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${trip.startDate} - ${trip.endDate} ($totalDays Days)", style = MaterialTheme.typography.bodyMedium)
                        }

                        val flightInfo = trip.notes.split(" | ").filter { it.contains("Depart:") || it.contains("Arrive:") }
                        if (flightInfo.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            flightInfo.forEach { info ->
                                Text(
                                    text = "✈️ $info",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showFlightDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("✈️ Add / Edit Flight Tickets", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> ItineraryTab(
                        totalDays = totalDays,
                        selectedDay = selectedDay,
                        onSelectDay = { selectedDay = it },
                        itineraryList = itineraryList,
                        deletedDefaultDays = deletedDefaultDays,
                        onDataChanged = { saveTripData() }
                    )
                    1 -> BudgetTab(
                        totalDays = totalDays,
                        selectedDay = selectedDay,
                        onSelectDay = { selectedDay = it },
                        addBudgetEnabled = addBudgetEnabled,
                        onToggleBudget = { addBudgetEnabled = it },
                        overallBudgetInput = overallBudgetInput,
                        onOverallBudgetChange = { overallBudgetInput = it; saveTripData() },
                        budgetItems = budgetItems,
                        onAddExpense = { cat, name, amt ->
                            budgetItems.add(BudgetItem(UUID.randomUUID().toString(), selectedDay, cat, name, amt))
                            saveTripData()
                        },
                        onDeleteExpense = { item ->
                            budgetItems.remove(item)
                            saveTripData()
                        }
                    )
                    2 -> PackingTab(
                        packingList = packingList,
                        onAddItem = { name ->
                            packingList.add(PackingItem(UUID.randomUUID().toString(), name, false))
                            saveTripData()
                        },
                        onDeleteItem = { item ->
                            packingList.remove(item)
                            saveTripData()
                        },
                        onCheckChanged = { saveTripData() }
                    )
                    3 -> NotesTab(
                        notesList = notesList,
                        onAddNote = { title, content, color ->
                            notesList.add(NoteItem(UUID.randomUUID().toString(), title, content, color))
                            saveTripData()
                        },
                        onDeleteNote = { note ->
                            notesList.remove(note)
                            saveTripData()
                        },
                        onEditNote = { note, title, content, color ->
                            val index = notesList.indexOfFirst { it.id == note.id }
                            if (index != -1) {
                                notesList[index] = note.copy(title = title, content = content, colorHex = color)
                                saveTripData()
                            }
                        }
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Trip details could not be loaded.")
                }
            }
        }
    }

    if (showFlightDialog) {
        AlertDialog(
            onDismissRequest = { showFlightDialog = false },
            title = { Text("Add / Edit Flight Tickets") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = departingAirport,
                            onValueChange = { departingAirport = it },
                            label = { Text("Departing Airport") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        if (filteredDepAirports.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                                shadowElevation = 4.dp,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column {
                                    filteredDepAirports.take(3).forEach { airport ->
                                        Text(
                                            text = airport,
                                            modifier = Modifier.fillMaxWidth().clickable { departingAirport = airport }.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Departing Time:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = depHour,
                            onValueChange = { if (it.length <= 2) depHour = it },
                            label = { Text("HH") },
                            modifier = Modifier.width(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text(":", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = depMin,
                            onValueChange = { if (it.length <= 2) depMin = it },
                            label = { Text("MM") },
                            modifier = Modifier.width(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf("AM", "PM").forEach { period ->
                                FilterChip(
                                    selected = depAmPm == period,
                                    onClick = { depAmPm = period },
                                    label = { Text(period, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = arrivingAirport,
                            onValueChange = { arrivingAirport = it },
                            label = { Text("Arriving Airport") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        if (filteredArrAirports.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                                shadowElevation = 4.dp,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column {
                                    filteredArrAirports.take(3).forEach { airport ->
                                        Text(
                                            text = airport,
                                            modifier = Modifier.fillMaxWidth().clickable { arrivingAirport = airport }.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Arriving Time:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = arrHour,
                            onValueChange = { if (it.length <= 2) arrHour = it },
                            label = { Text("HH") },
                            modifier = Modifier.width(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text(":", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = arrMin,
                            onValueChange = { if (it.length <= 2) arrMin = it },
                            label = { Text("MM") },
                            modifier = Modifier.width(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf("AM", "PM").forEach { period ->
                                FilterChip(
                                    selected = arrAmPm == period,
                                    onClick = { arrAmPm = period },
                                    label = { Text(period, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val depFormatted = "${depHour.padStart(2, '0')}:${depMin.padStart(2, '0')} $depAmPm"
                    val arrFormatted = "${arrHour.padStart(2, '0')}:${arrMin.padStart(2, '0')} $arrAmPm"

                    itineraryList.removeAll { it.activity.startsWith("Departing Flight:") || it.activity.startsWith("Arriving Flight:") }

                    if (departingAirport.isNotBlank()) {
                        itineraryList.removeAll { it.day == 1 && it.time.equals(depFormatted, ignoreCase = true) }
                        deletedDefaultDays[1]?.remove(depFormatted)
                        itineraryList.add(ItineraryItem(UUID.randomUUID().toString(), 1, depFormatted, "Departing Flight: $departingAirport"))
                    }
                    if (arrivingAirport.isNotBlank()) {
                        itineraryList.removeAll { it.day == 1 && it.time.equals(arrFormatted, ignoreCase = true) }
                        deletedDefaultDays[1]?.remove(arrFormatted)
                        itineraryList.add(ItineraryItem(UUID.randomUUID().toString(), 1, arrFormatted, "Arriving Flight: $arrivingAirport"))
                    }
                    saveTripData()
                    showFlightDialog = false
                }) { Text("Update Itinerary") }
            },
            dismissButton = {
                TextButton(onClick = { showFlightDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==================== ITINERARY TAB ====================
@Composable
fun ItineraryTab(
    totalDays: Int,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    itineraryList: MutableList<ItineraryItem>,
    deletedDefaultDays: MutableMap<Int, MutableSet<String>>,
    onDataChanged: () -> Unit
) {
    var showAddTimeDialog by remember { mutableStateOf(false) }

    var hourInput by remember { mutableStateOf("08") }
    var minuteInput by remember { mutableStateOf("00") }
    var amPmInput by remember { mutableStateOf("AM") }
    var activityInput by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    val currentDayItems = remember(selectedDay, itineraryList.toList(), deletedDefaultDays[selectedDay]?.toSet()) {
        val filtered = itineraryList.filter { it.day == selectedDay }
        val defaults = mutableListOf<ItineraryItem>()
        val deletedSet = deletedDefaultDays[selectedDay] ?: emptySet()

        val startUserExists = filtered.any { it.time.equals("12:00 AM", ignoreCase = true) }
        val endUserExists = filtered.any { it.time.equals("11:59 PM", ignoreCase = true) }

        if (!startUserExists && !deletedSet.contains("12:00 AM")) {
            defaults.add(ItineraryItem("start_$selectedDay", selectedDay, "12:00 AM", "Start of Day"))
        }
        if (!endUserExists && !deletedSet.contains("11:59 PM")) {
            defaults.add(ItineraryItem("end_$selectedDay", selectedDay, "11:59 PM", "End of Day"))
        }

        filtered + defaults
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(totalDays) { index ->
                val dayNum = index + 1
                FilterChip(
                    selected = selectedDay == dayNum,
                    onClick = { onSelectDay(dayNum) },
                    label = { Text("Day $dayNum") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Chronological Schedule (Day $selectedDay)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddTimeDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Activity")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val sortedItems = currentDayItems.sortedBy { parseTimeStringToMinutes(it.time) }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedItems, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.activity, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = {
                            if (item.id.startsWith("start_") || item.id.startsWith("end_") || item.activity == "Start of Day" || item.activity == "End of Day") {
                                val set = deletedDefaultDays.getOrPut(selectedDay) { mutableSetOf() }
                                set.add(item.time)
                            } else {
                                itineraryList.remove(item)
                            }
                            onDataChanged()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddTimeDialog) {
        AlertDialog(
            onDismissRequest = { showAddTimeDialog = false },
            title = { Text("Add Itinerary Activity") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Time:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hourInput,
                            onValueChange = { if (it.length <= 2) hourInput = it },
                            label = { Text("HH") },
                            modifier = Modifier.width(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Text(":", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        OutlinedTextField(
                            value = minuteInput,
                            onValueChange = { if (it.length <= 2) minuteInput = it },
                            label = { Text("MM") },
                            modifier = Modifier.width(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf("AM", "PM").forEach { period ->
                                FilterChip(
                                    selected = amPmInput == period,
                                    onClick = { amPmInput = period },
                                    label = { Text(period, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = activityInput,
                        onValueChange = { activityInput = it },
                        label = { Text("Activity Description") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = "${hourInput.padStart(2, '0')}:${minuteInput.padStart(2, '0')} $amPmInput"
                    if (activityInput.isNotBlank()) {
                        itineraryList.removeAll { it.day == selectedDay && it.time.equals(formattedTime, ignoreCase = true) }
                        if (formattedTime.equals("12:00 AM", ignoreCase = true) || formattedTime.equals("11:59 PM", ignoreCase = true)) {
                            deletedDefaultDays[selectedDay]?.remove(formattedTime)
                        }
                        itineraryList.add(ItineraryItem(UUID.randomUUID().toString(), selectedDay, formattedTime, activityInput))
                        onDataChanged()
                        activityInput = ""
                        showAddTimeDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddTimeDialog = false }) { Text("Cancel") } }
        )
    }
}

fun parseTimeStringToMinutes(timeStr: String): Int {
    return try {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val date = formatter.parse(timeStr.trim().uppercase())
        if (date != null) {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.time = date
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } else 0
    } catch (e: Exception) {
        0
    }
}

// ==================== BUDGET TAB ====================
@Composable
fun BudgetTab(
    totalDays: Int,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    addBudgetEnabled: Boolean,
    onToggleBudget: (Boolean) -> Unit,
    overallBudgetInput: String,
    onOverallBudgetChange: (String) -> Unit,
    budgetItems: List<BudgetItem>,
    onAddExpense: (String, String, Double) -> Unit,
    onDeleteExpense: (BudgetItem) -> Unit
) {
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }

    val dailyBudgets = remember { mutableStateMapOf<Int, String>() }
    val currentDayBudgetInput = dailyBudgets[selectedDay] ?: "100"

    val totalSpentOverall = budgetItems.sumOf { it.amount }
    val overallBudget = overallBudgetInput.toDoubleOrNull() ?: 1500.0
    val remainingOverall = overallBudget - totalSpentOverall

    val daySpentTotal = budgetItems.filter { it.day == selectedDay }.sumOf { it.amount }
    val dayBudgetTarget = currentDayBudgetInput.toDoubleOrNull() ?: 100.0
    val remainingDay = dayBudgetTarget - daySpentTotal

    val overallRatio = if (overallBudget > 0) (totalSpentOverall / overallBudget).toFloat().coerceIn(0f, 1f) else 0f
    val dayRatio = if (dayBudgetTarget > 0) (daySpentTotal / dayBudgetTarget).toFloat().coerceIn(0f, 1f) else 0f

    val overallCircleColor by animateColorAsState(
        targetValue = when {
            overallRatio < 0.6f -> Color(0xFF4E9F3D)
            overallRatio < 0.85f -> Color(0xFFFBC02D)
            else -> Color(0xFFD32F2F)
        }, label = "overall_color"
    )

    val dayCircleColor by animateColorAsState(
        targetValue = when {
            dayRatio < 0.6f -> Color(0xFF4E9F3D)
            dayRatio < 0.85f -> Color(0xFFFBC02D)
            else -> Color(0xFFD32F2F)
        }, label = "day_color"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add Budget?")
            Spacer(modifier = Modifier.width(16.dp))
            Switch(checked = addBudgetEnabled, onCheckedChange = onToggleBudget)
        }

        if (addBudgetEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = overallBudgetInput,
                onValueChange = onOverallBudgetChange,
                label = { Text("Overall Budget Target ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = overallCircleColor.copy(alpha = 0.15f)),
                border = BorderStroke(2.dp, overallCircleColor)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Overall Budget: $$overallBudget", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Spent: $$totalSpentOverall | Remaining: $$remainingOverall", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(totalDays) { index ->
                    val dayNum = index + 1
                    FilterChip(
                        selected = selectedDay == dayNum,
                        onClick = { onSelectDay(dayNum) },
                        label = { Text("Day $dayNum") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentDayBudgetInput,
                onValueChange = { dailyBudgets[selectedDay] = it },
                label = { Text("Day $selectedDay Budget Target ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = dayCircleColor.copy(alpha = 0.15f)),
                border = BorderStroke(1.5.dp, dayCircleColor)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Day $selectedDay Target: $$dayBudgetTarget", fontWeight = FontWeight.Bold)
                        Text("Day Spent: $$daySpentTotal | Remaining: $$remainingDay", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Expenses for Day $selectedDay", fontWeight = FontWeight.Bold)
                Button(onClick = { showAddExpenseDialog = true }) {
                    Text("+ Add Expense")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val currentDayExpenses = budgetItems.filter { it.day == selectedDay }

            currentDayExpenses.forEach { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("[${item.category}] ${item.name}", fontWeight = FontWeight.SemiBold)
                            Text("$${item.amount}", color = MaterialTheme.colorScheme.outline)
                        }
                        IconButton(onClick = { onDeleteExpense(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
            Text("Toggle 'Add Budget?' on to track expenses.", color = MaterialTheme.colorScheme.outline)
        }
    }

    if (showAddExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Add Expense (Day $selectedDay)") },
            text = {
                Column {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Food", "Souvenirs", "Transit", "Misc.")) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expenseName,
                        onValueChange = { expenseName = it },
                        label = { Text("Item Name") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Cost ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = expenseAmount.toDoubleOrNull() ?: 0.0
                    if (expenseName.isNotBlank() && amt > 0) {
                        onAddExpense(selectedCategory, expenseName, amt)
                        expenseName = ""
                        expenseAmount = ""
                        showAddExpenseDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddExpenseDialog = false }) { Text("Cancel") } }
        )
    }
}

// ==================== PACKING TAB ====================
@Composable
fun PackingTab(
    packingList: MutableList<PackingItem>,
    onAddItem: (String) -> Unit,
    onDeleteItem: (PackingItem) -> Unit,
    onCheckChanged: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Packing Checklist", fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }) {
                Text("+ Add Item")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(packingList, key = { it.id }) { item ->
                var checked by remember { mutableStateOf(item.isChecked) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                checked = !checked
                                item.isChecked = checked
                                onCheckChanged()
                            },
                            onLongClick = { onDeleteItem(item) }
                        ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                checked = it
                                item.isChecked = it
                                onCheckChanged()
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(item.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text("(Hold to delete)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Packing Item") },
            text = {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (itemName.isNotBlank()) {
                        onAddItem(itemName)
                        itemName = ""
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

// ==================== NOTES TAB ====================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesTab(
    notesList: MutableList<NoteItem>,
    onAddNote: (String, String, Long) -> Unit,
    onDeleteNote: (NoteItem) -> Unit,
    onEditNote: (NoteItem, String, String, Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var contentInputState by remember { mutableStateOf(TextFieldValue("")) }
    var colorInput by remember { mutableStateOf(0xFFFFF59D) }

    var noteToDelete by remember { mutableStateOf<NoteItem?>(null) }
    var noteToEdit by remember { mutableStateOf<NoteItem?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editContentState by remember { mutableStateOf(TextFieldValue("")) }
    var editColor by remember { mutableStateOf(0xFFFFF59D) }

    val availableColors = listOf(0xFFFFF59D, 0xFFF48FB1, 0xFF90CAF9, 0xFFA5D6A7, 0xFFCE93D8)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Sticky Note Board", fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }) {
                Text("+ Add Note")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notesList, key = { it.id }) { note ->
                Card(
                    modifier = Modifier
                        .height(160.dp)
                        .combinedClickable(
                            onClick = {
                                noteToEdit = note
                                editTitle = note.title
                                editContentState = TextFieldValue(note.content)
                                editColor = note.colorHex
                            },
                            onLongClick = { noteToDelete = note }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(note.colorHex))
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Text(note.title, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = parseFormattedNoteText(note.content),
                            color = Color.DarkGray,
                            fontSize = 13.sp,
                            maxLines = 5
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Title") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contentInputState,
                        onValueChange = { contentInputState = it },
                        label = { Text("Content") },
                        modifier = Modifier.height(120.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { contentInputState = applyMarkdownFormatting(contentInputState, "**") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("B", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { contentInputState = applyMarkdownFormatting(contentInputState, "<u>", "</u>") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("U", fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                        }
                        OutlinedButton(
                            onClick = { contentInputState = applyBulletPoint(contentInputState) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("• Bullet")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Color:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableColors.forEach { colorHex ->
                            val isSelected = colorInput == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { colorInput = colorHex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (titleInput.isNotBlank()) {
                        onAddNote(titleInput, contentInputState.text, colorInput)
                        titleInput = ""
                        contentInputState = TextFieldValue("")
                        colorInput = 0xFFFFF59D
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(onClick = {
                    noteToDelete?.let { onDeleteNote(it) }
                    noteToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { noteToDelete = null }) { Text("Cancel") } }
        )
    }

    if (noteToEdit != null) {
        AlertDialog(
            onDismissRequest = { noteToEdit = null },
            title = { Text("Edit Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editContentState,
                        onValueChange = { editContentState = it },
                        label = { Text("Content") },
                        modifier = Modifier.height(120.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { editContentState = applyMarkdownFormatting(editContentState, "**") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("B", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { editContentState = applyMarkdownFormatting(editContentState, "<u>", "</u>") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("U", fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                        }
                        OutlinedButton(
                            onClick = { editContentState = applyBulletPoint(editContentState) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("• Bullet")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Change Color:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableColors.forEach { colorHex ->
                            val isSelected = editColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { editColor = colorHex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    noteToEdit?.let { onEditNote(it, editTitle, editContentState.text, editColor) }
                    noteToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { noteToEdit = null }) { Text("Cancel") }
            }
        )
    }
}

private fun applyMarkdownFormatting(tfv: TextFieldValue, tag: String): TextFieldValue {
    val start = tfv.selection.start
    val end = tfv.selection.end
    val text = tfv.text

    if (start != end) {
        val selectedText = text.substring(start, end)
        val newText = text.replaceRange(start, end, "$tag$selectedText$tag")
        return TextFieldValue(text = newText, selection = TextRange(start + tag.length, end + tag.length))
    } else {
        val newText = text.insert(start, "$tag$tag")
        return TextFieldValue(text = newText, selection = TextRange(start + tag.length))
    }
}

private fun applyMarkdownFormatting(tfv: TextFieldValue, openTag: String, closeTag: String): TextFieldValue {
    val start = tfv.selection.start
    val end = tfv.selection.end
    val text = tfv.text

    if (start != end) {
        val selectedText = text.substring(start, end)
        val newText = text.replaceRange(start, end, "$openTag$selectedText$closeTag")
        return TextFieldValue(text = newText, selection = TextRange(start + openTag.length, end + openTag.length))
    } else {
        val newText = text.insert(start, "$openTag$closeTag")
        return TextFieldValue(text = newText, selection = TextRange(start + openTag.length))
    }
}

private fun applyBulletPoint(tfv: TextFieldValue): TextFieldValue {
    val text = tfv.text
    val cursor = tfv.selection.start
    val lineStart = text.lastIndexOf('\n', cursor - 1).let { if (it == -1) 0 else it + 1 }
    val newText = text.insert(lineStart, "• ")
    return TextFieldValue(text = newText, selection = TextRange(cursor + 2))
}

private fun String.insert(index: Int, str: String): String {
    return this.substring(0, index) + str + this.substring(index)
}

/**
 * Parses markdown tags (**bold**, <u>underline</u>) into rendered AnnotatedString with real SpanStyles so bolding and underlining display visually.
 */
@Composable
fun parseFormattedNoteText(content: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val regex = "(\\*\\*.*?\\*\\*|<u>.*?</u>)".toRegex()
        val matchResults = regex.findAll(content)

        for (match in matchResults) {
            if (match.range.first > currentIndex) {
                append(content.substring(currentIndex, match.range.first))
            }
            val matchedString = match.value
            if (matchedString.startsWith("**") && matchedString.endsWith("**")) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(matchedString.substring(2, matchedString.length - 2))
                pop()
            } else if (matchedString.startsWith("<u>") && matchedString.endsWith("</u>")) {
                pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                append(matchedString.substring(3, matchedString.length - 6))
                pop()
            }
            currentIndex = match.range.last + 1
        }
        if (currentIndex < content.length) {
            append(content.substring(currentIndex))
        }
    }
}

fun calculateTripDays(startStr: String, endStr: String): Int {
    return try {
        val formatter = SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val start = formatter.parse(startStr)
        val end = formatter.parse(endStr)
        if (start != null && end != null) {
            val diff = end.time - start.time
            val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
            if (days > 0) days else 1
        } else 1
    } catch (e: Exception) {
        5
    }
}