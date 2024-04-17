package com.example.chessonline.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chessonline.Figure
import com.example.chessonline.Position
import com.example.chessonline.data.FigureDataBaseRepository
import com.example.chessonline.domain.usecase.AddFigure
import com.example.chessonline.domain.usecase.GetFiguresList
import com.example.chessonline.domain.usecase.MoveFigureTo
import com.example.chessonline.domain.usecase.RemoveFigure
import kotlinx.coroutines.launch

class FiguresViewModel(application: Application): AndroidViewModel(application) {
    private val repository = FigureDataBaseRepository(application)

    private val addFigureUseCase = AddFigure(repository)
    private val getFiguresListUseCase = GetFiguresList(repository)
    private val moveFigureToUseCase = MoveFigureTo(repository)
    private val removeFigureUseCase = RemoveFigure(repository)

    private var _figuresListLD = MutableLiveData<List<Figure>>()
    val figuresListLD: LiveData<List<Figure>>
        get() = _figuresListLD

    fun getFiguresList() {
        viewModelScope.launch {
            _figuresListLD.value = getFiguresListUseCase.getFiguresList().value
        }
    }

    fun addFigure(figure: Figure) {
        viewModelScope.launch {
            addFigureUseCase.addFigure(figure)
        }
    }

    fun moveFigureTo(fromPosition: Position, toPosition: Position) {
        viewModelScope.launch {
            moveFigureToUseCase.moveFigureTo(fromPosition, toPosition)
        }.invokeOnCompletion {
            getFiguresList()
        }
    }

    fun removeFigure(position: Position) {
        viewModelScope.launch {
            removeFigureUseCase.removeFigure(position)
        }
    }

    fun addFiguresList(figures: List<Figure>) {
        for (fig in figures) {
            addFigure(fig)
        }
    }
}