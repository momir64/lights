package rs.moma.lights.ui.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import rs.moma.lights.viewmodels.MainViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rs.moma.lights.data.models.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import rs.moma.lights.ui.theme.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import rs.moma.lights.ui.components.Dropdown

@Composable
fun LightGroupDialog(group: MutableState<Group?>) {
    val vm: MainViewModel = viewModel()
    val config by vm.config.collectAsState()
    val g = group.value ?: return
    val editMode = g.id > -1

    @Composable
    fun rememberTxt(value: String?) = remember { mutableStateOf(TextFieldValue(value ?: "")) }
    fun acceptDigits(it: TextFieldValue) = it.text.isEmpty() || it.text.matches(Regex("""^\d+$"""))
    fun acceptIp(it: TextFieldValue) = it.text.isEmpty() || it.text.matches(Regex("""^\d[\d.]*$"""))

    var name by remember { mutableStateOf(g.name) }
    var id by rememberTxt(
        if (editMode) g.id.toString()
        else generateSequence(1) { it + 1 }.first { candidate -> config.groups.none { it.id == candidate } }.toString()
    )

    var dayIp by rememberTxt(g.day.ip)
    var dayType by remember { mutableStateOf(g.day.type.toString()) }
    var dayId by remember { mutableIntStateOf(g.day.id ?: 0) }

    var nightIp by rememberTxt(g.night.ip)
    var nightType by remember { mutableStateOf(g.night.type.toString()) }
    var nightId by remember { mutableIntStateOf(g.night.id ?: 0) }
    var nightBrightness by rememberTxt((g.night.brightness ?: 100).toString())

    val valid = name.isNotBlank()
            && dayIp.text.isNotBlank()
            && nightIp.text.isNotBlank()
            && (nightType == LightType.Switch.toString() || nightBrightness.text.isNotBlank())

    Dialog(onDismissRequest = { group.value = null }) {
        val activityWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { activityWindow?.setDimAmount(0.75f) }

        Box(
            Modifier
                .background(CardColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Light group name") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = id,
                        readOnly = true,
                        onValueChange = { if (acceptDigits(it)) id = it },
                        modifier = Modifier.weight(0.33f),
                        label = { Text("ID") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = dayIp,
                    onValueChange = { if (acceptIp(it)) dayIp = it },
                    label = { Text("Day light IP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Row(Modifier.fillMaxWidth()) {
                    Dropdown(Modifier.weight(1f), LightType.entries.map { it.toString() }, dayType, "Day light type") { dayType = it }
                    if (dayType == LightType.Switch.toString()) {
                        Spacer(Modifier.width(12.dp))
                        Dropdown(Modifier.weight(0.75f), listOf(0, 1), dayId, "Output") { dayId = it }
                    }
                }

                OutlinedTextField(
                    value = nightIp,
                    onValueChange = { if (acceptIp(it)) nightIp = it },
                    label = { Text("Night light IP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Row(Modifier.fillMaxWidth()) {
                    Dropdown(Modifier.weight(1f), LightType.entries.map { it.toString() }, nightType, "Night light type") { nightType = it }
                    Spacer(Modifier.width(12.dp))
                    if (nightType == LightType.Switch.toString())
                        Dropdown(Modifier.weight(0.75f), listOf(0, 1), nightId, "Output") { nightId = it }
                    else
                        OutlinedTextField(
                            value = nightBrightness,
                            onValueChange = { if (acceptDigits(it)) nightBrightness = it },
                            modifier = Modifier.weight(0.75f),
                            label = { Text("Brightness") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            suffix = { Text("%") }
                        )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                        onClick = { group.value = null }
                    ) { Text("Cancel", color = PrimaryColor) }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16),
                        colors = ButtonDefaults.buttonColors(containerColor = if (valid) AccentColor else ButtonColor),
                        onClick = {
                            if (!valid) {
                                Toast.makeText(vm.context, "Fill out all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val brightness = if (nightType != LightType.Switch.toString()) nightBrightness.text.toIntOrNull() else null
                            val result = Group(
                                id = id.text.toIntOrNull() ?: -1,
                                name = name.trim(),
                                day = Light(dayIp.text, LightType.fromString(dayType), dayId),
                                night = Light(nightIp.text, LightType.fromString(nightType), nightId, brightness)
                            )

                            if (editMode) {
                                vm.updateGroup(result)
                            } else
                                vm.addGroup(result)

                            group.value = null
                        }
                    ) { Text(if (editMode) "Update" else "Create", color = if (valid) Color.Black else PrimaryColor) }
                }
            }
        }
    }
}

