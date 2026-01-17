package rs.moma.lights.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.moma.lights.ui.theme.*

@Composable
fun ToggleButton(active: Boolean, icon: Int, label: String? = null, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) AccentColor else ButtonColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painterResource(icon), null, tint = if (active) Color.Black else Color.White)
            if (label != null) {
                Spacer(Modifier.height(12.dp))
                Text(label, color = if (active) Color.Black else Color.White)
            }
        }
    }
}