package org.bibletranslationtools.docscanner.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.search
import org.example.dropdown.data.DropdownConfig
import org.example.dropdown.data.search.SearchIcon
import org.example.dropdown.data.search.SearchInput
import org.example.dropdown.data.search.SearchSettings
import org.example.dropdown.data.selection.SingleItemContentConfig
import org.example.project.ui.SearchableDropdown
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.KProperty1

@Composable
fun <T: Any> SearchableComboBox(
    items: List<T> = emptyList(),
    selected: MutableState<T?> = remember { mutableStateOf(null) },
    onSelected: (T) -> Unit = {},
    searchEnabled: Boolean = true,
    titleProperty: KProperty1<T, *>,
    subtitleProperty: KProperty1<T, *>,
    placeHolderText: String = "",
    properties: List<KProperty1<T, *>> = emptyList()
) {
    SearchableDropdown(
        items = items,
        searchSettings = SearchSettings(
            searchEnabled = searchEnabled,
            searchProperties = properties,
            searchIcon = SearchIcon(
                iconTintColor = MaterialTheme.colorScheme.onBackground
            ),
            searchInput = SearchInput(
                inputTextColor = MaterialTheme.colorScheme.onBackground,
                placeholder = {
                    Text(
                        text = stringResource(Res.string.search),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        ),
        dropdownConfig = DropdownConfig(
            horizontalPadding = 16.dp,
            shape = RoundedCornerShape(8.dp),
            headerPlaceholder = { Text(text = placeHolderText) },
            headerBackgroundColor = MaterialTheme.colorScheme.background,
            contentBackgroundColor = MaterialTheme.colorScheme.background,
            separationSpace = 0
        ),
        itemContentConfig = SingleItemContentConfig.Custom(
            content = { item, _ ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text(
                        text = titleProperty.get(item).toString(),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = subtitleProperty.get(item).toString(),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        ),
        selectedItem = selected
    )
}