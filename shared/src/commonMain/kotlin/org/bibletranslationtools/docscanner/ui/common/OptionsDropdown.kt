package org.bibletranslationtools.docscanner.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun <T> OptionsDropdown(
    expanded: Boolean,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismissRequest: () -> Unit,
    optionLabel: (T) -> String = { it.toString() }
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            DropdownMenuItem(
                text = {
                    Text(
                        text = optionLabel(option),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = {
                    onSelect(option)
                    onDismissRequest()
                }
            )
            if (index != options.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}
