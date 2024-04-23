package com.example.chessonline.domain.usecase

import com.example.chessonline.Figure
import com.example.chessonline.domain.FigureRepository

class AddFiguresList (
    private val repository: FigureRepository
) {
    suspend fun addFiguresList(figures: List<Figure>) {
        repository.addFiguresList(figures)
    }
}