package com.atrajit.fluppymodigame.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ApiKeyDialog(
    onDismiss: () -> Unit,
    onApiKeySet: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🤖 AI-Powered Game",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Enter Gemini API Key",
                    fontSize = 16.sp,
                    color = Color(0xFFB0B0B0),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Get your free API key from:\nai.google.dev",
                    fontSize = 12.sp,
                    color = Color(0xFF6BCB77),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { 
                        apiKey = it
                        showError = false
                    },
                    label = { Text("API Key", color = Color(0xFFB0B0B0)) },
                    placeholder = { Text("AIza...", color = Color(0xFF808080)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6BCB77),
                        unfocusedBorderColor = Color(0xFF4D96FF)
                    ),
                    singleLine = true,
                    isError = showError
                )
                
                if (showError) {
                    Text(
                        text = "Please enter a valid API key",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Features list
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF16213E)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AI Features:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6BCB77)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FeatureItem("📊 Adaptive difficulty based on your skill")
                        FeatureItem("💬 Live commentary from Modi & Mamata")
                        FeatureItem("🎭 Level-based dialogue themes")
                        FeatureItem("🎮 Real-time gameplay analysis")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Skip")
                    }
                    
                    Button(
                        onClick = {
                            if (apiKey.isNotBlank() && apiKey.startsWith("AIza")) {
                                onApiKeySet(apiKey.trim())
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6BCB77)
                        )
                    ) {
                        Text("Activate", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color(0xFFE0E0E0),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
