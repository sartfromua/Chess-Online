package com.example.chessonline.data.local

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.chessonline.Figure
import com.example.chessonline.Position
import com.example.chessonline.domain.FigureRepository

class FigureDataBaseRepository(context: Context): FigureRepository {
    val dao = FigureDB.getInstance(context).getFigureDao()

    override suspend fun moveFigureTo(fromPos: Position, toPos: Position) {
        val figure = dao.getFigureOnPosition(fromPos.x, fromPos.y)
        Log.d("XXXXX", "BD Moving $figure")
        figure.position.x = toPos.x
        figure.position.y = toPos.y
        dao.removeFigure(toPos.x, toPos.y)
        dao.updateFigure(figure)
    }

    override suspend fun addFigure(figure: Figure) {
        dao.addFigure(EntityMapper.figureToEntity(figure))
    }

    override suspend fun editFigure(figure: Figure) {
        dao.removeFigure(figure.position.x, figure.position.y)
        dao.addFigure(EntityMapper.figureToEntity(figure))
//        dao.updateFigure(EntityMapper.figureToEntity(figure))
    }

    override suspend fun getFiguresList(): LiveData<List<Figure>> {
//        return MediatorLiveData<List<Figure>>().apply {
//            addSource(dao.getFiguresList()) {
//                value = EntityMapper.entitiesToFigureList(it)
//            }
//        }
//        return MediatorLiveData<List<Figure>>().apply {
//            this.value = EntityMapper.entitiesToFigureList(dao.getFiguresList())
//        }
        Log.d("XXXXX", "Call to BD to get figureListLD: ${MediatorLiveData(
            EntityMapper.entitiesToFigureList(
                dao.getFiguresList()
            )
        ).value}")
        return MediatorLiveData(EntityMapper.entitiesToFigureList(dao.getFiguresList()))
    }

    override suspend fun removeFigure(position: Position) {
        Log.d("XXXXX", "DELETING $position")
        dao.removeFigure(position.x, position.y)
    }

    override suspend fun removeAllFigures() {
        dao.removeAllFigures()
    }

    override suspend fun addFiguresList(figures: List<Figure>) {
        dao.addFiguresList(EntityMapper.figuresToEntities(figures))
    }


}