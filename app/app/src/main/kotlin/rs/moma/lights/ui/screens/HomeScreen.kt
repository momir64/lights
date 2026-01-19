package rs.moma.lights.ui.screens

import androidx.lifecycle.viewmodel.compose.viewModel
import rs.moma.lights.ui.components.ToggleButton
import rs.moma.lights.viewmodels.MainViewModel
import androidx.compose.foundation.layout.*
import rs.moma.lights.ui.components.Slider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import rs.moma.lights.R

@Composable
fun HomeScreen() {
    val vm: MainViewModel = viewModel()
    val config by vm.config.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ToggleButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(end = 16.dp, top = 18.dp, bottom = 39.5.dp)
                    .weight(1f),
                active = config.allOff,
                label = "Turn all off",
                icon = R.drawable.ic_power
            ) { vm.setAllOff(!config.allOff) }

            ToggleButton(
                modifier = Modifier
                    .width(140.dp)
                    .padding(end = 16.dp, top = 14.5.dp, bottom = 43.dp)
                    .weight(1f),
                active = config.nightMode,
                label = "Night mode",
                icon = R.drawable.ic_night
            ) { vm.setNightMode(!config.nightMode) }
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.padding(start = 32.dp, bottom = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(config.brightness, vertical = true) {
                    vm.setBrightness(it)
                }
            }
        }
    }
}