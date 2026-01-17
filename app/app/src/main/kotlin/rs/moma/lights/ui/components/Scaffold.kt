package rs.moma.lights.ui.components

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.painterResource
import rs.moma.lights.viewmodels.MainViewModel
import androidx.compose.foundation.layout.Row
import rs.moma.lights.ui.theme.AccentColor
import rs.moma.lights.data.models.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.moma.lights.ui.screens.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import rs.moma.lights.R
import rs.moma.lights.ui.dialogs.LightGroupDialog

@Composable
fun Scaffold() {
    val items = listOf(
        "Home" to @Composable { Icon(painterResource(R.drawable.ic_home), "Home") },
        "Lights" to @Composable { Icon(painterResource(R.drawable.ic_lights), "Light groups") },
        "Schedule" to @Composable { Icon(painterResource(R.drawable.ic_schedule), "Schedule") }
    )
    val dialogGroup = remember { mutableStateOf<Group?>(null) }
    val pagerState = rememberPagerState { items.size }
    val scope = rememberCoroutineScope()
    val vm: MainViewModel = viewModel()

    if (dialogGroup.value != null)
        LightGroupDialog(dialogGroup)

    Scaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton({ vm.logout() }) {
                    Icon(painterResource(R.drawable.ic_logout), "Log out", tint = AccentColor)
                }
                Row {
                    if (pagerState.currentPage == 1) {
                        IconButton({ dialogGroup.value = Group() }) {
                            Icon(painterResource(R.drawable.ic_add), "Add light group", tint = AccentColor)
                        }
                        IconButton({ vm.reset() }) {
                            Icon(painterResource(R.drawable.ic_reset), "Reset settings", tint = AccentColor)
                        }
                        IconButton({ vm.save() }) {
                            Icon(painterResource(R.drawable.ic_save), "Save settings", tint = AccentColor)
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { i, (name, icon) ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == i,
                        onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                        icon = icon,
                        label = { Text(name) }
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(pagerState, Modifier.padding(padding)) { page ->
            when (items[page].first) {
                "Home" -> HomeScreen()
                "Lights" -> LightsScreen()
                "Schedule" -> ScheduleScreen()
            }
        }
    }
}