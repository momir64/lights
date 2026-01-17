package rs.moma.lights.ui.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import rs.moma.lights.ui.theme.CardColor
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rs.moma.lights.ui.theme.*

@Composable
fun ConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Removal confirmation", fontSize = 24.sp)
            val activityWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            SideEffect { activityWindow?.setDimAmount(0.75f) }
        },
        text = {
            Text(
                text = "Are you sure you want to delete this light group?",
                modifier = Modifier.padding(bottom = 6.dp),
                fontSize = 18.sp,
                color = OutlineColor
            )
        },
        dismissButton = {
            Button(
                modifier = Modifier.size(96.dp, 42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                shape = RoundedCornerShape(16),
                onClick = onDismiss
            ) { Text("Cancel", color = Color.White) }
            Spacer(Modifier.width(2.dp))
        },
        confirmButton = {
            Button(
                modifier = Modifier.size(96.dp, 42.dp),
                shape = RoundedCornerShape(16),
                onClick = onConfirm
            ) { Text("Delete") }
        },
        containerColor = CardColor,
        shape = RoundedCornerShape(8)
    )
}
