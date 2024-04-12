package com.example.chessonline.domain.usecase

import androidx.lifecycle.LiveData
import com.example.chessonline.Figure
import com.example.chessonline.Position
import com.example.chessonline.domain.FigureRepository

class GetFiguresList (
    private val repository: FigureRepository
) {
    suspend fun getFiguresList(): LiveData<List<Figure>> {
        return repository.getFiguresList()
    }
}