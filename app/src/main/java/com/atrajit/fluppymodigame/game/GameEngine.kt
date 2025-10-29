package com.atrajit.fluppymodigame.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
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
    private val onScoreEffect: (Offset) -> Unit = {}
) {
    // Game state
    var isRunning by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var gameOver by mutableStateOf(false)
    var score by mutableStateOf(0)
    
    // Bird properties
    var birdPosition by mutableStateOf(Offset(screenWidth / 3, screenHeight / 2))
    var birdVelocity by mutableStateOf(0f)
    val birdSize = Size(60f, 60f)
    
    // Physics constants
    private val gravity = 0.5f
    private val jumpVelocity = -10f
    private val maxVelocity = 15f
    
    // Obstacles
    var obstacles by mutableStateOf<List<Obstacle>>(emptyList())
    private val gapHeight = 200f
    private val obstacleWidth = 80f
    private val minObstacleHeight = 100f
    private val obstacleSpacing = 300f
    private var obstacleSpeed = 4f
    
    // Game loop
    private var gameJob: Job? = null
    private val gameScope = CoroutineScope(Dispatchers.Default)
    
    // Day/Night cycle
    var isDayTime by mutableStateOf(true)
    private var timeElapsed = 0f
    private val dayNightCycleDuration = 30f // seconds
    
    // Difficulty progression
    private var difficultyLevel = 1
    private val difficultyIncreaseInterval = 10 // points
    
    init {
        resetGame()
    }
    
    fun startGame() {
        if (isRunning) return
        
        resetGame()
        isRunning = true
        gameOver = false
        
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
        birdPosition = Offset(screenWidth / 3, screenHeight / 2)
        birdVelocity = 0f
        obstacles = emptyList()
        score = 0
        gameOver = false
        isPaused = false
        obstacleSpeed = 4f
        difficultyLevel = 1
        timeElapsed = 0f
        isDayTime = true
        
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
        // Move obstacles
        obstacles = obstacles.map { obstacle ->
            obstacle.copy(x = obstacle.x - obstacleSpeed * deltaTime)
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
        val topHeight = (minObstacleHeight + Math.random() * (screenHeight - gapHeight - 2 * minObstacleHeight)).toFloat()
        val newObstacle = Obstacle(
            x = xPosition,
            topHeight = topHeight,
            bottomY = topHeight + gapHeight,
            bottomHeight = screenHeight - topHeight - gapHeight,
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
            obstacleSpeed += 0.5f
        }
    }
    
    private fun endGame() {
        gameOver = true
        isRunning = false
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