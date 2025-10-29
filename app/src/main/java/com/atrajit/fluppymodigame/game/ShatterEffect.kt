package com.atrajit.fluppymodigame.game

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ShatterPiece(
    val position: Offset,
    val velocity: Offset,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val lifetime: Float,
    val currentLifetime: Float,
    val angularVelocity: Float
)

class ShatterEffect {
    var shatterPieces by mutableStateOf<List<ShatterPiece>>(emptyList())
    var isActive by mutableStateOf(false)
    
    fun trigger(position: Offset, size: Float) {
        isActive = true
        val pieceCount = 20
        val pieces = List(pieceCount) {
            val angle = (it * (360f / pieceCount)) * (Math.PI / 180f).toFloat()
            val speed = Random.nextFloat() * 8f + 8f
            val pieceSize = size / 4f + Random.nextFloat() * (size / 6f)
            
            ShatterPiece(
                position = position,
                velocity = Offset(
                    cos(angle) * speed,
                    sin(angle) * speed - Random.nextFloat() * 5f
                ),
                size = pieceSize,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 20f - 10f,
                lifetime = 1.5f,
                currentLifetime = 0f,
                angularVelocity = Random.nextFloat() * 360f - 180f
            )
        }
        shatterPieces = pieces
    }
    
    fun update(deltaTime: Float) {
        if (!isActive) return
        
        shatterPieces = shatterPieces.mapNotNull { piece ->
            val newLifetime = piece.currentLifetime + deltaTime
            if (newLifetime >= piece.lifetime) {
                null
            } else {
                piece.copy(
                    position = piece.position + piece.velocity * deltaTime,
                    velocity = piece.velocity.copy(
                        y = piece.velocity.y + 0.5f, // Gravity
                        x = piece.velocity.x * 0.98f
                    ),
                    rotation = piece.rotation + piece.angularVelocity * deltaTime,
                    currentLifetime = newLifetime
                )
            }
        }
        
        if (shatterPieces.isEmpty()) {
            isActive = false
        }
    }
    
    fun reset() {
        shatterPieces = emptyList()
        isActive = false
    }
}
