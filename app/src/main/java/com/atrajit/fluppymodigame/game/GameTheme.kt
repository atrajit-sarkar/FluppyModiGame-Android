package com.atrajit.fluppymodigame.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.atrajit.fluppymodigame.R

enum class BackgroundType {
    SPACE,
    DJ_PARTY,
    FIERY_HELL,
    NARUTO_GENJUTSU,
    HAUNTED_HOUSE
}

data class GameTheme(
    val scoreRange: IntRange,
    val birdDrawableId: Int,
    val obstacleDrawableId: Int,
    val backgroundType: BackgroundType,
    val backgroundDescription: String
)

object ThemeManager {
    private val themes = listOf(
        GameTheme(
            scoreRange = 0..10,
            birdDrawableId = R.drawable.modi,
            obstacleDrawableId = R.drawable.mamata,
            backgroundType = BackgroundType.SPACE,
            backgroundDescription = "Space with stars and planets"
        ),
        GameTheme(
            scoreRange = 11..20,
            birdDrawableId = R.drawable.glaring_modi,
            obstacleDrawableId = R.drawable.hot_mamata,
            backgroundType = BackgroundType.DJ_PARTY,
            backgroundDescription = "DJ party with sunrise, seashore and flying birds"
        ),
        GameTheme(
            scoreRange = 21..30,
            birdDrawableId = R.drawable.angry_modi,
            obstacleDrawableId = R.drawable.angry_mamata,
            backgroundType = BackgroundType.FIERY_HELL,
            backgroundDescription = "Fiery hell with flames and lava"
        ),
        GameTheme(
            scoreRange = 31..40,
            birdDrawableId = R.drawable.highest_modi,
            obstacleDrawableId = R.drawable.uruchimaru_mamata,
            backgroundType = BackgroundType.NARUTO_GENJUTSU,
            backgroundDescription = "Naruto Shippuden genjutsu with Itachi crows"
        ),
        GameTheme(
            scoreRange = 41..Int.MAX_VALUE,
            birdDrawableId = R.drawable.hulk_modi,
            obstacleDrawableId = R.drawable.mamata_ghost,
            backgroundType = BackgroundType.HAUNTED_HOUSE,
            backgroundDescription = "Spooky haunted houses with bats"
        )
    )
    
    fun getThemeForScore(score: Int): GameTheme {
        return themes.firstOrNull { score in it.scoreRange } ?: themes.first()
    }
    
    fun getAllThemes(): List<GameTheme> = themes
}
