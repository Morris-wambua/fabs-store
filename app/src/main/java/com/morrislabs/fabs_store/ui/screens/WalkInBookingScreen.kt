package com.morrislabs.fabs_store.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.data.model.ReservationCreatedBy
import com.morrislabs.fabs_store.data.model.ReservationDTO
import com.morrislabs.fabs_store.data.model.ReservationStatus
import com.morrislabs.fabs_store.data.model.TimeSlot
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.data.model.toDisplayName
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun WalkInBookingScreen(
    storeId: String,
    storeViewModel: StoreViewModel,
    onNavigateBack: () -> Unit,
    onBookingCreated: () -> Unit
) {
    val walkInServicesState by storeViewModel.walkInServicesState.collectAsState()
    val walkInExpertsState by storeViewModel.walkInExpertsState.collectAsState()
    val walkInAvailableSlotsByPairState by storeViewModel.walkInAvailableSlotsByPairState.collectAsState()
    val bookingActionState by storeViewModel.walkInBookingActionState.collectAsState()
    val customerLookupState by storeViewModel.walkInCustomerLookupState.collectAsState()

    var currentStep by remember { mutableIntStateOf(0) }
    var customerEmail by remember { mutableStateOf("") }
    var serviceSearch by remember { mutableStateOf("") }
    var selectedServiceIds by remember { mutableStateOf(setOf<String>()) }
    var selectedExpertsByService by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedTimeSlotsByPair by remember { mutableStateOf(mapOf<String, TimeSlot>()) }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showUnregisteredWarning by remember { mutableStateOf(false) }

    val dateFormat = remember { DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()) }
    val displayDate = selectedDateMillis?.let { dateFormat.format(Date(it)) } ?: "Select a date"
    val services = (walkInServicesState as? StoreViewModel.LoadingState.Success<List<TypeOfServiceDTO>>)?.data.orEmpty()
    val experts = (walkInExpertsState as? StoreViewModel.LoadingState.Success<List<ExpertDTO>>)?.data.orEmpty()
    val expertsById = remember(experts) { experts.associateBy { it.id } }
    val selectedServices = remember(services, selectedServiceIds) { services.filter { selectedServiceIds.contains(it.id) } }
    val selectedPairs = remember(selectedServices, selectedExpertsByService) {
        selectedServices.flatMap { service ->
            selectedExpertsByService[service.id].orEmpty().map { expertId -> Triple(service.id, expertId, service.duration ?: 60) }
        }
    }
    val totalPrice = selectedServices.sumOf { service -> service.price * selectedExpertsByService[service.id].orEmpty().size }

    val isEmailValid = customerEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(customerEmail.trim()).matches()
    val resolvedReservationUserId = when (val state = customerLookupState) {
        is StoreViewModel.WalkInCustomerLookupState.Found -> state.user.id
        StoreViewModel.WalkInCustomerLookupState.NotFound -> storeId
        else -> null
    }

    val scheduleStepValid = selectedDateMillis != null &&
        selectedServices.isNotEmpty() &&
        selectedServices.all { selectedExpertsByService[it.id].orEmpty().isNotEmpty() } &&
        selectedPairs.all { selectedTimeSlotsByPair[pairKey(it.first, it.second)] != null } &&
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

    LaunchedEffect(selectedDateMillis, selectedPairs) {
        val dateMillis = selectedDateMillis
        if (dateMillis == null) {
            storeViewModel.clearWalkInAvailableSlots()
            selectedTimeSlotsByPair = emptyMap()
            return@LaunchedEffect
        }

        if (selectedPairs.isEmpty()) {
            storeViewModel.clearWalkInAvailableSlots()
            selectedTimeSlotsByPair = emptyMap()
            return@LaunchedEffect
        }

        val bookingDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString()

        storeViewModel.clearWalkInAvailableSlots()
        selectedTimeSlotsByPair = selectedTimeSlotsByPair.filterKeys { key ->
            selectedPairs.any { pairKey(it.first, it.second) == key }
        }

        selectedPairs.forEach { (serviceId, expertId, durationMinutes) ->
            storeViewModel.fetchWalkInAvailableSlots(
                serviceId = serviceId,
                expertId = expertId,
                date = bookingDate,
                durationMinutes = durationMinutes
            )
        }
    }

    LaunchedEffect(bookingActionState) {
        if (bookingActionState is StoreViewModel.WalkInBookingActionState.Success) {
            storeViewModel.resetWalkInBookingActionState()
            onBookingCreated()
        }
    }

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

    if (showUnregisteredWarning) {
        AlertDialog(
            onDismissRequest = { showUnregisteredWarning = false },
            title = { Text("Unregistered Customer") },
            text = {
                Text(
                    "No customer account was found for this email. Proceeding will create a walk-in booking tracked under store-side records, and customer app-linked payment tracking will not be available."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnregisteredWarning = false
                        currentStep = 1
                        localValidationError = null
                    }
                ) { Text("Proceed") }
            },
            dismissButton = {
                TextButton(onClick = { showUnregisteredWarning = false }) { Text("Back") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    val title = when (currentStep) {
                        0 -> "Walk-in Booking: Customer"
                        1 -> "Walk-in Booking: Services"
                        else -> "Walk-in Booking: Schedule"
                    }
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) {
                            currentStep -= 1
                            localValidationError = null
                        } else {
                            onNavigateBack()
                        }
                    }) {
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
                        when (currentStep) {
                            0 -> {
                                if (!isEmailValid) {
                                    localValidationError = "Enter a valid customer email"
                                    return@Button
                                }
                                when (customerLookupState) {
                                    StoreViewModel.WalkInCustomerLookupState.Loading -> {
                                        localValidationError = "Customer lookup in progress"
                                    }
                                    is StoreViewModel.WalkInCustomerLookupState.Found -> {
                                        currentStep = 1
                                    }
                                    StoreViewModel.WalkInCustomerLookupState.NotFound -> {
                                        showUnregisteredWarning = true
                                    }
                                    is StoreViewModel.WalkInCustomerLookupState.Error -> {
                                        localValidationError = (customerLookupState as StoreViewModel.WalkInCustomerLookupState.Error).message
                                    }
                                    else -> {
                                        localValidationError = "Wait for customer lookup to complete"
                                    }
                                }
                            }

                            1 -> {
                                if (selectedServices.isEmpty()) {
                                    localValidationError = "Select at least one service"
                                    return@Button
                                }
                                currentStep = 2
                            }

                            else -> {
                                if (!scheduleStepValid) {
                                    localValidationError = "Select date, experts, and slots for all services"
                                    return@Button
                                }
                                val userId = resolvedReservationUserId
                                if (userId.isNullOrBlank()) {
                                    localValidationError = "Customer lookup is still in progress"
                                    return@Button
                                }
                                val bookingDate = Instant.ofEpochMilli(selectedDateMillis!!)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                val payloads = selectedServices.flatMap { service ->
                                    selectedExpertsByService[service.id].orEmpty().map { expertId ->
                                        val slot = selectedTimeSlotsByPair[pairKey(service.id, expertId)]
                                        if (slot == null) {
                                            localValidationError = "Select a slot for each service and expert"
                                            return@map null
                                        }
                                        ReservationDTO(
                                            userId = userId,
                                            name = "Appointment for ${service.subCategory.toDisplayName()}",
                                            price = service.price.toDouble(),
                                            reservationDate = bookingDate.toString(),
                                            startTime = slot.startTime,
                                            endTime = slot.endTime,
                                            expert = expertsById[expertId]?.name.orEmpty().ifBlank { "Expert" },
                                            status = ReservationStatus.BOOKED_ACCEPTED,
                                            createdBy = ReservationCreatedBy.STORE,
                                            store = storeId,
                                            typeOfService = service.id,
                                            reservationExpert = expertId
                                        )
                                    }.filterNotNull()
                                }

                                if (payloads.isEmpty()) {
                                    localValidationError = "No valid reservations to submit"
                                    return@Button
                                }
                                storeViewModel.createWalkInReservations(payloads)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    enabled = bookingActionState !is StoreViewModel.WalkInBookingActionState.Loading,
                    colors = if (currentStep == 2) {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B4311),
                            contentColor = Color.White
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF13EC5B),
                            contentColor = Color.Black
                        )
                    }
                ) {
                    if (bookingActionState is StoreViewModel.WalkInBookingActionState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = if (currentStep == 2) Color.White else Color.Black
                        )
                    } else {
                        if (currentStep == 2) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm Booking")
                        } else {
                            Text("Continue")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WalkInStepProgress(currentStep = currentStep)

            WalkInBookingStepContent(
                currentStep = currentStep,
                customerEmail = customerEmail,
                onCustomerEmailChange = { customerEmail = it },
                customerLookupState = customerLookupState,
                isEmailValid = isEmailValid,
                serviceSearch = serviceSearch,
                onServiceSearchChange = { serviceSearch = it },
                walkInServicesState = walkInServicesState,
                services = services,
                selectedServiceIds = selectedServiceIds,
                onServiceToggled = { service ->
                    if (selectedServiceIds.contains(service.id)) {
                        selectedServiceIds = selectedServiceIds - service.id
                        selectedExpertsByService = selectedExpertsByService - service.id
                        selectedTimeSlotsByPair = selectedTimeSlotsByPair.filterKeys { !it.startsWith("${service.id}|") }
                    } else {
                        selectedServiceIds = selectedServiceIds + service.id
                        selectedExpertsByService = selectedExpertsByService + (service.id to emptySet())
                    }
                },
                displayDate = displayDate,
                selectedDateMillis = selectedDateMillis,
                onDateClick = { showDatePicker = true },
                walkInExpertsState = walkInExpertsState,
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
                },
                expertsById = expertsById,
                walkInAvailableSlotsByPairState = walkInAvailableSlotsByPairState,
                selectedTimeSlotsByPair = selectedTimeSlotsByPair,
                onTimeSlotSelected = { serviceId, expertId, slot ->
                    selectedTimeSlotsByPair = selectedTimeSlotsByPair + (pairKey(serviceId, expertId) to slot)
                },
                totalPrice = totalPrice
            )

            val actionError = (bookingActionState as? StoreViewModel.WalkInBookingActionState.Error)?.message
            val errorText = actionError ?: localValidationError
            if (!errorText.isNullOrBlank()) {
                Text(errorText, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

