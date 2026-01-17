package rs.moma.lights.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import rs.moma.lights.viewmodels.MainViewModel
import rs.moma.lights.ui.screens.LoginScreen
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun Navigation() {
    val vm: MainViewModel = viewModel()
    val isLoggedIn by vm.isLoggedIn.collectAsState()

    Surface(Modifier.fillMaxSize()) {
        if (isLoggedIn != true)
            LoginScreen()
        else
            Scaffold()
    }
}