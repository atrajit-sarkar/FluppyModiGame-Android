package com.atrajit.fluppymodigame.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ParticleSystem {
    private var particles by mutableStateOf<List<Particle>>(emptyList())
    private val maxParticles = 50
    
    fun createExplosion(position: Offset, particleCount: Int = 20, color: Color = Color.Yellow) {
        val newParticles = List(particleCount) {
            val angle = Random.nextDouble(0.0, Math.PI * 2).toFloat()
            val speed = Random.nextDouble(1.0, 5.0).toFloat()
            val size = Random.nextDouble(2.0, 8.0).toFloat()
            val lifetime = Random.nextDouble(0.5, 1.5).toFloat()
            
            Particle(
                position = position,
                velocity = Offset(
                    cos(angle) * speed,
                    sin(angle) * speed
                ),
                color = color,
                size = size,
                lifetime = lifetime,
                currentLifetime = 0f
            )
        }
        
        particles = (particles + newParticles).takeLast(maxParticles)
    }
    
    fun update(deltaTime: Float) {
        particles = particles.mapNotNull { particle ->
            val newLifetime = particle.currentLifetime + deltaTime
            if (newLifetime >= particle.lifetime) {
                null // Remove expired particles
            } else {
                particle.copy(
                    position = particle.position + particle.velocity * deltaTime,
                    velocity = particle.velocity.copy(y = particle.velocity.y + 0.1f), // Add gravity
                    currentLifetime = newLifetime
                )
            }
        }
    }
    
    @Composable
    fun Render() {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val alpha = 1f - (particle.currentLifetime / particle.lifetime)
                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.size * (1f - particle.currentLifetime / particle.lifetime * 0.5f),
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
    val currentLifetime: Float
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