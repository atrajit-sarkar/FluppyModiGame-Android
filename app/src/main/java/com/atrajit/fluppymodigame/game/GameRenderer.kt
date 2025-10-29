package com.atrajit.fluppymodigame.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.atrajit.fluppymodigame.R

@Composable
fun GameRenderer(gameEngine: GameEngine) {
    val density = LocalDensity.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Obstacles (pipes)
        gameEngine.obstacles.forEach { obstacle ->
            // Top pipe
            Image(
                painter = painterResource(id = R.drawable.mamata),
                contentDescription = "Top Pipe",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(
                        x = with(density) { obstacle.x.toDp() },
                        y = 0.dp
                    )
                    .size(
                        width = with(density) { obstacle.width.toDp() },
                        height = with(density) { obstacle.topHeight.toDp() }
                    )
            )
            
            // Bottom pipe
            Image(
                painter = painterResource(id = R.drawable.mamata),
                contentDescription = "Bottom Pipe",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(
                        x = with(density) { obstacle.x.toDp() },
                        y = with(density) { obstacle.bottomY.toDp() }
                    )
                    .size(
                        width = with(density) { obstacle.width.toDp() },
                        height = with(density) { obstacle.bottomHeight.toDp() }
                    )
            )
        }
        
        // Bird
        val birdRotation = if (gameEngine.birdVelocity > 0) {
            (gameEngine.birdVelocity * 2).coerceAtMost(30f)
        } else {
            (gameEngine.birdVelocity * 2).coerceAtLeast(-30f)
        }
        
        Image(
            painter = painterResource(id = R.drawable.modi),
            contentDescription = "Bird",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset(
                    x = with(density) { gameEngine.birdPosition.x.toDp() },
                    y = with(density) { gameEngine.birdPosition.y.toDp() }
                )
                .size(with(density) { gameEngine.birdSize.width.toDp() })
                .rotate(birdRotation)
        )
        
        // Ground
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color(0xFF8B4513),
                topLeft = Offset(0f, size.height - 50f),
                size = androidx.compose.ui.geometry.Size(size.width, 50f)
            )
        }
    }
}