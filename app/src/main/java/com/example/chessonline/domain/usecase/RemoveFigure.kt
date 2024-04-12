package com.example.chessonline.domain.usecase

import com.example.chessonline.Position
import com.example.chessonline.domain.FigureRepository

class RemoveFigure (
    private val repository: FigureRepository
) {
    suspend fun removeFigure(position: Position) {
        repository.removeFigure(position)
    }
}