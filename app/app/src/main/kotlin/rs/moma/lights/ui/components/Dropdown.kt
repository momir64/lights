package rs.moma.lights.ui.components

import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <T> Dropdown(modifier: Modifier, values: List<T>, selected: T, label: String, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, onExpandedChange = { expanded = !expanded }, modifier) {
        OutlinedTextField(
            value = selected.toString().replaceFirstChar { it.uppercase() },
            label = { Text(label) },
            onValueChange = {},
            readOnly = true,
            modifier = modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            values.forEach {
                DropdownMenuItem(
                    text = { Text(it.toString().replaceFirstChar { it -> it.uppercase() }) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
