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
    isDayTime: Boolean,
    backgroundType: BackgroundType = BackgroundType.SPACE
) {
    when (backgroundType) {
        BackgroundType.SPACE -> SpaceBackgroundContent(modifier, isDayTime)
        BackgroundType.DJ_PARTY -> DJPartyBackground(modifier)
        BackgroundType.FIERY_HELL -> FieryHellBackground(modifier)
        BackgroundType.NARUTO_GENJUTSU -> NarutoGenjutsuBackground(modifier)
        BackgroundType.HAUNTED_HOUSE -> HauntedHouseBackground(modifier)
    }
}

@Composable
private fun SpaceBackgroundContent(
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

@Composable
private fun DJPartyBackground(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.016f
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Sunrise gradient sky
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF6B6B), // Orange-red at top
                    Color(0xFFFFD93D), // Yellow in middle
                    Color(0xFF6BCB77), // Light green
                    Color(0xFF4D96FF)  // Ocean blue at bottom
                )
            ),
            size = size
        )
        
        // Sun
        val sunY = size.height * 0.25f
        drawCircle(
            color = Color(0xFFFFA500),
            radius = 80f,
            center = Offset(size.width * 0.7f, sunY)
        )
        
        // Sun glow
        drawCircle(
            color = Color(0xFFFFA500).copy(alpha = 0.3f),
            radius = 120f,
            center = Offset(size.width * 0.7f, sunY)
        )
        
        // Flying birds
        val birdCount = 5
        for (i in 0 until birdCount) {
            val birdX = (time * 50f + i * 200f) % size.width
            val birdY = size.height * 0.3f + sin(time * 2f + i) * 50f
            drawBird(Offset(birdX, birdY))
        }
        
        // Seashore waves at bottom
        val waveHeight = size.height * 0.15f
        val waveY = size.height - waveHeight
        
        drawRect(
            color = Color(0xFF0077BE),
            topLeft = Offset(0f, waveY),
            size = androidx.compose.ui.geometry.Size(size.width, waveHeight)
        )
        
        // Animated waves
        for (i in 0..5) {
            val waveOffset = (time * 100f + i * 100f) % size.width
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 30f,
                center = Offset(waveOffset, waveY + 20f)
            )
        }
    }
}

@Composable
private fun FieryHellBackground(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.016f
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Dark hellish gradient
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A0000), // Dark red at top
                    Color(0xFF330000), // Deeper red
                    Color(0xFF660000), // Bright red
                    Color(0xFFCC0000)  // Intense red at bottom
                )
            ),
            size = size
        )
        
        // Lava pools at bottom
        val lavaY = size.height * 0.85f
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF4500),
                    Color(0xFFFF6B00),
                    Color(0xFFFFFF00)
                )
            ),
            topLeft = Offset(0f, lavaY),
            size = androidx.compose.ui.geometry.Size(size.width, size.height - lavaY)
        )
        
        // Animated flames
        for (i in 0..10) {
            val flameX = (i * size.width / 10f)
            val flameFlicker = sin(time * 5f + i) * 20f
            val flameHeight = 100f + flameFlicker
            
            // Flame shape
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFF00).copy(alpha = 0.8f),
                        Color(0xFFFF6B00).copy(alpha = 0.6f),
                        Color(0xFFFF4500).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                radius = 40f + flameFlicker,
                center = Offset(flameX, lavaY - flameHeight)
            )
        }
        
        // Ember particles floating up
        for (i in 0..20) {
            val emberX = (i * size.width / 20f + sin(time + i) * 50f)
            val emberY = size.height - ((time * 50f + i * 100f) % size.height)
            
            drawCircle(
                color = Color(0xFFFFAA00).copy(alpha = 0.6f),
                radius = 3f,
                center = Offset(emberX, emberY)
            )
        }
    }
}

@Composable
private fun NarutoGenjutsuBackground(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.016f
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Dark genjutsu gradient
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A0033), // Deep purple
                    Color(0xFF330066), // Purple
                    Color(0xFF4D0099), // Brighter purple
                    Color(0xFF0D0D0D)  // Almost black at bottom
                )
            ),
            size = size
        )
        
        // Sharingan-like red circles
        val sharinganCount = 3
        for (i in 0 until sharinganCount) {
            val rotation = time * 30f + i * 120f
            val ringX = size.width * (0.2f + i * 0.3f)
            val ringY = size.height * (0.3f + sin(time + i) * 0.1f)
            
            drawCircle(
                color = Color(0xFFFF0000).copy(alpha = 0.3f),
                radius = 60f,
                center = Offset(ringX, ringY)
            )
            
            drawCircle(
                color = Color(0xFFCC0000).copy(alpha = 0.5f),
                radius = 40f,
                center = Offset(ringX, ringY)
            )
        }
        
        // Itachi's crows
        val crowCount = 8
        for (i in 0 until crowCount) {
            val crowX = (time * 70f + i * 150f) % size.width
            val crowY = size.height * (0.2f + i * 0.08f) + sin(time * 2f + i) * 30f
            
            // Crow silhouette (simple representation)
            drawCircle(
                color = Color.Black.copy(alpha = 0.8f),
                radius = 15f,
                center = Offset(crowX, crowY)
            )
            
            // Crow wings
            drawCircle(
                color = Color.Black.copy(alpha = 0.6f),
                radius = 10f,
                center = Offset(crowX - 15f, crowY)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.6f),
                radius = 10f,
                center = Offset(crowX + 15f, crowY)
            )
            
            // Red eye glow
            drawCircle(
                color = Color(0xFFFF0000).copy(alpha = 0.8f),
                radius = 3f,
                center = Offset(crowX, crowY)
            )
        }
        
        // Purple energy wisps
        for (i in 0..5) {
            val wispX = (time * 40f + i * 200f) % size.width
            val wispY = size.height * 0.5f + sin(time * 3f + i) * 100f
            
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9D50BB).copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                radius = 40f,
                center = Offset(wispX, wispY)
            )
        }
    }
}

@Composable
private fun HauntedHouseBackground(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            time += 0.016f
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Dark spooky night sky
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0D0D1A), // Very dark blue
                    Color(0xFF1A1A2E), // Dark purple-blue
                    Color(0xFF16213E)  // Dark blue
                )
            ),
            size = size
        )
        
        // Full moon
        val moonX = size.width * 0.8f
        val moonY = size.height * 0.2f
        drawCircle(
            color = Color(0xFFE8E8E8),
            radius = 60f,
            center = Offset(moonX, moonY)
        )
        
        // Moon glow
        drawCircle(
            color = Color(0xFFE8E8E8).copy(alpha = 0.3f),
            radius = 80f,
            center = Offset(moonX, moonY)
        )
        
        // Flying bats
        val batCount = 10
        for (i in 0 until batCount) {
            val batX = (time * 80f + i * 120f) % size.width
            val batY = size.height * (0.2f + i * 0.05f) + sin(time * 4f + i) * 40f
            
            // Bat body (simple silhouette)
            drawCircle(
                color = Color.Black,
                radius = 8f,
                center = Offset(batX, batY)
            )
            
            // Bat wings (simplified)
            val wingFlap = sin(time * 10f + i) * 5f
            drawCircle(
                color = Color.Black,
                radius = 12f,
                center = Offset(batX - 12f, batY + wingFlap)
            )
            drawCircle(
                color = Color.Black,
                radius = 12f,
                center = Offset(batX + 12f, batY + wingFlap)
            )
        }
        
        // Haunted house silhouettes at bottom
        val houseY = size.height * 0.7f
        
        // House 1
        drawRect(
            color = Color.Black,
            topLeft = Offset(size.width * 0.1f, houseY),
            size = androidx.compose.ui.geometry.Size(150f, size.height - houseY)
        )
        
        // House 1 roof
        val roof1Path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.1f - 20f, houseY)
            lineTo(size.width * 0.1f + 75f, houseY - 60f)
            lineTo(size.width * 0.1f + 170f, houseY)
            close()
        }
        drawPath(roof1Path, Color.Black)
        
        // Glowing windows
        val windowFlicker = (sin(time * 3f) + 1f) / 2f
        drawRect(
            color = Color(0xFFFFAA00).copy(alpha = 0.6f * windowFlicker),
            topLeft = Offset(size.width * 0.1f + 30f, houseY + 30f),
            size = androidx.compose.ui.geometry.Size(30f, 40f)
        )
        drawRect(
            color = Color(0xFFFFAA00).copy(alpha = 0.6f * windowFlicker),
            topLeft = Offset(size.width * 0.1f + 90f, houseY + 30f),
            size = androidx.compose.ui.geometry.Size(30f, 40f)
        )
        
        // House 2 (smaller, in background)
        drawRect(
            color = Color(0xFF1A1A1A),
            topLeft = Offset(size.width * 0.65f, houseY + 50f),
            size = androidx.compose.ui.geometry.Size(120f, size.height - houseY - 50f)
        )
        
        // Spooky mist at ground level
        for (i in 0..8) {
            val mistX = (time * 20f + i * 150f) % size.width
            val mistY = size.height - 100f + sin(time + i) * 20f
            
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF888888).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                radius = 60f,
                center = Offset(mistX, mistY)
            )
        }
    }
}

// Helper function to draw a simple bird silhouette
private fun DrawScope.drawBird(position: Offset) {
    // Bird body
    drawCircle(
        color = Color.Black,
        radius = 5f,
        center = position
    )
    
    // Bird wings (V shape)
    drawLine(
        color = Color.Black,
        start = position,
        end = Offset(position.x - 10f, position.y - 5f),
        strokeWidth = 2f
    )
    drawLine(
        color = Color.Black,
        start = position,
        end = Offset(position.x + 10f, position.y - 5f),
        strokeWidth = 2f
    )
}

