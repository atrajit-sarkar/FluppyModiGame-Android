package com.atrajit.fluppymodigame.game

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atrajit.fluppymodigame.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    // Game state
    var gameState by remember { mutableStateOf(GameState.START) }
    var highScore by remember { mutableStateOf(getHighScore(context)) }
    
    // Screen dimensions
    val screenWidth = with(density) { 
        context.resources.displayMetrics.widthPixels.toFloat() 
    }
    val screenHeight = with(density) { 
        context.resources.displayMetrics.heightPixels.toFloat() 
    }
    
    // Sound effects
    val jumpSound = remember { 
        try {
            MediaPlayer.create(context, R.raw.jump)
        } catch (e: Exception) {
            null
        }
    }
    val collisionSound = remember { 
        try {
            MediaPlayer.create(context, R.raw.collision)
        } catch (e: Exception) {
            null
        }
    }
    val scoreSound = remember { 
        try {
            MediaPlayer.create(context, R.raw.score)
        } catch (e: Exception) {
            null
        }
    }
    
    // Particle system for visual effects
    val particleSystem = remember { ParticleSystem() }
    
    // Game engine
    val gameEngine = remember {
        GameEngine(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            onGameOver = { score ->
                coroutineScope.launch {
                    collisionSound?.start()
                    gameState = GameState.GAME_OVER
                    if (score > highScore) {
                        highScore = score
                        saveHighScore(context, highScore)
                    }
                }
            },
            onScoreUpdate = {
                coroutineScope.launch {
                    scoreSound?.start()
                }
            },
            onCollision = { position ->
                // Create explosion effect on collision
                particleSystem.createExplosion(
                    position = position.copy(x = position.x + 30f, y = position.y + 30f),
                    particleCount = 30,
                    color = Color.Red
                )
            },
            onScoreEffect = { position ->
                // Create score effect when passing obstacles
                particleSystem.createExplosion(
                    position = position,
                    particleCount = 15,
                    color = Color.Green
                )
            }
        )
    }
    
    // Clean up resources when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            jumpSound?.release()
            collisionSound?.release()
            scoreSound?.release()
            gameEngine.stopGame()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (gameEngine.isDayTime) Color(0xFF87CEEB) else Color(0xFF191970)
            )
            .pointerInput(gameState) {
                detectTapGestures {
                    when (gameState) {
                        GameState.PLAYING -> {
                            gameEngine.onTap()
                            coroutineScope.launch {
                                jumpSound?.seekTo(0)
                                jumpSound?.start()
                                // Create particle effect at bird position when jumping
                                particleSystem.createExplosion(
                                    position = gameEngine.birdPosition.copy(
                                        x = gameEngine.birdPosition.x + 30f,
                                        y = gameEngine.birdPosition.y + 30f
                                    ),
                                    particleCount = 5,
                                    color = Color.White
                                )
                            }
                        }
                        GameState.START -> {
                            gameState = GameState.PLAYING
                            gameEngine.startGame()
                        }
                        GameState.GAME_OVER -> {
                            // Do nothing on tap during game over
                        }
                    }
                }
            }
    ) {
        // Draw game elements
        GameRenderer(gameEngine = gameEngine)
        
        // Draw particle effects
        ParticleEffect(particleSystem = particleSystem)
        
        // UI overlays based on game state
        when (gameState) {
            GameState.START -> {
                StartScreen(
                    onStart = {
                        gameState = GameState.PLAYING
                        gameEngine.startGame()
                    }
                )
            }
            GameState.PLAYING -> {
                // Score display
                Text(
                    text = "Score: ${gameEngine.score}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp)
                )
            }
            GameState.GAME_OVER -> {
                GameOverScreen(
                    score = gameEngine.score,
                    highScore = highScore,
                    onRestart = {
                        gameEngine.resetGame()
                        gameState = GameState.PLAYING
                        gameEngine.startGame()
                    }
                )
            }
        }
    }
}

// Game renderer is now implemented in GameRenderer.kt

@Composable
fun StartScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Fluppy Modi Game",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = onStart,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Start Game",
                fontSize = 20.sp
            )
        }
        
        Text(
            text = "Tap to fly!",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    highScore: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Over",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Score: $score",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "High Score: $highScore",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = onRestart,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Play Again",
                fontSize = 20.sp
            )
        }
    }
}

enum class GameState {
    START,
    PLAYING,
    GAME_OVER
}

// SharedPreferences functions for high score
private fun getHighScore(context: Context): Int {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    return prefs.getInt("high_score", 0)
}

private fun saveHighScore(context: Context, score: Int) {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    prefs.edit().putInt("high_score", score).apply()
}