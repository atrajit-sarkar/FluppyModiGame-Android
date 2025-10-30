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
import com.atrajit.fluppymodigame.ai.ApiKeyDialog
import com.atrajit.fluppymodigame.ai.GeminiAIManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    // AI Manager
    val aiManager = remember { GeminiAIManager(context) }
    var showApiKeyDialog by remember { mutableStateOf(!aiManager.hasApiKey()) }
    var currentCommentary by remember { mutableStateOf("") }
    var commentarySpeaker by remember { mutableStateOf("") }
    
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
    val boomSound = remember { 
        try {
            MediaPlayer.create(context, R.raw.boom)
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
    
    // Background music
    val backgroundMusic = remember {
        try {
            MediaPlayer.create(context, R.raw.game_theme)?.apply {
                isLooping = true
                setVolume(0.5f, 0.5f) // Increased volume
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Particle system for visual effects
    val particleSystem = remember { ParticleSystem() }
    
    // Shatter effect for collision
    val shatterEffect = remember { ShatterEffect() }
    
    // Mutable reference for game engine (for callbacks)
    var gameEngineRef: GameEngine? by remember { mutableStateOf(null) }
    
    // Game engine
    val gameEngine = remember {
        GameEngine(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            onGameOver = { score ->
                coroutineScope.launch {
                    // Play both collision sounds simultaneously with different volumes
                    collisionSound?.apply {
                        setVolume(1.0f, 1.0f) // 100% volume for bahen-ke-lode
                        start()
                    }
                    boomSound?.apply {
                        setVolume(0.6f, 0.6f) // 60% volume for boom
                        start()
                    }
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
                // Trigger shatter effect - Modi breaks into pieces
                shatterEffect.trigger(
                    position = position.copy(x = position.x + 60f, y = position.y + 60f),
                    size = 120f // Fixed size for shatter effect
                )
                
                // Create dramatic explosion effect on collision
                particleSystem.createExplosion(
                    position = position.copy(x = position.x + 60f, y = position.y + 60f),
                    particleCount = 80,
                    color = Color(0xFFFF5722) // Orange-red explosion
                )
                // Add secondary white flash
                particleSystem.createExplosion(
                    position = position.copy(x = position.x + 60f, y = position.y + 60f),
                    particleCount = 40,
                    color = Color.White
                )
            },
            onScoreEffect = { position ->
                // Create celebratory score effect when passing obstacles
                particleSystem.createExplosion(
                    position = position,
                    particleCount = 25,
                    color = Color(0xFF00FF00) // Bright green
                )
                particleSystem.createExplosion(
                    position = position,
                    particleCount = 15,
                    color = Color(0xFFFFEB3B) // Gold sparkles
                )
            },
            onCommentaryUpdate = { speaker, action ->
                // Generate AI commentary
                gameEngineRef?.let { engine ->
                    if (aiManager.hasApiKey()) {
                        coroutineScope.launch {
                            val scoreLevel = when (engine.score) {
                                in 0..10 -> 0
                                in 11..20 -> 11
                                in 21..30 -> 21
                                in 31..40 -> 31
                                else -> 41
                            }
                            val commentary = aiManager.generateCommentary(
                                scoreLevel, engine.score, action, speaker
                            )
                            if (commentary.isNotEmpty()) {
                                currentCommentary = commentary
                                commentarySpeaker = speaker
                                // Clear after 5.5 seconds (5500ms) - giving user time to read
                                delay(5500)
                                if (currentCommentary == commentary) {
                                    currentCommentary = ""
                                }
                            }
                        }
                    }
                }
            }
        ).also { gameEngineRef = it }
    }
    
    // Clean up resources when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            jumpSound?.release()
            collisionSound?.release()
            boomSound?.release()
            scoreSound?.release()
            backgroundMusic?.stop()
            backgroundMusic?.release()
            gameEngine.stopGame()
        }
    }
    
    // Control background music based on game state
    LaunchedEffect(gameState) {
        when (gameState) {
            GameState.PLAYING -> {
                try {
                    backgroundMusic?.let {
                        if (!it.isPlaying) {
                            it.start()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            GameState.GAME_OVER -> {
                try {
                    backgroundMusic?.let {
                        if (it.isPlaying) {
                            it.pause()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            GameState.START -> {
                try {
                    backgroundMusic?.let {
                        if (it.isPlaying) {
                            it.pause()
                        }
                        it.seekTo(0) // Reset to beginning
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
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
        // Space background
        SpaceBackground(
            modifier = Modifier.fillMaxSize(),
            isDayTime = gameEngine.isDayTime,
            backgroundType = gameEngine.currentTheme.backgroundType
        )
        
        // Draw game elements
        GameRenderer(
            gameEngine = gameEngine,
            shatterEffect = shatterEffect
        )
        
        // Draw particle effects
        ParticleEffect(particleSystem = particleSystem)
        
        // Update shatter effect
        LaunchedEffect(Unit) {
            while (true) {
                shatterEffect.update(0.016f)
                delay(16)
            }
        }
        
        // AI difficulty adjustment on game over
        LaunchedEffect(gameState) {
            if (gameState == GameState.GAME_OVER && aiManager.hasApiKey()) {
                val metrics = gameEngine.getGameplayMetrics()
                val adjustment = aiManager.generateDifficultyAdjustment(
                    gameEngine.score, 
                    metrics.first, 
                    metrics.second, 
                    gameEngine.aiSpeedMultiplier, 
                    metrics.third
                )
                gameEngine.applyAIDifficultyAdjustment(adjustment)
            }
        }
        
        // UI overlays based on game state
        when (gameState) {
            GameState.START -> {
                StartScreen(
                    onStart = {
                        shatterEffect.reset() // Reset shatter effect when starting
                        gameState = GameState.PLAYING
                        gameEngine.startGame()
                    }
                )
            }
            GameState.PLAYING -> {
                // Commentary display at the top
                if (currentCommentary.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .background(
                                color = Color(0xCC000000),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (commentarySpeaker == "mamata") "Mamata:" else "Modi:",
                                color = if (commentarySpeaker == "mamata") Color(0xFFFF6B6B) else Color(0xFF6BCB77),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentCommentary,
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Score display with shadow for better visibility
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = if (currentCommentary.isNotEmpty()) 90.dp else 32.dp)
                ) {
                    // Shadow text
                    Text(
                        text = "Score: ${gameEngine.score}",
                        color = Color.Black,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                    )
                    // Main text
                    Text(
                        text = "Score: ${gameEngine.score}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            GameState.GAME_OVER -> {
                GameOverScreen(
                    score = gameEngine.score,
                    highScore = highScore,
                    onRestart = {
                        shatterEffect.reset()
                        gameEngine.resetGame()
                        gameState = GameState.PLAYING
                        gameEngine.startGame()
                    }
                )
            }
        }
        
        // API Key Dialog
        if (showApiKeyDialog) {
            ApiKeyDialog(
                onDismiss = { showApiKeyDialog = false },
                onApiKeySet = { key ->
                    aiManager.setApiKey(key)
                    showApiKeyDialog = false
                }
            )
        }
    }
}

// Game renderer is now implemented in GameRenderer.kt

@Composable
fun StartScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x60000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title with better styling
            Text(
                text = "Fluppy Modi\nGame",
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 64.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onStart,
                modifier = Modifier
                    .padding(16.dp)
                    .size(width = 200.dp, height = 60.dp)
            ) {
                Text(
                    text = "Start Game",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Tap to fly!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    highScore: Int,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB0000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game Over title with shadow
            Box(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Game Over",
                    color = Color.Black,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(x = 3.dp, y = 3.dp)
                )
                Text(
                    text = "Game Over",
                    color = Color.Red,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Score display
            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Score: $score",
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                )
                Text(
                    text = "Score: $score",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // High score display
            Box(modifier = Modifier.padding(bottom = 40.dp)) {
                Text(
                    text = "High Score: $highScore",
                    color = Color.Black,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                )
                Text(
                    text = "High Score: $highScore",
                    color = Color(0xFFFFD700),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .padding(16.dp)
                    .size(width = 200.dp, height = 60.dp)
            ) {
                Text(
                    text = "Play Again",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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