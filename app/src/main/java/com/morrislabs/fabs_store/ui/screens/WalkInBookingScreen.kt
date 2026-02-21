package com.morrislabs.fabs_store.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.data.model.ReservationDTO
import com.morrislabs.fabs_store.data.model.ReservationCreatedBy
import com.morrislabs.fabs_store.data.model.ReservationStatus
import com.morrislabs.fabs_store.data.model.TimeSlot
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.data.model.toDisplayName
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun WalkInBookingScreen(
    storeId: String,
    storeViewModel: StoreViewModel,
    onNavigateBack: () -> Unit,
    onBookingCreated: () -> Unit
) {
    val walkInServicesState by storeViewModel.walkInServicesState.collectAsState()
    val walkInExpertsState by storeViewModel.walkInExpertsState.collectAsState()
    val walkInAvailableSlotsByExpertState by storeViewModel.walkInAvailableSlotsByExpertState.collectAsState()
    val bookingActionState by storeViewModel.walkInBookingActionState.collectAsState()
    val customerLookupState by storeViewModel.walkInCustomerLookupState.collectAsState()

    var customerEmail by remember { mutableStateOf("") }
    var serviceSearch by remember { mutableStateOf("") }
    var selectedServiceIds by remember { mutableStateOf(setOf<String>()) }
    var selectedExpertsByService by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    var selectedDurationMinutes by remember { mutableStateOf<Int?>(null) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedTimeSlotsByPair by remember { mutableStateOf(mapOf<String, TimeSlot>()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showOtherDurationDialog by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val displayDate = selectedDateMillis?.let { dateFormat.format(Date(it)) } ?: "Select a date"
    val services = (walkInServicesState as? StoreViewModel.LoadingState.Success<List<TypeOfServiceDTO>>)?.data.orEmpty()
    val experts = (walkInExpertsState as? StoreViewModel.LoadingState.Success<List<ExpertDTO>>)?.data.orEmpty()
    val expertsById = remember(experts) { experts.associateBy { it.id } }
    val selectedServices = remember(services, selectedServiceIds) { services.filter { selectedServiceIds.contains(it.id) } }
    val totalPrice = selectedServices.sumOf { service -> service.price * selectedExpertsByService[service.id].orEmpty().size }
    val selectedPairs = selectedServices.flatMap { service ->
        selectedExpertsByService[service.id].orEmpty().map { expertId -> pairKey(service.id, expertId) }
    }
    val isEmailValid = customerEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(customerEmail.trim()).matches()
    val resolvedReservationUserId = when (val state = customerLookupState) {
        is StoreViewModel.WalkInCustomerLookupState.Found -> state.user.id
        StoreViewModel.WalkInCustomerLookupState.NotFound -> storeId
        else -> null
    }

    val isFormValid = isEmailValid &&
        selectedDateMillis != null &&
        selectedDurationMinutes != null &&
        selectedServices.isNotEmpty() &&
        selectedServices.all { selectedExpertsByService[it.id].orEmpty().isNotEmpty() } &&
        selectedPairs.all { selectedTimeSlotsByPair[it] != null } &&
        resolvedReservationUserId != null

    LaunchedEffect(storeId) {
        if (storeId.isBlank()) return@LaunchedEffect
        storeViewModel.fetchWalkInExperts(storeId)
    }

    LaunchedEffect(storeId, serviceSearch) {
        if (storeId.isBlank()) return@LaunchedEffect
        delay(300)
        storeViewModel.fetchWalkInServices(storeId, serviceSearch.trim().ifBlank { null })
    }

    LaunchedEffect(customerEmail) {
        val normalizedEmail = customerEmail.trim()
        if (normalizedEmail.isBlank()) {
            storeViewModel.resetWalkInCustomerLookup()
            return@LaunchedEffect
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            storeViewModel.resetWalkInCustomerLookup()
            return@LaunchedEffect
        }
        delay(300)
        storeViewModel.lookupWalkInCustomerByEmail(normalizedEmail)
    }

    LaunchedEffect(selectedDateMillis, selectedDurationMinutes, selectedExpertsByService) {
        val dateMillis = selectedDateMillis
        val duration = selectedDurationMinutes
        if (dateMillis == null || duration == null) {
            storeViewModel.clearWalkInAvailableSlots()
            selectedTimeSlotsByPair = emptyMap()
            return@LaunchedEffect
        }

        val bookingDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString()

        val selectedExpertIds = selectedExpertsByService.values.flatten().toSet()
        if (selectedExpertIds.isEmpty()) {
            storeViewModel.clearWalkInAvailableSlots()
            selectedTimeSlotsByPair = emptyMap()
            return@LaunchedEffect
        }

        storeViewModel.clearWalkInAvailableSlots()
        selectedTimeSlotsByPair = selectedTimeSlotsByPair.filterKeys { key ->
            selectedPairs.contains(key)
        }
        selectedExpertIds.forEach { expertId ->
            storeViewModel.fetchWalkInAvailableSlots(expertId, bookingDate, duration)
        }
    }

    LaunchedEffect(bookingActionState) {
        if (bookingActionState is StoreViewModel.WalkInBookingActionState.Success) {
            storeViewModel.resetWalkInBookingActionState()
            onBookingCreated()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Create Walk-in Booking", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        localValidationError = null
                        if (!isFormValid) {
                            localValidationError = "Fill all required booking details"
                            return@Button
                        }
                        val userId = resolvedReservationUserId
                        if (userId.isNullOrBlank()) {
                            localValidationError = "Customer email lookup is still in progress"
                            return@Button
                        }
                        val bookingDate = Instant.ofEpochMilli(selectedDateMillis!!)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val payloads = selectedServices.flatMap { service ->
                            selectedExpertsByService[service.id].orEmpty().map { expertId ->
                                val expertName = expertsById[expertId]?.name.orEmpty()
                                val selectedSlot = selectedTimeSlotsByPair[pairKey(service.id, expertId)]
                                if (selectedSlot == null) {
                                    localValidationError = "Select a time slot for each service-expert pair"
                                    return@map null
                                }
                                ReservationDTO(
                                    userId = userId,
                                    name = "Appointment for ${service.subCategory.toDisplayName()}",
                                    price = service.price.toDouble(),
                                    reservationDate = bookingDate.toString(),
                                    startTime = selectedSlot.startTime,
                                    endTime = selectedSlot.endTime,
                                    expert = expertName.ifBlank { "Expert" },
                                    status = ReservationStatus.BOOKED_ACCEPTED,
                                    createdBy = ReservationCreatedBy.STORE,
                                    store = storeId,
                                    typeOfService = service.id,
                                    reservationExpert = expertId
                                )
                            }.filterNotNull()
                        }
                        if (payloads.isEmpty()) {
                            localValidationError = "No valid reservation payload generated"
                            return@Button
                        }
                        storeViewModel.createWalkInReservations(payloads)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    enabled = bookingActionState !is StoreViewModel.WalkInBookingActionState.Loading
                ) {
                    if (bookingActionState is StoreViewModel.WalkInBookingActionState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Booking")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showOtherDurationDialog) {
            AlertDialog(
                onDismissRequest = { showOtherDurationDialog = false },
                title = { Text("Select Duration") },
                text = {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        extendedDurations.forEach { minutes ->
                            FilterChip(
                                selected = selectedDurationMinutes == minutes,
                                onClick = {
                                    selectedDurationMinutes = minutes
                                    showOtherDurationDialog = false
                                },
                                label = { Text(formatDuration(minutes)) }
                            )
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showOtherDurationDialog = false }) { Text("Close") } }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "CUSTOMER DETAILS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.OutlinedTextField(
                value = customerEmail,
                onValueChange = { customerEmail = it },
                label = { Text("Customer Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            when (customerLookupState) {
                StoreViewModel.WalkInCustomerLookupState.Loading -> {
                    Text("Checking customer account...")
                }
                is StoreViewModel.WalkInCustomerLookupState.Found -> {
                    val customer = (customerLookupState as StoreViewModel.WalkInCustomerLookupState.Found).user
                    Text(
                        text = "Customer found: ${customer.firstName} ${customer.lastName}".trim(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Payment mode: Customer app")
                }
                StoreViewModel.WalkInCustomerLookupState.NotFound -> {
                    Text(
                        text = "Email not found. Customer may be new or email may be incorrect.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Text("Payment mode: Cash (new customer)")
                }
                is StoreViewModel.WalkInCustomerLookupState.Error -> {
                    Text(
                        text = (customerLookupState as StoreViewModel.WalkInCustomerLookupState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    if (customerEmail.isNotBlank() && !isEmailValid) {
                        Text("Enter a valid email address", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            ServiceSelectionSection(
                serviceSearch = serviceSearch,
                onServiceSearchChange = { serviceSearch = it },
                servicesState = walkInServicesState,
                services = services,
                selectedServiceIds = selectedServiceIds,
                onServiceToggled = { service ->
                    if (selectedServiceIds.contains(service.id)) {
                        selectedServiceIds = selectedServiceIds - service.id
                        selectedExpertsByService = selectedExpertsByService - service.id
                        selectedTimeSlotsByPair = selectedTimeSlotsByPair.filterKeys { key ->
                            !key.startsWith("${service.id}|")
                        }
                    } else {
                        selectedServiceIds = selectedServiceIds + service.id
                        selectedExpertsByService = selectedExpertsByService + (service.id to emptySet())
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
            ExpertAssignmentSection(
                expertsState = walkInExpertsState,
                selectedServices = selectedServices,
                experts = experts,
                selectedExpertsByService = selectedExpertsByService,
                onExpertToggled = { serviceId, expertId ->
                    val selected = selectedExpertsByService[serviceId].orEmpty()
                    val updated = if (selected.contains(expertId)) selected - expertId else selected + expertId
                    selectedExpertsByService = selectedExpertsByService + (serviceId to updated)
                    if (!updated.contains(expertId)) {
                        selectedTimeSlotsByPair = selectedTimeSlotsByPair - pairKey(serviceId, expertId)
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
            TimeSlotAssignmentSection(
                selectedServices = selectedServices,
                expertsById = expertsById,
                selectedExpertsByService = selectedExpertsByService,
                availableSlotsByExpertState = walkInAvailableSlotsByExpertState,
                selectedTimeSlotsByPair = selectedTimeSlotsByPair,
                onTimeSlotSelected = { serviceId, expertId, slot ->
                    selectedTimeSlotsByPair = selectedTimeSlotsByPair + (pairKey(serviceId, expertId) to slot)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "DATE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selectedDateMillis != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select date", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            DurationSection(
                selectedDurationMinutes = selectedDurationMinutes,
                onQuickDurationSelected = { selectedDurationMinutes = it },
                onOtherClick = { showOtherDurationDialog = true }
            )

            Spacer(modifier = Modifier.height(18.dp))
            ReadOnlyPriceSection(totalPrice = totalPrice)

            val actionError = (bookingActionState as? StoreViewModel.WalkInBookingActionState.Error)?.message
            val errorText = actionError ?: localValidationError
            if (!errorText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(errorText, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
