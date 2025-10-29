package com.atrajit.fluppymodigame.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.atrajit.fluppymodigame.R
import kotlin.math.abs

@Composable
fun GameRenderer(gameEngine: GameEngine) {
    val density = LocalDensity.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Obstacles (pipes) with futuristic glow
        gameEngine.obstacles.forEach { obstacle ->
            // Calculate pulsing effect based on position
            val pulseAlpha = 0.3f + (kotlin.math.sin(obstacle.x / 100f) * 0.2f).toFloat()
            
            // Top pipe with glow
            Box {
                // Glow effect behind
                Image(
                    painter = painterResource(id = R.drawable.mamata),
                    contentDescription = "Top Pipe Glow",
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
                        .blur(8.dp)
                        .alpha(pulseAlpha)
                )
                
                // Main pipe with shadow
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
                        .shadow(
                            elevation = 8.dp,
                            spotColor = Color(0xFF00BCD4),
                            ambientColor = Color(0xFF00BCD4)
                        )
                )
            }
            
            // Bottom pipe with glow
            Box {
                // Glow effect behind
                Image(
                    painter = painterResource(id = R.drawable.mamata),
                    contentDescription = "Bottom Pipe Glow",
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
                        .blur(8.dp)
                        .alpha(pulseAlpha)
                )
                
                // Main pipe
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
                        .shadow(
                            elevation = 8.dp,
                            spotColor = Color(0xFF00BCD4),
                            ambientColor = Color(0xFF00BCD4)
                        )
                )
            }
            
            // Draw gap highlight
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gapY = obstacle.topHeight
                val gapHeight = obstacle.bottomY - obstacle.topHeight
                val gapX = obstacle.x
                
                // Draw glowing gap indicator
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x0000FF00),
                            Color(0x4000FF00),
                            Color(0x0000FF00)
                        )
                    ),
                    topLeft = Offset(gapX, gapY),
                    size = androidx.compose.ui.geometry.Size(obstacle.width, gapHeight)
                )
            }
        }
        
        // Bird with professional effects
        val birdRotation = if (gameEngine.birdVelocity > 0) {
            (gameEngine.birdVelocity * 2).coerceAtMost(30f)
        } else {
            (gameEngine.birdVelocity * 2).coerceAtLeast(-30f)
        }
        
        // Calculate flap animation
        val flapScale = 1f + (abs(kotlin.math.sin(System.currentTimeMillis() / 100.0)) * 0.1f).toFloat()
        
        Box {
            // Bird glow/aura
            Image(
                painter = painterResource(id = R.drawable.modi),
                contentDescription = "Bird Aura",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset(
                        x = with(density) { gameEngine.birdPosition.x.toDp() },
                        y = with(density) { gameEngine.birdPosition.y.toDp() }
                    )
                    .size(with(density) { (gameEngine.birdSize.width * 1.2f).toDp() })
                    .scale(flapScale * 1.1f)
                    .rotate(birdRotation)
                    .blur(6.dp)
                    .alpha(0.5f)
            )
            
            // Main bird with shadow
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
                    .scale(flapScale)
                    .rotate(birdRotation)
                    .shadow(
                        elevation = 12.dp,
                        spotColor = Color(0xFFFFEB3B),
                        ambientColor = Color(0xFFFFEB3B)
                    )
            )
            
            // Energy trail effect
            Canvas(
                modifier = Modifier
                    .offset(
                        x = with(density) { gameEngine.birdPosition.x.toDp() },
                        y = with(density) { gameEngine.birdPosition.y.toDp() }
                    )
                    .size(with(density) { gameEngine.birdSize.width.toDp() })
            ) {
                val centerOffset = Offset(size.width / 2, size.height / 2)
                
                // Draw energy circles
                for (i in 0..2) {
                    drawCircle(
                        color = Color(0xFFFFEB3B).copy(alpha = 0.2f - i * 0.06f),
                        radius = (size.width / 2) + (i * 10f),
                        center = centerOffset,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
        
        // Futuristic ground with gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            val groundHeight = 80f
            val groundY = size.height - groundHeight
            
            // Ground gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF0D47A1),
                        Color(0xFF01579B)
                    ),
                    startY = groundY,
                    endY = size.height
                ),
                topLeft = Offset(0f, groundY),
                size = androidx.compose.ui.geometry.Size(size.width, groundHeight)
            )
            
            // Ground glow line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00BCD4),
                        Color(0xFF00E5FF),
                        Color(0xFF00BCD4),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, groundY),
                end = Offset(size.width, groundY),
                strokeWidth = 4f
            )
            
            // Grid pattern on ground
            val gridSpacing = 50f
            for (i in 0..size.width.toInt() step gridSpacing.toInt()) {
                drawLine(
                    color = Color(0x30FFFFFF),
                    start = Offset(i.toFloat(), groundY),
                    end = Offset(i.toFloat(), size.height),
                    strokeWidth = 1f
                )
            }
        }
    }
}