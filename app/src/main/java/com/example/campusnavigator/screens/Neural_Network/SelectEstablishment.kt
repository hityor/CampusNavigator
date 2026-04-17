package com.example.campusnavigator.screens.Neural_Network

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstablishmentList(options: List<String>,
                      selectedOption: String?,
                      onOptionSelected: (String) -> Unit,
                      modifier: Modifier = Modifier,
                      placeholder: String = "Выберите заведение") {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier)
    {
        Surface(modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {expanded = true},
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp)
        {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically)
            {
                Text(text = selectedOption ?: placeholder,
                    color = if (selectedOption == null) Color.Gray else Color.Unspecified,
                    style = MaterialTheme.typography.bodyLarge)

                Icon(imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Открыть список",
                    tint = Color.Unspecified)
            }
        }

        DropdownMenu(expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f))
        {
            options.forEach {
                option -> DropdownMenuItem(text = {Text(option)},
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    })
            }
        }
    }

}