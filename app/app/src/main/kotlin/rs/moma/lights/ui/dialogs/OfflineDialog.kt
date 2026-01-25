package rs.moma.lights.ui.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import rs.moma.lights.viewmodels.MainViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.material3.Button
import rs.moma.lights.ui.theme.CardColor
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import rs.moma.lights.ui.theme.*

@Composable
fun OfflineDialog() {
    val vm: MainViewModel = viewModel()
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text("Server is unreachable", fontSize = 24.sp)
            val activityWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            SideEffect { activityWindow?.setDimAmount(0.75f) }
        },
        text = {
            Text(
                text = "We were unable to reach the server, please try again later.",
                modifier = Modifier.padding(bottom = 6.dp),
                fontSize = 18.sp,
                color = OutlineColor
            )
        },
        confirmButton = {
            Button(
                modifier = Modifier.size(96.dp, 42.dp),
                shape = RoundedCornerShape(16),
                onClick = { scope.launch { vm.ping(true) } }
            ) { Text("Retry") }
        },
        containerColor = CardColor,
        shape = RoundedCornerShape(8)
    )
}
