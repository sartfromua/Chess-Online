package com.example.chessonline.domain.usecase

import com.example.chessonline.domain.FigureRepository

class RemoveAllFigures(
    private val repository: FigureRepository
) {
    suspend fun removeAllFigures() {
        repository.removeAllFigures()
    }
}
