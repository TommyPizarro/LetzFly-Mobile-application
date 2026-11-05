package com.gamecodeschool.letzfly.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.gamecodeschool.letzfly.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class BucketItem(val id: String, val destinationName: String, val expectedDateMillis: Long? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: TripViewModel,
    onDeleteConfirmed: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val userTrips by viewModel.userTrips.collectAsState()

    val prefs = remember(currentUserEmail) {
        context.getSharedPreferences("user_prefs_${currentUserEmail.ifBlank { "guest" }}", Context.MODE_PRIVATE)
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    var savedImageUriStr by remember { mutableStateOf(prefs.getString("profile_image_uri", null)) }

    var fullNameInput by remember {
        mutableStateOf(
            prefs.getString("user_full_name", null) ?: currentUserEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        )
    }

    val bucketList = remember { mutableStateListOf<BucketItem>() }
    var showAddBucketDialog by remember { mutableStateOf(false) }
    var newDestinationInput by remember { mutableStateOf("") }
    var expectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showCalendarPicker by remember { mutableStateOf(false) }

    val popularHubs = popularInternationalHubs + popularUSCities
    val filteredBucketSuggestions = remember(newDestinationInput) {
        if (newDestinationInput.isBlank() || newDestinationInput.length < 2) emptyList()
        else popularHubs.filter { it.contains(newDestinationInput, ignoreCase = true) && !it.equals(newDestinationInput, ignoreCase = true) }.take(4)
    }

    LaunchedEffect(currentUserEmail) {
        bucketList.clear()
        val savedBucketStr = prefs.getString("user_bucket_list", "")
        if (!savedBucketStr.isNullOrBlank()) {
            savedBucketStr.split(";;").forEach { row ->
                val parts = row.split("##")
                if (parts.size >= 2) {
                    val id = parts[0]
                    val name = parts[1]
                    val date = if (parts.size >= 3 && parts[2] != "null") parts[2].toLongOrNull() else null
                    bucketList.add(BucketItem(id, name, date))
                }
            }
        }
    }

    fun saveBucketList() {
        val serialized = bucketList.joinToString(";;") { "${it.id}##${it.destinationName}##${it.expectedDateMillis}" }
        prefs.edit().putString("user_bucket_list", serialized).apply()
    }

    var showFirstDeleteDialog by remember { mutableStateOf(false) }
    var showSecondDeleteDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            savedImageUriStr = uri.toString()
            prefs.edit().putString("profile_image_uri", uri.toString()).apply()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) photoPickerLauncher.launch(arrayOf("image/*"))
    }

    val memberSinceDate = remember {
        val savedDate = prefs.getString("member_since", null)
        if (savedDate != null) savedDate
        else {
            val formatted = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
            prefs.edit().putString("member_since", formatted).apply()
            formatted
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Bucket List", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        prefs.edit().putString("user_full_name", fullNameInput).apply()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            permissionLauncher.launch(permission)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!savedImageUriStr.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(savedImageUriStr)),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Avatar Placeholder",
                            modifier = Modifier.size(90.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap photo to change profile picture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = fullNameInput,
                    onValueChange = {
                        fullNameInput = it
                        prefs.edit().putString("user_full_name", it).apply()
                    },
                    label = { Text("Full Name (Editable)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentUserEmail.ifBlank { "user@letzfly.com" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = memberSinceDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Member Since") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Travel Bucket List", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = { showAddBucketDialog = true }) {
                        Text("🪣", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Place")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (bucketList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No dream destinations added yet. Tap 'Add Place' to start your bucket list!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(bucketList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪣", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.destinationName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                }
                                if (item.expectedDateMillis != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val diffMillis = item.expectedDateMillis - System.currentTimeMillis()
                                    val daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
                                    val countdownText = if (daysLeft >= 0) "$daysLeft days until target" else "${-daysLeft} days overdue"
                                    Text(
                                        text = "Target Date: ${convertMillisToAcronymDate(item.expectedDateMillis)} ($countdownText)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = {
                                bucketList.remove(item)
                                saveBucketList()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { showFirstDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Profile", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddBucketDialog) {
        AlertDialog(
            onDismissRequest = { showAddBucketDialog = false },
            title = { Text("Add Dream Destination") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newDestinationInput,
                            onValueChange = { newDestinationInput = it },
                            label = { Text("Destination (e.g., Tokyo, Japan)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        if (filteredBucketSuggestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                                shadowElevation = 4.dp,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column {
                                    filteredBucketSuggestions.forEach { suggestion ->
                                        Text(
                                            text = suggestion,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    newDestinationInput = suggestion
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

                    OutlinedButton(
                        onClick = { showCalendarPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (expectedDateMillis != null) "Target: ${convertMillisToAcronymDate(expectedDateMillis!!)}" else "Set Expected Date (Optional)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newDestinationInput.isNotBlank()) {
                        bucketList.add(BucketItem(UUID.randomUUID().toString(), newDestinationInput.trim(), expectedDateMillis))
                        saveBucketList()
                        newDestinationInput = ""
                        expectedDateMillis = null
                        showAddBucketDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    newDestinationInput = ""
                    expectedDateMillis = null
                    showAddBucketDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showCalendarPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCalendarPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        expectedDateMillis = millis
                    }
                    showCalendarPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showCalendarPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }

    if (showFirstDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showFirstDeleteDialog = false },
            title = { Text("Delete Profile?") },
            text = { Text("This action will remove your account and all saved trips from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    showFirstDeleteDialog = false
                    showSecondDeleteDialog = true
                }) { Text("Proceed", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showFirstDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showSecondDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showSecondDeleteDialog = false },
            title = { Text("Are you absolutely sure?") },
            text = { Text("This cannot be undone. All data associated with $currentUserEmail will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    // 1. Wipe all SharedPreferences data for this user account
                    prefs.edit().clear().apply()

                    // 2. Delete all trips created by this user from the database/repository
                    userTrips.forEach { trip ->
                        viewModel.deleteTrip(trip.id)
                    }

                    // 3. Clear active session & trigger navigation callback back to login/auth
                    viewModel.setCurrentUser("")

                    showSecondDeleteDialog = false
                    onDeleteConfirmed()
                }) { Text("Permanently Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showSecondDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}