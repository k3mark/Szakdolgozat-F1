package com.example.f1_application.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.f1_application.data.local.UserEntity
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    repository: F1Repository
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Regisztráció", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(username, { username = it }, label = { Text("Új felhasználónév") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Jelszó") }, modifier = Modifier.fillMaxWidth())

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    error = "Minden mezőt tölts ki!"
                    return@Button
                }
                scope.launch {
                    val success = repository.registerUser(UserEntity(username, password))
                    if (success) onRegisterSuccess() else error = "Ez a név már foglalt!"
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) { Text("Regisztráció") }
    }
}