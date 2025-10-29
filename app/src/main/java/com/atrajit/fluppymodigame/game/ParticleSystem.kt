package com.atrajit.fluppymodigame.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ParticleSystem {
    private var particles by mutableStateOf<List<Particle>>(emptyList())
    private var shockwaves by mutableStateOf<List<Shockwave>>(emptyList())
    private var sparks by mutableStateOf<List<Spark>>(emptyList())
    private val maxParticles = 100
    
    fun createExplosion(position: Offset, particleCount: Int = 30, color: Color = Color.Yellow) {
        val newParticles = List(particleCount) {
            val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
            val speed = Random.nextDouble(3.0, 12.0).toFloat()
            val size = Random.nextDouble(3.0, 12.0).toFloat()
            val lifetime = Random.nextDouble(0.6, 1.8).toFloat()
            
            Particle(
                position = position,
                velocity = Offset(
                    cos(angle) * speed,
                    sin(angle) * speed
                ),
                color = color,
                size = size,
                lifetime = lifetime,
                currentLifetime = 0f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 10f - 5f
            )
        }
        
        // Add shockwave effect
        val newShockwave = Shockwave(
            position = position,
            maxRadius = 150f,
            currentRadius = 0f,
            lifetime = 0.8f,
            currentLifetime = 0f,
            color = color
        )
        
        // Add sparks
        val newSparks = List(15) {
            val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
            val speed = Random.nextDouble(8.0, 20.0).toFloat()
            val length = Random.nextDouble(15.0, 40.0).toFloat()
            
            Spark(
                position = position,
                velocity = Offset(cos(angle) * speed, sin(angle) * speed),
                length = length,
                lifetime = Random.nextDouble(0.3, 0.8).toFloat(),
                currentLifetime = 0f,
                color = Color.White
            )
        }
        
        particles = (particles + newParticles).takeLast(maxParticles)
        shockwaves = (shockwaves + newShockwave).takeLast(5)
        sparks = (sparks + newSparks).takeLast(50)
    }
    
    fun update(deltaTime: Float) {
        // Update particles
        particles = particles.mapNotNull { particle ->
            val newLifetime = particle.currentLifetime + deltaTime
            if (newLifetime >= particle.lifetime) {
                null
            } else {
                particle.copy(
                    position = particle.position + particle.velocity * deltaTime,
                    velocity = particle.velocity.copy(
                        y = particle.velocity.y + 0.3f, // Gravity
                        x = particle.velocity.x * 0.98f  // Air resistance
                    ),
                    currentLifetime = newLifetime,
                    rotation = particle.rotation + particle.rotationSpeed
                )
            }
        }
        
        // Update shockwaves
        shockwaves = shockwaves.mapNotNull { shockwave ->
            val newLifetime = shockwave.currentLifetime + deltaTime
            if (newLifetime >= shockwave.lifetime) {
                null
            } else {
                val progress = newLifetime / shockwave.lifetime
                shockwave.copy(
                    currentRadius = shockwave.maxRadius * progress,
                    currentLifetime = newLifetime
                )
            }
        }
        
        // Update sparks
        sparks = sparks.mapNotNull { spark ->
            val newLifetime = spark.currentLifetime + deltaTime
            if (newLifetime >= spark.lifetime) {
                null
            } else {
                spark.copy(
                    position = spark.position + spark.velocity * deltaTime,
                    velocity = spark.velocity * 0.95f, // Deceleration
                    currentLifetime = newLifetime
                )
            }
        }
    }
    
    @Composable
    fun Render() {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw shockwaves
            shockwaves.forEach { shockwave ->
                val alpha = 1f - (shockwave.currentLifetime / shockwave.lifetime)
                
                // Outer ring
                drawCircle(
                    color = shockwave.color.copy(alpha = alpha * 0.6f),
                    radius = shockwave.currentRadius,
                    center = shockwave.position,
                    style = Stroke(width = 8f)
                )
                
                // Inner ring
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.8f),
                    radius = shockwave.currentRadius * 0.7f,
                    center = shockwave.position,
                    style = Stroke(width = 4f)
                )
            }
            
            // Draw sparks (lightning-like trails)
            sparks.forEach { spark ->
                val alpha = 1f - (spark.currentLifetime / spark.lifetime)
                val endPos = spark.position - spark.velocity.copy(
                    x = spark.velocity.x * (spark.length / spark.velocity.x),
                    y = spark.velocity.y * (spark.length / spark.velocity.y)
                )
                
                // Outer glow
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            spark.color.copy(alpha = alpha * 0.8f),
                            Color.Transparent
                        ),
                        start = spark.position,
                        end = endPos
                    ),
                    start = spark.position,
                    end = endPos,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                
                // Core line
                drawLine(
                    color = spark.color.copy(alpha = alpha),
                    start = spark.position,
                    end = endPos,
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
            
            // Draw particles with enhanced effects
            particles.forEach { particle ->
                val alpha = 1f - (particle.currentLifetime / particle.lifetime)
                val currentSize = particle.size * (1f - particle.currentLifetime / particle.lifetime * 0.5f)
                
                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            particle.color.copy(alpha = alpha * 0.6f),
                            Color.Transparent
                        ),
                        center = particle.position,
                        radius = currentSize * 2f
                    ),
                    radius = currentSize * 2f,
                    center = particle.position
                )
                
                // Core particle
                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = currentSize,
                    center = particle.position
                )
                
                // Inner highlight
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.7f),
                    radius = currentSize * 0.4f,
                    center = particle.position
                )
            }
        }
    }
}

data class Particle(
    val position: Offset,
    val velocity: Offset,
    val color: Color,
    val size: Float,
    val lifetime: Float,
    val currentLifetime: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

data class Shockwave(
    val position: Offset,
    val maxRadius: Float,
    val currentRadius: Float,
    val lifetime: Float,
    val currentLifetime: Float,
    val color: Color
)

data class Spark(
    val position: Offset,
    val velocity: Offset,
    val length: Float,
    val lifetime: Float,
    val currentLifetime: Float,
    val color: Color
)

@Composable
fun ParticleEffect(
    particleSystem: ParticleSystem,
    isActive: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(isActive) {
        if (isActive) {
            coroutineScope.launch {
                while (true) {
                    particleSystem.update(0.016f) // ~60fps
                    delay(16)
                }
            }
        }
    }
    
    particleSystem.Render()
}