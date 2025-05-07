package org.bibletranslationtools.docscanner.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.cancel
import docscanner.composeapp.generated.resources.login
import docscanner.composeapp.generated.resources.password
import docscanner.composeapp.generated.resources.username
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginDialog(
    onLogin: (String, String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var showPassword: Boolean by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(Res.string.login),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(stringResource(Res.string.username))
                    },
                    modifier = Modifier.semantics {
                        contentType = ContentType.Username
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(stringResource(Res.string.password))
                    },
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if(showPassword) {
                                    Icons.Filled.VisibilityOff
                                } else Icons.Filled.Visibility,
                                contentDescription = "Show Password"
                            )
                        }
                    },
                    modifier = Modifier.semantics {
                        contentType = ContentType.Password
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Spacer(Modifier.width(0.dp))

                    Button(onClick = onDismissRequest) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = {
                            onLogin(username.trim(), password)
                            onDismissRequest()
                        },
                        enabled = username.isNotEmpty() &&
                                password.isNotEmpty()
                    ) {
                        Text(stringResource(Res.string.login))
                    }
                }
            }
        }
    }
}