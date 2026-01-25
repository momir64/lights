package rs.moma.lights.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import rs.moma.lights.viewmodels.MainViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import rs.moma.lights.ui.theme.*
import rs.moma.lights.R

@Composable
fun LoginScreen() {
    var password by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val vm: MainViewModel = viewModel()

    Box(
        Modifier
            .fillMaxSize()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(280.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton({ show = !show }) {
                        Icon(painterResource(if (show) R.drawable.ic_visibility_off else R.drawable.ic_visibility), null)
                    }
                }
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.login(password) },
                enabled = password.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor, disabledContainerColor = ButtonColor)
            ) { Text("Login", color = if (password.isEmpty()) Color.White else Color.Black) }
        }
    }
}