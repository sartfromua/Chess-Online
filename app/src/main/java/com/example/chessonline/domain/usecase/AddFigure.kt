package com.example.chessonline.domain.usecase

import com.example.chessonline.Figure
import com.example.chessonline.Position
import com.example.chessonline.domain.FigureRepository

class AddFigure (
    private val repository: FigureRepository
) {
    suspend fun addFigure(figure: Figure) {
        repository.addFigure(figure)
    }
}