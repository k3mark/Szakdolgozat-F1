package com.example.f1_application.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    repository: F1Repository
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("F1 App Belépés", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Felhasználónév") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Jelszó") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

        Button(
            onClick = {
                scope.launch {
                    val user = repository.login(username, password)
                    if (user != null) onLoginSuccess(username) else error = "Hibás adatok!"
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) { Text("Bejelentkezés") }

        TextButton(onClick = onNavigateToRegister) { Text("Még nincs fiókom, regisztrálok") }
    }
}