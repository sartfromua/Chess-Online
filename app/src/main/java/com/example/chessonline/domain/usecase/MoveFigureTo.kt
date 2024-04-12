package com.example.chessonline.domain.usecase

import com.example.chessonline.Position
import com.example.chessonline.domain.FigureRepository

class MoveFigureTo (
    private val repository: FigureRepository
) {
    suspend fun moveFigureTo(fromPos: Position, toPos: Position) {
        repository.moveFigureTo(fromPos, toPos)
    }
}