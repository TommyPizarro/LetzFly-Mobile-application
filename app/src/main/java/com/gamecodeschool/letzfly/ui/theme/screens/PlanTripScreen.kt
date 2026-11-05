package com.gamecodeschool.letzfly.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamecodeschool.letzfly.data.Trip
import com.gamecodeschool.letzfly.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

val usStatesAndCapitals = mapOf(
    "Alabama" to "Montgomery", "Alaska" to "Juneau", "Arizona" to "Phoenix", "Arkansas" to "Little Rock",
    "California" to "Sacramento", "Colorado" to "Denver", "Connecticut" to "Hartford", "Delaware" to "Dover",
    "Florida" to "Tallahassee", "Georgia" to "Atlanta", "Hawaii" to "Honolulu", "Idaho" to "Boise",
    "Illinois" to "Springfield", "Indiana" to "Indianapolis", "Iowa" to "Des Moines", "Kansas" to "Topeka",
    "Kentucky" to "Frankfort", "Louisiana" to "Baton Rouge", "Maine" to "Augusta", "Maryland" to "Annapolis",
    "Massachusetts" to "Boston", "Michigan" to "Lansing", "Minnesota" to "Saint Paul", "Mississippi" to "Jackson",
    "Missouri" to "Jefferson City", "Montana" to "Helena", "Nebraska" to "Lincoln", "Nevada" to "Carson City",
    "New Hampshire" to "Concord", "New Jersey" to "Trenton", "New Mexico" to "Santa Fe", "New York" to "Albany",
    "North Carolina" to "Raleigh", "North Dakota" to "Bismarck", "Ohio" to "Columbus", "Oklahoma" to "Oklahoma City",
    "Oregon" to "Salem", "Pennsylvania" to "Harrisburg", "Rhode Island" to "Providence", "South Carolina" to "Columbia",
    "South Dakota" to "Pierre", "Tennessee" to "Nashville", "Texas" to "Austin", "Utah" to "Salt Lake City",
    "Vermont" to "Montpelier", "Virginia" to "Richmond", "Washington" to "Olympia", "West Virginia" to "Charleston",
    "Wisconsin" to "Madison", "Wyoming" to "Cheyenne", "Washington D.C." to "Washington D.C."
)

val popularUSCities = listOf(
    "New York, NY, USA", "Los Angeles, CA, USA", "Chicago, IL, USA", "Houston, TX, USA",
    "Phoenix, AZ, USA", "Philadelphia, PA, USA", "San Antonio, TX, USA", "San Diego, CA, USA",
    "Dallas, TX, USA", "Austin, TX, USA", "San Jose, CA, USA", "Fort Worth, TX, USA",
    "Columbus, OH, USA", "Charlotte, NC, USA", "Indianapolis, IN, USA", "San Francisco, CA, USA",
    "Seattle, WA, USA", "Denver, CO, USA", "Greeley, CO, USA", "Washington, D.C., USA",
    "Boston, MA, USA", "Nashville, TN, USA", "Las Vegas, NV, USA", "Honolulu, HI, USA",
    "Orlando, FL, USA", "Miami, FL, USA", "Atlanta, GA, USA", "New Orleans, LA, USA"
)

val popularInternationalHubs = listOf(
    "Tokyo, Japan", "Kyoto, Japan", "Osaka, Japan", "Nara, Japan", "Hiroshima, Japan",
    "Seoul, South Korea", "Busan, South Korea", "Jeju Island, South Korea",
    "Beijing, China", "Shanghai, China", "Hong Kong", "Taipei, Taiwan",
    "Bangkok, Thailand", "Phuket, Thailand", "Chiang Mai, Thailand", "Singapore",
    "Bali, Indonesia", "Jakarta, Indonesia", "Kuala Lumpur, Malaysia",
    "Hanoi, Vietnam", "Ho Chi Minh City, Vietnam", "Manila, Philippines", "Cebu, Philippines",
    "New Delhi, India", "Mumbai, India",
    "London, UK", "Edinburgh, UK", "Paris, France", "Nice, France",
    "Rome, Italy", "Florence, Italy", "Venice, Italy", "Milan, Italy",
    "Barcelona, Spain", "Madrid, Spain", "Amsterdam, Netherlands",
    "Berlin, Germany", "Munich, Germany", "Frankfurt, Germany",
    "Vienna, Austria", "Zurich, Switzerland", "Geneva, Switzerland",
    "Athens, Greece", "Santorini, Greece", "Lisbon, Portugal",
    "Copenhagen, Denmark", "Stockholm, Sweden", "Oslo, Norway", "Reykjavik, Iceland",
    "Toronto, Canada", "Vancouver, Canada", "Montreal, Canada",
    "Mexico City, Mexico", "Cancun, Mexico", "Cabo San Lucas, Mexico",
    "Costa Rica", "Panama City, Panama", "Bogota, Colombia", "Lima, Peru", "Cusco, Peru",
    "Santiago, Chile", "Buenos Aires, Argentina", "Rio de Janeiro, Brazil", "Sao Paulo, Brazil",
    "Dubai, UAE", "Doha, Qatar", "Tel Aviv, Israel", "Cairo, Egypt", "Marrakech, Morocco",
    "Cape Town, South Africa", "Sydney, Australia", "Melbourne, Australia", "Auckland, New Zealand"
)

val comprehensiveAirports = listOf(
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

val carMakesAndModels = listOf(
    "Toyota Corolla", "Toyota Camry", "Toyota RAV4", "Toyota Highlander", "Toyota Tacoma",
    "Honda Civic", "Honda Accord", "Honda CR-V", "Ford F-150", "Ford Mustang",
    "Chevrolet Silverado", "Tesla Model 3", "Tesla Model Y", "Subaru Outback", "Jeep Wrangler", "RAM 1500"
)

val boatTypesAndModels = listOf(
    "Yamaha WaveRunner (Jet Ski)", "Sea-Doo GTX 300 (PWC)", "Boston Whaler 210 Montauk",
    "Sea Ray Sundancer 320", "MasterCraft X24 (Wakeboard Boat)", "Bennington 23 QX (Pontoon)",
    "Catalina 30 (Sailboat)", "Lagoon 42 (Sailing Catamaran)", "Royal Caribbean Cruise Liner"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTripScreen(
    viewModel: TripViewModel,
    onTripCreated: () -> Unit,
    onBack: () -> Unit
) {
    var tripName by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var travelType by remember { mutableStateOf("Flight") }

    val keyboardController = LocalSoftwareKeyboardController.current

    var startMillis by remember { mutableStateOf<Long?>(null) }
    var endMillis by remember { mutableStateOf<Long?>(null) }
    var dateError by remember { mutableStateOf("") }

    var addTicket by remember { mutableStateOf(false) }
    var departingAirport by remember { mutableStateOf("") }
    var depHour by remember { mutableStateOf("08") }
    var depMin by remember { mutableStateOf("00") }
    var depAmPm by remember { mutableStateOf("AM") }

    var arrivingAirport by remember { mutableStateOf("") }
    var arrHour by remember { mutableStateOf("12") }
    var arrMin by remember { mutableStateOf("00") }
    var arrAmPm by remember { mutableStateOf("PM") }

    var addVehicle by remember { mutableStateOf(false) }
    var vehicleInput by remember { mutableStateOf("") }
    var driveStartTime by remember { mutableStateOf("09:00 AM") }

    var addBoat by remember { mutableStateOf(false) }
    var boatInput by remember { mutableStateOf("") }
    var sailStartTime by remember { mutableStateOf("10:00 AM") }

    var showStartCalendar by remember { mutableStateOf(false) }
    var showEndCalendar by remember { mutableStateOf(false) }

    val filteredDestinations = remember(destination) {
        if (destination.isBlank() || destination.length < 2) emptyList()
        else {
            val query = destination.trim().lowercase()
            val matches = mutableListOf<String>()

            matches.addAll(popularInternationalHubs.filter { it.lowercase().contains(query) })
            matches.addAll(popularUSCities.filter { it.lowercase().contains(query) })

            usStatesAndCapitals.forEach { (state, capital) ->
                if (state.lowercase().contains(query) || capital.lowercase().contains(query)) {
                    matches.add("$capital, $state, USA")
                }
            }

            if (matches.size == 1 && matches[0].equals(destination.trim(), ignoreCase = true)) {
                emptyList()
            } else {
                matches.distinct().take(6)
            }
        }
    }

    val filteredDepartingAirports = remember(departingAirport) {
        if (departingAirport.isBlank() || departingAirport.length < 2) emptyList()
        else {
            val list = comprehensiveAirports.filter { it.contains(departingAirport, ignoreCase = true) }
            if (list.size == 1 && list[0].equals(departingAirport.trim(), ignoreCase = true)) emptyList() else list.take(5)
        }
    }

    val filteredArrivingAirports = remember(arrivingAirport) {
        if (arrivingAirport.isBlank() || arrivingAirport.length < 2) emptyList()
        else {
            val list = comprehensiveAirports.filter { it.contains(arrivingAirport, ignoreCase = true) }
            if (list.size == 1 && list[0].equals(arrivingAirport.trim(), ignoreCase = true)) emptyList() else list.take(5)
        }
    }

    val filteredCars = remember(vehicleInput) {
        if (vehicleInput.isBlank() || vehicleInput.length < 2) emptyList()
        else carMakesAndModels.filter { it.contains(vehicleInput, ignoreCase = true) && !it.equals(vehicleInput, ignoreCase = true) }.take(5)
    }

    val filteredBoats = remember(boatInput) {
        if (boatInput.isBlank() || boatInput.length < 2) emptyList()
        else boatTypesAndModels.filter { it.contains(boatInput, ignoreCase = true) && !it.equals(boatInput, ignoreCase = true) }.take(5)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan New Trip", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = { Text("Trip Name") },
                trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination (US Cities or Global)") },
                    trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                if (filteredDestinations.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column {
                            filteredDestinations.forEach { match ->
                                Text(
                                    text = match,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            destination = match
                                            keyboardController?.hide()
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date") },
                    trailingIcon = {
                        IconButton(onClick = { showStartCalendar = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Start Date")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date") },
                    trailingIcon = {
                        IconButton(onClick = { showEndCalendar = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "End Date")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            if (dateError.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(dateError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Travel Type", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Flight", "Drive", "Sail").forEach { type ->
                    FilterChip(
                        selected = travelType == type,
                        onClick = { travelType = type },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (travelType) {
                "Flight" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Add Flight & Ticket Details?")
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(checked = addTicket, onCheckedChange = { addTicket = it })
                    }
                    if (addTicket) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = departingAirport,
                                onValueChange = { departingAirport = it },
                                label = { Text("Departing Airport") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            if (filteredDepartingAirports.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                                    shadowElevation = 4.dp,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column {
                                        filteredDepartingAirports.forEach { airport ->
                                            Text(
                                                text = airport,
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    departingAirport = airport
                                                    keyboardController?.hide()
                                                }.padding(12.dp)
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
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            if (filteredArrivingAirports.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                                    shadowElevation = 4.dp,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column {
                                        filteredArrivingAirports.forEach { airport ->
                                            Text(
                                                text = airport,
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    arrivingAirport = airport
                                                    keyboardController?.hide()
                                                }.padding(12.dp)
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
                }
                "Drive" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Add Vehicle?")
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(checked = addVehicle, onCheckedChange = { addVehicle = it })
                    }
                    if (addVehicle) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = vehicleInput,
                                onValueChange = { vehicleInput = it },
                                label = { Text("Vehicle Make & Model") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            if (filteredCars.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                                    shadowElevation = 4.dp,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column {
                                        filteredCars.forEach { car ->
                                            Text(
                                                text = car,
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    vehicleInput = car
                                                    keyboardController?.hide()
                                                }.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = driveStartTime,
                            onValueChange = { driveStartTime = it },
                            label = { Text("Drive Start Time (e.g., 09:00 AM)") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                "Sail" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Add Boat?")
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(checked = addBoat, onCheckedChange = { addBoat = it })
                    }
                    if (addBoat) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = boatInput,
                                onValueChange = { boatInput = it },
                                label = { Text("Boat Type / Model") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            if (filteredBoats.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                                    shadowElevation = 4.dp,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column {
                                        filteredBoats.forEach { boat ->
                                            Text(
                                                text = boat,
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    boatInput = boat
                                                    keyboardController?.hide()
                                                }.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = sailStartTime,
                            onValueChange = { sailStartTime = it },
                            label = { Text("Departure Start Time (e.g., 10:00 AM)") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val isFormValid = destination.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank() && dateError.isBlank()

            Button(
                onClick = {
                    if (isFormValid) {
                        val depFormatted = "${depHour.padStart(2, '0')}:${depMin.padStart(2, '0')} $depAmPm"
                        val arrFormatted = "${arrHour.padStart(2, '0')}:${arrMin.padStart(2, '0')} $arrAmPm"

                        val fullNotes = buildString {
                            if (tripName.isNotBlank()) append("$tripName | ")
                            append("Mode: $travelType")
                            if (addTicket && departingAirport.isNotBlank()) append(" | Depart: $departingAirport at $depFormatted")
                            if (addTicket && arrivingAirport.isNotBlank()) append(" | Arrive: $arrivingAirport at $arrFormatted")
                            if (addVehicle && vehicleInput.isNotBlank()) append(" | Vehicle: $vehicleInput (Start: $driveStartTime)")
                            if (addBoat && boatInput.isNotBlank()) append(" | Boat: $boatInput (Start: $sailStartTime)")
                            if (notes.isNotBlank()) append(" | $notes")
                        }

                        // Automatically seed initial itinerary data for Day 1 with flights if enabled
                        val initialItinerary = buildString {
                            val items = mutableListOf<String>()
                            if (addTicket && departingAirport.isNotBlank()) {
                                items.add("${UUID.randomUUID()}##1##$depFormatted##Departing Flight: $departingAirport")
                            }
                            if (addTicket && arrivingAirport.isNotBlank()) {
                                items.add("${UUID.randomUUID()}##1##$arrFormatted##Arriving Flight: $arrivingAirport")
                            }
                            append(items.joinToString(";;"))
                        }

                        viewModel.addTrip(
                            Trip(
                                id = UUID.randomUUID().toString(),
                                userEmail = viewModel.currentUserEmail.value,
                                destination = destination,
                                startDate = startDate,
                                endDate = endDate,
                                budget = 1500.0,
                                notes = fullNotes,
                                itineraryData = initialItinerary
                            )
                        )
                        onTripCreated()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Create Trip", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showStartCalendar) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        startMillis = millis
                        startDate = convertMillisToAcronymDate(millis)
                        if (endMillis != null && millis > endMillis!!) {
                            dateError = "End date cannot be before start date."
                        } else {
                            dateError = ""
                        }
                    }
                    showStartCalendar = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartCalendar = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }

    if (showEndCalendar) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        if (startMillis != null && millis < startMillis!!) {
                            dateError = "End date cannot be before start date."
                        } else {
                            endMillis = millis
                            endDate = convertMillisToAcronymDate(millis)
                            dateError = ""
                        }
                    }
                    showEndCalendar = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndCalendar = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }
}

fun convertMillisToAcronymDate(millis: Long): String {
    val formatter = SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}