package com.morrislabs.fabs_store.ui.screens

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.data.model.TimeSlot
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel

@Composable
internal fun WalkInBookingStepContent(
    currentStep: Int,
    customerEmail: String,
    onCustomerEmailChange: (String) -> Unit,
    customerLookupState: StoreViewModel.WalkInCustomerLookupState,
    isEmailValid: Boolean,
    serviceSearch: String,
    onServiceSearchChange: (String) -> Unit,
    walkInServicesState: StoreViewModel.LoadingState<List<TypeOfServiceDTO>>,
    services: List<TypeOfServiceDTO>,
    selectedServiceIds: Set<String>,
    onServiceToggled: (TypeOfServiceDTO) -> Unit,
    displayDate: String,
    selectedDateMillis: Long?,
    onDateClick: () -> Unit,
    walkInExpertsState: StoreViewModel.LoadingState<List<ExpertDTO>>,
    selectedServices: List<TypeOfServiceDTO>,
    experts: List<ExpertDTO>,
    selectedExpertsByService: Map<String, Set<String>>,
    onExpertToggled: (String, String) -> Unit,
    expertsById: Map<String, ExpertDTO>,
    walkInAvailableSlotsByPairState: Map<String, StoreViewModel.LoadingState<List<TimeSlot>>>,
    selectedTimeSlotsByPair: Map<String, TimeSlot>,
    onTimeSlotSelected: (String, String, TimeSlot) -> Unit,
    totalPrice: Int
) {
    when (currentStep) {
        0 -> {
            val isCustomerFound = customerLookupState is StoreViewModel.WalkInCustomerLookupState.Found
            val greenColor = Color(0xFF13EC5B)

            Text(
                text = "Customer Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            OutlinedTextField(
                value = customerEmail,
                onValueChange = onCustomerEmailChange,
                label = { Text("Customer Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isCustomerFound) greenColor else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isCustomerFound) greenColor else MaterialTheme.colorScheme.outline
                )
            )

            when (customerLookupState) {
                StoreViewModel.WalkInCustomerLookupState.Loading -> Text("Checking customer account...")
                is StoreViewModel.WalkInCustomerLookupState.Found -> {
                    val customer = customerLookupState.user
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = greenColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Customer found: ${customer.firstName} ${customer.lastName}".trim(),
                            color = greenColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payments and history will be linked to this customer account for seamless tracking across store and app.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                StoreViewModel.WalkInCustomerLookupState.NotFound -> {
                    Text(
                        text = "No account found for this email.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You can still proceed as unregistered walk-in customer.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                is StoreViewModel.WalkInCustomerLookupState.Error -> {
                    Text(text = customerLookupState.message, color = MaterialTheme.colorScheme.error)
                }

                else -> if (customerEmail.isNotBlank() && !isEmailValid) {
                    Text("Enter a valid email address", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        1 -> {
            Text(
                text = "Select Services",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            ServiceSelectionStep(
                serviceSearch = serviceSearch,
                onServiceSearchChange = onServiceSearchChange,
                servicesState = walkInServicesState,
                services = services,
                selectedServiceIds = selectedServiceIds,
                onServiceToggled = onServiceToggled
            )
        }

        else -> {
            Text(
                text = "Schedule Booking",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Pick date first, then assign experts and slots per service.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                onClick = onDateClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF13EC5B))
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

            ExpertAssignmentStep(
                expertsState = walkInExpertsState,
                selectedServices = selectedServices,
                experts = experts,
                selectedExpertsByService = selectedExpertsByService,
                onExpertToggled = onExpertToggled
            )

            TimeSlotAssignmentStep(
                selectedServices = selectedServices,
                expertsById = expertsById,
                selectedExpertsByService = selectedExpertsByService,
                availableSlotsByPairState = walkInAvailableSlotsByPairState,
                selectedTimeSlotsByPair = selectedTimeSlotsByPair,
                onTimeSlotSelected = onTimeSlotSelected
            )

            WalkInBookingTotals(totalPrice = totalPrice)
        }
    }
}
