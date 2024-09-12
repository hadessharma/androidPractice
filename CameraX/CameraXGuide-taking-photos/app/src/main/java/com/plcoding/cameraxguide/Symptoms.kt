import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsButtonWithDropdown() {

    // List of symptoms
    val symptoms = listOf("Headache", "Nausea", "Dizziness", "Fatigue")

    // State to control the visibility of the drop-down menu
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(symptoms[0]) }


    Column {
        // Symptoms Button
        Button(
            onClick = { expanded = !expanded }, // Toggle drop-down visibility
            modifier = Modifier
                .width(150.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("Symptoms")
        }

        // Drop-down menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = false }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                symptoms.forEachIndexed { index, symptom ->
                    DropdownMenuItem(
                        text = { Text(symptom) },
                        onClick = {
                            selectedText = symptoms[index]
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
