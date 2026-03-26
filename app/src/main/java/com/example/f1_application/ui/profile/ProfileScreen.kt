package com.example.f1_application.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.f1_application.data.repository.F1Repository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    username: String,
    repository: F1Repository,
    onLogout: () -> Unit,
    onUsernameChanged: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(username) }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<Pair<String, Color>?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FIÓK BEÁLLÍTÁSOK", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it },
            label = { Text("Felhasználónév módosítása") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Új jelszó") },
            modifier = Modifier.fillMaxWidth()
        )

        message?.let {
            Text(it.first, color = it.second, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (newUsername.isBlank() || newPassword.length < 4) {
                    message = "Túl rövid név vagy jelszó!" to Color.Red
                    return@Button
                }
                scope.launch {
                    val success = repository.updateUserInfo(username, newUsername, newPassword)
                    if (success) {
                        message = "Adatok sikeresen frissítve!" to Color.Green
                        onUsernameChanged(newUsername)
                    } else {
                        message = "Ez a név már foglalt!" to Color.Red
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ADATOK MENTÉSE")
        }

        Spacer(Modifier.height(16.dp))

        // KEDVENCEK TÖRLÉSE
        OutlinedButton(
            onClick = {
                scope.launch {
                    repository.resetFavorites(username)
                    message = "Kedvencek alaphelyzetbe állítva!" to Color.Blue
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("KEDVENCEK ALAPHELYZETBE")
        }

        Spacer(Modifier.weight(1f))

        // KIJELENTKEZÉS
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("KIJELENTKEZÉS")
        }

        Spacer(Modifier.height(8.dp))

        // FIÓK TÖRLÉSE
        TextButton(
            onClick = {
                scope.launch {
                    repository.deleteAccount(username)
                    onLogout()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(8.dp))
            Text("Fiók végleges törlése", color = MaterialTheme.colorScheme.error)
        }
    }
}