package com.atrajit.fluppymodigame.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.atrajit.fluppymodigame.ai.DifficultyAdjustment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameEngine(
    private val screenWidth: Float,
    private val screenHeight: Float,
    private val onGameOver: (Int) -> Unit,
    private val onScoreUpdate: (Int) -> Unit,
    private val onCollision: (Offset) -> Unit = {},
    private val onScoreEffect: (Offset) -> Unit = {},
    private val onCommentaryUpdate: (String, String) -> Unit = { _, _ -> } // (speaker, text)
) {
    // Game state
    var isRunning by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var gameOver by mutableStateOf(false)
    var score by mutableStateOf(0)
    var currentTheme by mutableStateOf(ThemeManager.getThemeForScore(0))
    
    // AI gameplay tracking
    var hitCount = 0
    private var gameStartTime = 0L
    private var totalGameTime = 0f
    private var gamesPlayed = 0
    var aiSpeedMultiplier by mutableStateOf(1.0f)
    var aiGapMultiplier by mutableStateOf(1.0f)
    
    // Bird properties
    var birdPosition by mutableStateOf(Offset(screenWidth / 4, screenHeight / 2))
    var birdVelocity by mutableStateOf(0f)
    val birdSize = Size(120f, 120f)  // Increased from 60 to 120 for better visibility
    
    // Physics constants
    private val gravity = 0.6f
    private val jumpVelocity = -12f
    private val maxVelocity = 18f
    
    // Obstacles - Progressive difficulty system
    var obstacles by mutableStateOf<List<Obstacle>>(emptyList())
    private val initialGapHeight = 650f  // Very generous starting gap
    private val minGapHeight = 350f  // Minimum gap at higher levels
    private var currentGapHeight = initialGapHeight
    
    private val obstacleWidth = 300f  // Original PNG width
    
    private val initialMinObstacleHeight = 80f  // Start with very short obstacles
    private val maxMinObstacleHeight = 200f  // Maximum obstacle height at higher levels
    private var currentMinObstacleHeight = initialMinObstacleHeight
    
    private val initialMaxObstacleHeight = 100f  // Initial max obstacle height
    private val maxObstacleHeight = 350f  // Maximum at higher levels
    private var currentMaxObstacleHeight = initialMaxObstacleHeight
    
    private val obstacleSpacing = 1000f  // More spacing between obstacles
    private var obstacleSpeed = 5f  // Base speed
    
    // Game loop
    private var gameJob: Job? = null
    private val gameScope = CoroutineScope(Dispatchers.Default)
    
    // Day/Night cycle
    var isDayTime by mutableStateOf(true)
    private var timeElapsed = 0f
    private val dayNightCycleDuration = 30f // seconds
    
    // Difficulty progression - slower increase for better gameplay
    private var difficultyLevel = 1
    private val difficultyIncreaseInterval = 15 // points - increased from 10 to 15
    
    // Gameplay metrics for AI
    fun getGameplayMetrics(): Triple<Int, Float, Float> {
        val avgSurvivalTime = if (gamesPlayed > 0) totalGameTime / gamesPlayed else 0f
        return Triple(hitCount, avgSurvivalTime, currentGapHeight)
    }
    
    fun applyAIDifficultyAdjustment(adjustment: DifficultyAdjustment) {
        aiSpeedMultiplier = adjustment.speedMultiplier.coerceIn(0.8f, 1.5f)
        aiGapMultiplier = adjustment.gapMultiplier.coerceIn(0.8f, 1.3f)
    }
    
    init {
        resetGame()
    }
    
    fun startGame() {
        if (isRunning) return
        
        resetGame()
        isRunning = true
        gameOver = false
        gameStartTime = System.currentTimeMillis()
        
        gameJob = gameScope.launch {
            var lastFrameTime = System.currentTimeMillis()
            
            while (isRunning && !gameOver) {
                if (!isPaused) {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = (currentTime - lastFrameTime) / 16.67f // normalize to ~60fps
                    lastFrameTime = currentTime
                    
                    update(deltaTime)
                }
                delay(16) // ~60fps
            }
        }
    }
    
    fun pauseGame() {
        isPaused = true
    }
    
    fun resumeGame() {
        isPaused = false
    }
    
    fun stopGame() {
        isRunning = false
        gameJob?.cancel()
    }
    
    fun resetGame() {
        birdPosition = Offset(screenWidth / 4, screenHeight / 2)
        birdVelocity = 0f
        obstacles = emptyList()
        score = 0
        gameOver = false
        isPaused = false
        obstacleSpeed = 5f
        difficultyLevel = 1
        timeElapsed = 0f
        isDayTime = true
        currentTheme = ThemeManager.getThemeForScore(0)
        
        // Reset difficulty parameters
        currentGapHeight = initialGapHeight
        currentMinObstacleHeight = initialMinObstacleHeight
        currentMaxObstacleHeight = initialMaxObstacleHeight
        
        // Initialize obstacles
        generateInitialObstacles()
    }
    
    fun onTap() {
        if (gameOver) return
        
        birdVelocity = jumpVelocity
    }
    
    private fun update(deltaTime: Float) {
        if (gameOver) return
        
        // Update bird physics
        birdVelocity = (birdVelocity + gravity * deltaTime).coerceAtMost(maxVelocity)
        birdPosition = birdPosition.copy(y = birdPosition.y + birdVelocity * deltaTime)
        
        // Check for collisions with screen boundaries
        if (birdPosition.y <= 0 || birdPosition.y + birdSize.height >= screenHeight) {
            endGame()
            return
        }
        
        // Update obstacles
        updateObstacles(deltaTime)
        
        // Check for collisions with obstacles
        if (checkCollisions()) {
            endGame()
            return
        }
        
        // Update day/night cycle
        updateDayNightCycle(deltaTime)
        
        // Update difficulty
        updateDifficulty()
    }
    
    private fun updateObstacles(deltaTime: Float) {
        // Move obstacles with AI speed multiplier
        val adjustedSpeed = obstacleSpeed * aiSpeedMultiplier
        obstacles = obstacles.map { obstacle ->
            obstacle.copy(x = obstacle.x - adjustedSpeed * deltaTime)
        }
        
        // Remove obstacles that are off-screen
        obstacles = obstacles.filter { it.x + obstacleWidth > 0 }
        
        // Check if we need to add new obstacles
        if (obstacles.isEmpty() || obstacles.last().x < screenWidth - obstacleSpacing) {
            addObstacle()
        }
        
        // Check for scoring
        obstacles.forEach { obstacle ->
            if (!obstacle.passed && birdPosition.x > obstacle.x + obstacleWidth) {
                obstacle.passed = true
                score++
                onScoreUpdate(score)
                onScoreEffect(Offset(obstacle.x + obstacleWidth, screenHeight / 2))
                
                // Update theme based on new score
                updateTheme()
                
                // Trigger commentary
                if (score % 3 == 0) { // Every 3 points
                    val speaker = if (score % 2 == 0) "mamata" else "modi"
                    onCommentaryUpdate(speaker, "scoring")
                }
            }
        }
    }
    
    private fun generateInitialObstacles() {
        // Add initial obstacles
        for (i in 0 until 3) {
            addObstacle(screenWidth + i * obstacleSpacing)
        }
    }
    
    private fun addObstacle(xPosition: Float = screenWidth) {
        // Calculate the safe zone where the gap can be positioned
        // Leave margin at top and bottom to ensure obstacles are visible
        val topMargin = 100f
        val bottomMargin = 150f // Account for ground
        
        // Apply AI gap multiplier
        val adjustedGap = currentGapHeight * aiGapMultiplier
        val safeZoneHeight = screenHeight - topMargin - bottomMargin - adjustedGap
        
        // Randomly position the gap within the safe zone
        val gapTopPosition = topMargin + (Math.random() * safeZoneHeight).toFloat()
        
        // Top obstacle goes from 0 to gap start
        val topHeight = gapTopPosition
        
        // Bottom obstacle starts after the gap and goes to screen height
        val bottomY = gapTopPosition + adjustedGap
        val bottomHeight = screenHeight - bottomY
        
        val newObstacle = Obstacle(
            x = xPosition,
            topHeight = topHeight,
            bottomY = bottomY,
            bottomHeight = bottomHeight,
            width = obstacleWidth,
            passed = false
        )
        obstacles = obstacles + newObstacle
    }
    
    private fun checkCollisions(): Boolean {
        val birdRect = android.graphics.RectF(
            birdPosition.x,
            birdPosition.y,
            birdPosition.x + birdSize.width,
            birdPosition.y + birdSize.height
        )
        
        obstacles.forEach { obstacle ->
            // Top obstacle collision
            val topObstacleRect = android.graphics.RectF(
                obstacle.x,
                0f,
                obstacle.x + obstacle.width,
                obstacle.topHeight
            )
            
            // Bottom obstacle collision
            val bottomObstacleRect = android.graphics.RectF(
                obstacle.x,
                obstacle.bottomY,
                obstacle.x + obstacle.width,
                obstacle.bottomY + obstacle.bottomHeight
            )
            
            if (birdRect.intersect(topObstacleRect) || birdRect.intersect(bottomObstacleRect)) {
                onCollision(birdPosition)
                return true
            }
        }
        
        return false
    }
    
    private fun updateDayNightCycle(deltaTime: Float) {
        timeElapsed += deltaTime / 60f // Convert to seconds
        if (timeElapsed >= dayNightCycleDuration) {
            timeElapsed = 0f
            isDayTime = !isDayTime
        }
    }
    
    private fun updateDifficulty() {
        val newDifficultyLevel = 1 + score / difficultyIncreaseInterval
        if (newDifficultyLevel > difficultyLevel) {
            difficultyLevel = newDifficultyLevel
            
            // Increase speed gradually
            obstacleSpeed += 0.3f
            
            // Gradually decrease gap (but not below minimum)
            currentGapHeight = (currentGapHeight - 25f).coerceAtLeast(minGapHeight)
            
            // Gradually increase minimum obstacle height
            currentMinObstacleHeight = (currentMinObstacleHeight + 15f).coerceAtMost(maxMinObstacleHeight)
            
            // Gradually increase maximum obstacle height
            currentMaxObstacleHeight = (currentMaxObstacleHeight + 25f).coerceAtMost(maxObstacleHeight)
        }
    }
    
    private fun updateTheme() {
        val newTheme = ThemeManager.getThemeForScore(score)
        if (newTheme != currentTheme) {
            currentTheme = newTheme
        }
    }
    
    private fun endGame() {
        gameOver = true
        isRunning = false
        
        // Track metrics for AI
        hitCount++
        gamesPlayed++
        val gameTime = (System.currentTimeMillis() - gameStartTime) / 1000f
        totalGameTime += gameTime
        
        onGameOver(score)
    }
}

data class Obstacle(
    val x: Float,
    val topHeight: Float,
    val bottomY: Float,
    val bottomHeight: Float,
    val width: Float,
    var passed: Boolean
)