package com.atrajit.fluppymodigame.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.atrajit.fluppymodigame.R

@Composable
fun Bird(position: Offset, rotation: Float = 0f) {
    Box(
        modifier = Modifier
            .offset(position.x.dp, position.y.dp)
            .size(60.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.modi),
            contentDescription = "Bird",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(60.dp)
        )
    }
}

@Composable
fun Pipe(x: Float, topHeight: Float, bottomY: Float, width: Float) {
    // Top pipe
    Box(
        modifier = Modifier
            .offset(x.dp, 0.dp)
            .size(width.dp, topHeight.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.mamata),
            contentDescription = "Top Pipe",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(width.dp, topHeight.dp)
        )
    }
    
    // Bottom pipe
    Box(
        modifier = Modifier
            .offset(x.dp, bottomY.dp)
            .size(width.dp, 1000.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.mamata),
            contentDescription = "Bottom Pipe",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(width.dp, 1000.dp)
        )
    }
}

@Composable
fun Background(isDayTime: Boolean) {
    val backgroundColor = if (isDayTime) Color(0xFF87CEEB) else Color(0xFF191970)
    Box(
        modifier = Modifier
            .size(1000.dp, 1000.dp)
            .offset(0.dp, 0.dp)
    ) {
        // Background color is set in the parent Box in GameScreen
    }
}