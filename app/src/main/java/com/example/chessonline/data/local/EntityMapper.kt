package com.example.chessonline.data.local

import com.example.chessonline.Figure
import com.example.chessonline.Position

object EntityMapper {
    fun figureToEntity(figure: Figure): FigureEntity {
        return FigureEntity(
            figure.name,
            figure.team,
            PositionEntity(figure.position.x, figure.position.y)
        )
    }

    fun entityToFigure(figureEntity: FigureEntity): Figure {
        return Figure(
            figureEntity.name,
            figureEntity.team,
            Position(figureEntity.position.x, figureEntity.position.y)
        )
    }

    fun entitiesToFigureList(entities: List<FigureEntity>): List<Figure> {
        return entities.map { entityToFigure(it) }
    }

    fun figuresToEntities(figures: List<Figure>): List<FigureEntity> {
        return figures.map { figureToEntity(it) }
    }
}