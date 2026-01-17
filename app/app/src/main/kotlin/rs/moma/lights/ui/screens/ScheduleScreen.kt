package rs.moma.lights.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTimePickerState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.TimePickerDefaults
import rs.moma.lights.viewmodels.MainViewModel
import androidx.compose.material3.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import rs.moma.lights.ui.theme.*

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScheduleScreen() {
    val vm: MainViewModel = viewModel()
    val config by vm.config.collectAsState()
    val pickerState = rememberTimePickerState(
        initialHour = config.schedule.hour,
        initialMinute = config.schedule.minute,
        is24Hour = true
    )

    LaunchedEffect(pickerState.hour, pickerState.minute) {
        delay(100)
        if (pickerState.hour != config.schedule.hour || pickerState.minute != config.schedule.minute)
            vm.schedule(pickerState.hour, pickerState.minute, config.schedule.allOff, config.schedule.nightMode)
    }

    LaunchedEffect(config.schedule.hour, config.schedule.minute) {
        pickerState.hour = config.schedule.hour
        pickerState.minute = config.schedule.minute
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(42.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimePicker(
            state = pickerState,
            modifier = Modifier.scale(1.1f),
            colors = TimePickerDefaults.colors(timeSelectorUnselectedContentColor = PrimaryColor)
        )

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Turn off the lights")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = config.schedule.allOff,
                onCheckedChange = {
                    vm.schedule(pickerState.hour, pickerState.minute, it, config.schedule.nightMode)
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Disable night mode")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = config.schedule.nightMode,
                onCheckedChange = {
                    vm.schedule(pickerState.hour, pickerState.minute, config.schedule.allOff, it)
                }
            )
        }
    }
}
