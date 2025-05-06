package org.bibletranslationtools.docscanner.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.bibletranslationtools.docscanner.data.local.git.Profile

data class ExtraAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

enum class PageType {
    SPLASH,
    HOME,
    PROJECT,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    title: String,
    profile: Profile?,
    page: PageType,
    vararg extraAction: ExtraAction,
) {
    val navigator = LocalNavigator.currentOrThrow
    var showDropDownMenu by remember { mutableStateOf(false) }

    var profileState by remember { mutableStateOf(profile) }
    var actionsState by remember { mutableStateOf(extraAction) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(profile, extraAction) {
        profileState = profile
        actionsState = extraAction
        showMenu = profile != null && extraAction.isNotEmpty()
    }

    TopAppBar(
        title = {
            SingleLineText(title)
        },
        navigationIcon = {
            if (page != PageType.HOME) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (showMenu) {
                IconButton(onClick = { showDropDownMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = showDropDownMenu,
                    onDismissRequest = { showDropDownMenu = false },
                    modifier = Modifier.width(200.dp)
                ) {
                    profileState?.let { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                                .height(32.dp)
                                .padding(bottom = 4.dp)
                        ) {
                            Text(text = user.currentUser)
                        }
                    }

                    HorizontalDivider()

                    if (page != PageType.SETTINGS) {
//                    DropdownMenuItem(
//                        text = { Text(stringResource(Res.string.settings)) },
//                        leadingIcon = {
//                            Icon(
//                                imageVector = Icons.Default.Settings,
//                                contentDescription = null
//                            )
//                        },
//                        onClick = {
//                            showDropDownMenu = false
//                            if (navigator.lastItem !is SettingsScreen) {
//                                navigator.push(SettingsScreen(user))
//                            }
//                        }
//                    )
                    }

                    actionsState.forEach {
                        DropdownMenuItem(
                            text = { Text(it.title) },
                            leadingIcon = {
                                Icon(
                                    imageVector = it.icon,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showDropDownMenu = false
                                it.onClick()
                            }
                        )
                    }
                }
            }
        }
    )
}