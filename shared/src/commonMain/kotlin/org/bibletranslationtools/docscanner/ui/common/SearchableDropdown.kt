package org.bibletranslationtools.docscanner.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/** Approx height of one [DropdownMenuItem]; used to size the lazy menu. */
private val ItemHeight = 48.dp

/** How many rows are visible before the menu scrolls. */
private const val MaxVisibleRows = 8

/** Fallback width used before the anchor field has been measured. */
private val FallbackMenuWidth = 280.dp

/**
 * A simple searchable dropdown built on Material3's [ExposedDropdownMenuBox].
 *
 * The menu body is a [LazyColumn], so very large option lists
 * stay fully scrollable while only composing the visible rows. The lazy list is wrapped
 * in a fixed-size [Box] because [ExposedDropdownMenu] measures its content with intrinsic
 * measurements, which lazy lists do not support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
    options: List<T>,
    selectedOption: T?,
    onItemSelected: (T) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() },
    textStyle: TextStyle = TextStyle.Default,
    placeholderTextStyle: TextStyle = textStyle
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldWidthPx by remember { mutableStateOf(0) }
    val selectedLabel = selectedOption?.let(optionLabel) ?: ""
    var query by remember(selectedLabel) { mutableStateOf(selectedLabel) }

    val filtered = remember(query, options, selectedLabel) {
        if (query.isBlank() || query == selectedLabel) {
            options
        } else {
            options.filter { optionLabel(it).contains(query, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            singleLine = true,
            textStyle = textStyle,
            placeholder = { Text(placeholder, style = placeholderTextStyle) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .onSizeChanged { fieldWidthPx = it.width }
        )

        if (filtered.isNotEmpty()) {
            val density = LocalDensity.current
            val menuWidth = if (fieldWidthPx > 0) {
                with(density) { fieldWidthPx.toDp() }
            } else {
                FallbackMenuWidth
            }
            val visibleRows = filtered.size.coerceAtMost(MaxVisibleRows)

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Box(
                    modifier = Modifier
                        .width(menuWidth)
                        .height(ItemHeight * visibleRows)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered) { option ->
                            DropdownMenuItem(
                                text = { Text(optionLabel(option), style = textStyle) },
                                onClick = {
                                    onItemSelected(option)
                                    query = optionLabel(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
