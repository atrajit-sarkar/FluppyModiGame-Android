package com.atrajit.fluppymodigame.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Float,
    val twinkleSpeed: Float
)

data class Planet(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val speed: Float
)

data class Nebula(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val alpha: Float
)

@Composable
fun SpaceBackground(
    modifier: Modifier = Modifier,
    isDayTime: Boolean
) {
    var time by remember { mutableStateOf(0f) }
    var stars by remember {
        mutableStateOf(
            List(150) {
                Star(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    size = Random.nextFloat() * 3f + 1f,
                    brightness = Random.nextFloat() * 0.5f + 0.5f,
                    twinkleSpeed = Random.nextFloat() * 2f + 1f
                )
            }
        )
    }
    
    var planets by remember {
        mutableStateOf(
            List(3) { i ->
                Planet(
                    x = Random.nextFloat(),
                    y = 0.2f + i * 0.3f,
                    radius = Random.nextFloat() * 40f + 30f,
                    color = listOf(
                        Color(0xFFFF6B6B), // Red
                        Color(0xFF4ECDC4), // Cyan
                        Color(0xFFFFE66D)  // Yellow
                    )[i],
                    speed = Random.nextFloat() * 0.0003f + 0.0001f
                )
            }
        )
    }
    
    var nebulas by remember {
        mutableStateOf(
            List(5) {
                Nebula(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    radius = Random.nextFloat() * 150f + 100f,
                    color = listOf(
                        Color(0xFF9D50BB),
                        Color(0xFF6E48AA),
                        Color(0xFF2575FC),
                        Color(0xFF6A82FB),
                        Color(0xFFFC6076)
                    ).random(),
                    alpha = Random.nextFloat() * 0.15f + 0.05f
                )
            }
        )
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.016f
            
            // Update planet positions
            planets = planets.map { planet ->
                var newX = planet.x + planet.speed
                if (newX > 1.2f) newX = -0.2f
                planet.copy(x = newX)
            }
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Background gradient
        val backgroundColor = if (isDayTime) {
            Color(0xFF0A1128) // Dark blue for day
        } else {
            Color(0xFF020111) // Almost black for night
        }
        
        drawRect(
            color = backgroundColor,
            size = size
        )
        
        // Draw nebulas (distant colored clouds)
        nebulas.forEach { nebula ->
            val nebulaX = nebula.x * size.width
            val nebulaY = nebula.y * size.height
            
            drawCircle(
                color = nebula.color.copy(alpha = nebula.alpha),
                radius = nebula.radius,
                center = Offset(nebulaX, nebulaY)
            )
            
            // Add glow effect
            drawCircle(
                color = nebula.color.copy(alpha = nebula.alpha * 0.5f),
                radius = nebula.radius * 1.5f,
                center = Offset(nebulaX, nebulaY)
            )
        }
        
        // Draw planets
        planets.forEach { planet ->
            val planetX = planet.x * size.width
            val planetY = planet.y * size.height
            
            // Planet shadow/depth
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = planet.radius,
                center = Offset(planetX + 5f, planetY + 5f)
            )
            
            // Planet body
            drawCircle(
                color = planet.color,
                radius = planet.radius,
                center = Offset(planetX, planetY)
            )
            
            // Planet highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = planet.radius * 0.4f,
                center = Offset(planetX - planet.radius * 0.3f, planetY - planet.radius * 0.3f)
            )
        }
        
        // Draw stars with twinkling effect
        stars.forEach { star ->
            val starX = star.x * size.width
            val starY = star.y * size.height
            
            // Calculate twinkle effect
            val twinkle = (sin(time * star.twinkleSpeed) + 1f) / 2f
            val alpha = star.brightness * twinkle
            
            // Draw star
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.size,
                center = Offset(starX, starY)
            )
            
            // Draw star glow for larger stars
            if (star.size > 2f) {
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.3f),
                    radius = star.size * 2f,
                    center = Offset(starX, starY)
                )
            }
        }
        
        // Draw shooting stars occasionally
        if (time % 5f < 0.5f) {
            val progress = (time % 5f) / 0.5f
            val startX = size.width * 0.8f
            val startY = size.height * 0.2f
            val endX = startX + 200f * progress
            val endY = startY + 100f * progress
            
            drawLine(
                color = Color.White.copy(alpha = (1f - progress) * 0.8f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
            
            // Shooting star trail
            drawLine(
                color = Color(0xFF64B5F6).copy(alpha = (1f - progress) * 0.4f),
                start = Offset(startX, startY),
                end = Offset(startX + 100f * progress, startY + 50f * progress),
                strokeWidth = 4f
            )
        }
    }
}
