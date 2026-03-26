package com.example.f1_application.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.theme.*
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
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── HEADER ───────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PROFILE",
                style = MaterialTheme.typography.headlineLarge,
                color = F1Red
            )
            Text(
                text = "ACCOUNT SETTINGS",
                style = MaterialTheme.typography.labelLarge,
                color = F1TextHint,
                letterSpacing = 3.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── AVATAR ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(F1Surface)
                .border(2.dp, F1Red, RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.take(2).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = F1Red,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(text = username, style = MaterialTheme.typography.titleLarge, color = F1TextPrim, fontWeight = FontWeight.Bold)
        Text(text = "F1 RACE TRACKER", style = MaterialTheme.typography.labelSmall, color = F1TextHint)

        Spacer(Modifier.height(28.dp))

        // ── ACCOUNT DETAILS ──────────────────────────────────────
        F1ProfileSectionHeader("ACCOUNT DETAILS")
        Spacer(Modifier.height(10.dp))

        F1ProfileTextField(value = newUsername, onValueChange = { newUsername = it }, label = "USERNAME", icon = Icons.Default.Person)
        Spacer(Modifier.height(8.dp))
        F1ProfileTextField(value = newPassword, onValueChange = { newPassword = it }, label = "NEW PASSWORD", icon = Icons.Default.Lock, isPassword = true)

        message?.let { (msg, color) ->
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.12f))
                    .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(text = msg, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                if (newUsername.isBlank() || newPassword.length < 4) {
                    message = "Username or password too short (min. 4 chars)!" to F1Red
                    return@Button
                }
                scope.launch {
                    val success = repository.updateUserInfo(username, newUsername, newPassword)
                    if (success) {
                        message = "Details updated successfully!" to F1Green
                        onUsernameChanged(newUsername)
                    } else {
                        message = "This username is already taken!" to F1Red
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = F1Red, contentColor = F1TextPrim),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "SAVE CHANGES", style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
        }

        Spacer(Modifier.height(28.dp))

        // ── FAVORITES ────────────────────────────────────────────
        F1ProfileSectionHeader("FAVORITES")
        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    repository.resetFavorites(username)
                    message = "Favorites reset!" to F1Orange
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = F1Orange),
            border = androidx.compose.foundation.BorderStroke(1.dp, F1Orange.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = "RESET FAVORITES", style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
        }

        Spacer(Modifier.height(28.dp))

        // ── SESSION ───────────────────────────────────────────────
        F1ProfileSectionHeader("SESSION")
        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = F1Surface2, contentColor = F1TextPrim),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = "LOG OUT", style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    repository.deleteAccount(username)
                    onLogout()
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = F1Red.copy(alpha = 0.8f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, F1Red.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = "DELETE ACCOUNT", style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun F1ProfileSectionHeader(title: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(3.dp).height(14.dp).background(F1Red, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = F1TextHint, letterSpacing = 2.sp)
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(color = F1Border, modifier = Modifier.weight(1f))
    }
}

@Composable
fun F1ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall, color = F1TextHint) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(icon, null, tint = F1TextHint, modifier = Modifier.size(18.dp)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = F1Red,
            unfocusedBorderColor = F1Border,
            focusedTextColor = F1TextPrim,
            unfocusedTextColor = F1TextPrim,
            cursorColor = F1Red,
            focusedContainerColor = F1Surface,
            unfocusedContainerColor = F1Surface,
            focusedLabelColor = F1Red,
            unfocusedLabelColor = F1TextHint
        ),
        shape = RoundedCornerShape(8.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = F1TextPrim)
    )
}