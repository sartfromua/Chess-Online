package com.example.chessonline.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.chessonline.Figure
import com.example.chessonline.Position
import com.example.chessonline.domain.FigureRepository

class FigureDataBaseRepository(context: Context): FigureRepository {
    val dao = FigureDB.getInstance(context).getFigureDao()

    override suspend fun moveFigureTo(fromPos: Position, toPos: Position): Boolean {
        val figure = dao.getFigureOnPosition(fromPos.x, fromPos.y)
        return if (figureCanGo(figure, toPos)) {
            figure.position.x = toPos.x
            figure.position.y = toPos.y
            dao.removeFigure(toPos.x, toPos.y)
            dao.updateFigure(figure)
            true
        } else false
    }

    override suspend fun addFigure(figure: Figure) {
        dao.addFigure(EntityMapper.figureToEntity(figure))
    }

    override suspend fun getFiguresList(): LiveData<List<Figure>> {
//        return MediatorLiveData<List<Figure>>().apply {
//            addSource(dao.getFiguresList()) {
//                value = EntityMapper.entitiesToFigureList(it)
//            }
//        }
        return MediatorLiveData<List<Figure>>().apply {
            this.value = EntityMapper.entitiesToFigureList(dao.getFiguresList())
        }
    }

    override suspend fun removeFigure(position: Position) {
        dao.removeFigure(position.x, position.y)
    }


    private fun figureCanGo(figure: FigureEntity, toPos: Position): Boolean {
        return true
    }

}