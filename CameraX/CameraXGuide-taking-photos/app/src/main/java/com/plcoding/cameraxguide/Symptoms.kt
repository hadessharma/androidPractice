import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsButtonWithDropdown() {

    // List of symptoms
    val symptoms = listOf(
        "Nausea",
        "Headache",
        "Diarrhea",
        "Sore Throat",
        "Fever",
        "Muscle Ache",
        "Loss of Smell or Taste",
        "Cough",
        "Shortness of Breath",
        "Feeling Tired"
    )
    var ratings = mutableMapOf<String, Int>()

    // State to control the visibility of the drop-down menu
    var expanded by remember { mutableStateOf(false) }
    var selectedSymptom by remember { mutableStateOf(symptoms[0]) }

    // State to store the selected rating
    // rating from 1 - 5
    var selectedRating by remember { mutableStateOf(0) }
    var symptomRatings by remember { mutableStateOf(ratings) }

    // State to manage the rating dialog visibility
    var ratingDialogVisible by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Align content to the center
    ) {
        // Symptoms Button
        Button(
            onClick = { expanded = !expanded }, // Toggle drop-down visibility
            modifier = Modifier
                .width(200.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Symptoms")
        }

        // Drop-down menu
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = false }) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = selectedSymptom,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                symptoms.forEachIndexed { index, symptom ->
                    DropdownMenuItem(text = { Text(symptom) }, onClick = {
                        selectedSymptom = symptoms[index]
                        expanded = false
                        ratingDialogVisible = true // Show rating dialog when a symptom is selected
                    }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        // Display selected rating
        Text(text = "${symptomRatings[selectedSymptom] ?: "N/A"}")
    }

    // Rating Dialog
    if (ratingDialogVisible) {
        AlertDialog(onDismissRequest = { ratingDialogVisible = false },
            title = { Text("Rate $selectedSymptom") },
            text = {
                Column {
                    (1..5).forEach { rating ->
                        Button(
                            onClick = {
                                selectedRating = rating
                                symptomRatings = symptomRatings.toMutableMap().apply {
                                    put(selectedSymptom, rating)
                                }
                                ratingDialogVisible = false // Close dialog after rating
                            }, modifier = Modifier.padding(4.dp)
                        ) {
                            Text(text = rating.toString())
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { ratingDialogVisible = false }) {
                    Text("Close")
                }
            })
    }
}