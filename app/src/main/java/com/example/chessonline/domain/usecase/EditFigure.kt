package com.example.chessonline.domain.usecase

import com.example.chessonline.Figure
import com.example.chessonline.domain.FigureRepository

class EditFigure (
    private val repository: FigureRepository
    ) {
        suspend fun editFigure(figure: Figure) {
            repository.editFigure(figure)
        }
    }