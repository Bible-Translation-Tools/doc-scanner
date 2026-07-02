package org.bibletranslationtools.docscanner.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import docscanner.composeapp.generated.resources.Res
import docscanner.composeapp.generated.resources.login
import docscanner.composeapp.generated.resources.login_info
import docscanner.composeapp.generated.resources.password
import docscanner.composeapp.generated.resources.username
import org.bibletranslationtools.docscanner.ui.common.AlertDialog
import org.bibletranslationtools.docscanner.ui.common.ProgressDialog
import org.bibletranslationtools.docscanner.ui.viewmodel.LoginEvent
import org.bibletranslationtools.docscanner.ui.viewmodel.LoginViewModel
import org.jetbrains.compose.resources.stringResource

class LoginScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: LoginViewModel = koinScreenModel()
        val navigator = LocalNavigator.currentOrThrow

        val state by viewModel.state.collectAsStateWithLifecycle()
        val event by viewModel.event.collectAsStateWithLifecycle(LoginEvent.Idle)

        var username by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var showPassword by rememberSaveable { mutableStateOf(false) }

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        LaunchedEffect(event) {
            if (event is LoginEvent.LoginSuccess) {
                navigator.pop()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.login)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        ) { paddingValue ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValue)
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Login",
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = stringResource(Res.string.login_info),
                    fontWeight = FontWeight.W500,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text(stringResource(Res.string.username)) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text(stringResource(Res.string.password)) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.onEvent(LoginEvent.Login(username.trim(), password))
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    trailingIcon = {
                        if (password.isNotEmpty()) {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) {
                                        Icons.Filled.Visibility
                                    } else Icons.Filled.VisibilityOff,
                                    contentDescription = if (showPassword) {
                                        "Hide password"
                                    } else "Show password"
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onEvent(LoginEvent.Login(username.trim(), password))
                    },
                    enabled = username.isNotEmpty() && password.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(text = stringResource(Res.string.login))
                }
            }

            state.progress?.let { ProgressDialog(it) }
            state.alert?.let { AlertDialog(it.message, it.onClosed) }
        }
    }
}
