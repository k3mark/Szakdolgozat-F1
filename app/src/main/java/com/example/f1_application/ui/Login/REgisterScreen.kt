package com.example.f1_application.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.f1_application.data.local.UserEntity
import com.example.f1_application.data.repository.F1Repository
import com.example.f1_application.ui.theme.*
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "F1",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                color = F1Red,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "RACE TRACKER",
                style = MaterialTheme.typography.labelLarge,
                color = F1TextHint,
                letterSpacing = 5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(F1Surface)
                    .border(1.dp, F1Border, RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "ÚJ FIÓK LÉTREHOZÁSA",
                        style = MaterialTheme.typography.labelLarge,
                        color = F1TextHint,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("FELHASZNÁLÓNÉV", style = MaterialTheme.typography.labelSmall, color = F1TextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = F1TextHint, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = F1Red,
                            unfocusedBorderColor = F1Border,
                            focusedTextColor = F1TextPrim,
                            unfocusedTextColor = F1TextPrim,
                            cursorColor = F1Red,
                            focusedContainerColor = F1Surface2,
                            unfocusedContainerColor = F1Surface2
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = F1TextPrim)
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("JELSZÓ", style = MaterialTheme.typography.labelSmall, color = F1TextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = F1TextHint, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = F1Red,
                            unfocusedBorderColor = F1Border,
                            focusedTextColor = F1TextPrim,
                            unfocusedTextColor = F1TextPrim,
                            cursorColor = F1Red,
                            focusedContainerColor = F1Surface2,
                            unfocusedContainerColor = F1Surface2
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = F1TextPrim)
                    )

                    error?.let {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(F1Red.copy(alpha = 0.12f))
                                .border(1.dp, F1Red.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(it, color = F1Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                error = "Minden mezőt tölts ki!"
                                return@Button
                            }
                            scope.launch {
                                val success = repository.registerUser(UserEntity(username, password))
                                if (success) onRegisterSuccess()
                                else error = "Ez a felhasználónév már foglalt!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = F1Red,
                            contentColor = F1TextPrim
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "REGISZTRÁCIÓ",
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onRegisterSuccess) {
                Text(
                    text = "←  Vissza a belépéshez",
                    color = F1TextSec,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
