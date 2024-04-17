package com.example.chessonline.domain

import androidx.lifecycle.LiveData
import com.example.chessonline.Figure
import com.example.chessonline.Position

interface FigureRepository {
    suspend fun moveFigureTo(fromPos: Position, toPos: Position)
    suspend fun addFigure(figure: Figure)
    suspend fun getFiguresList(): LiveData<List<Figure>>
    suspend fun removeFigure(position: Position)
}