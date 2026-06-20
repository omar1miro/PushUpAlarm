package com.pushup.alarm.ui.createalarm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushup.alarm.ui.theme.AccentOrange

private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val dayValues = listOf(1, 2, 3, 4, 5, 6, 7)
private val presetCounts = listOf(10, 15, 20, 30, 50)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAlarmScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateAlarmViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val timePickerState = rememberTimePickerState(
        initialHour = state.hour,
        initialMinute = state.minute,
        is24Hour = true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Edit Alarm" else "New Alarm",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.updateHour(timePickerState.hour)
                    viewModel.updateMinute(timePickerState.minute)
                    viewModel.saveAlarm(onNavigateBack)
                },
                containerColor = AccentOrange
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Push-Ups Required",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetCounts.forEach { count ->
                    FilterChip(
                        selected = state.pushUpCount == count,
                        onClick = { viewModel.updatePushUpCount(count) },
                        label = { Text("$count") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                            selectedLabelColor = AccentOrange
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (state.pushUpCount in presetCounts) "" else state.pushUpCount.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { if (it in 1..200) viewModel.updatePushUpCount(it) }
                },
                label = { Text("Custom count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Repeat Days",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayLabels.forEachIndexed { index, label ->
                    FilterChip(
                        selected = dayValues[index] in state.repeatDays,
                        onClick = { viewModel.toggleRepeatDay(dayValues[index]) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                            selectedLabelColor = AccentOrange
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.label,
                onValueChange = { viewModel.updateLabel(it) },
                label = { Text("Label (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
