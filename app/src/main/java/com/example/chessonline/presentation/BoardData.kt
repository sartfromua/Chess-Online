package com.example.chessonline.presentation

import com.example.chessonline.BISHOP_NAME
import com.example.chessonline.BLACK_TEAM
import com.example.chessonline.Coordinates
import com.example.chessonline.Figure
import com.example.chessonline.HORSE_NAME
import com.example.chessonline.KING_NAME
import com.example.chessonline.PAWN_NAME
import com.example.chessonline.Position
import com.example.chessonline.QUEEN_NAME
import com.example.chessonline.ROOK_NAME
import com.example.chessonline.WHITE_TEAM

object BoardData {
    fun getBoardCoordinates(xMax: Int, yMax: Int): Pair<Coordinates, Coordinates> {
        val boardWidth = xMax * 0.9
        val startCoordinates = Coordinates(xMax * 0.05F, (yMax/2-boardWidth/2).toFloat())
        val endCoordinates = Coordinates(xMax*0.95F, (yMax/2+boardWidth/2).toFloat())
        return Pair(startCoordinates, endCoordinates)
    }

    fun getRestartButtonCoordinates(xMax: Int, yMax: Int): Pair<Coordinates, Coordinates> {
        val startCoordinates = Coordinates(xMax * 0.25F, yMax * 0.05F)
        val endCoordinates = Coordinates(xMax * 0.75F, yMax * 0.13F)
        return Pair(startCoordinates, endCoordinates)
    }

    fun getCellSize(xMax: Int): Float {
        return (xMax*0.9/8).toFloat()
    }

    fun getStartFigures(team: String): MutableList<Figure> {
        val figures = mutableListOf<Figure>()
        var id = 1

        for (x in 1..8) figures.add(Figure(PAWN_NAME, WHITE_TEAM, Position(x,2), id++))
        figures.add(Figure(ROOK_NAME, WHITE_TEAM, Position(1, 1), id++))
        figures.add(Figure(ROOK_NAME, WHITE_TEAM, Position(8, 1), id++))
        figures.add(Figure(HORSE_NAME, WHITE_TEAM, Position(2, 1), id++))
        figures.add(Figure(HORSE_NAME, WHITE_TEAM, Position(7, 1), id++))
        figures.add(Figure(BISHOP_NAME, WHITE_TEAM, Position(3, 1), id++))
        figures.add(Figure(BISHOP_NAME, WHITE_TEAM, Position(6, 1), id++))
        figures.add(Figure(QUEEN_NAME, WHITE_TEAM, Position(4, 1), id++))
        figures.add(Figure(KING_NAME, WHITE_TEAM, Position(5, 1), id++))

        for (x in 1..8) figures.add(Figure(PAWN_NAME, BLACK_TEAM, Position(x,7), id++))
        figures.add(Figure(ROOK_NAME, BLACK_TEAM, Position(1, 8), id++))
        figures.add(Figure(ROOK_NAME, BLACK_TEAM, Position(8, 8), id++))
        figures.add(Figure(HORSE_NAME, BLACK_TEAM, Position(2, 8), id++))
        figures.add(Figure(HORSE_NAME, BLACK_TEAM, Position(7, 8), id++))
        figures.add(Figure(BISHOP_NAME, BLACK_TEAM, Position(3, 8), id++))
        figures.add(Figure(BISHOP_NAME, BLACK_TEAM, Position(6, 8), id++))
        figures.add(Figure(QUEEN_NAME, BLACK_TEAM, Position(4, 8), id++))
        figures.add(Figure(KING_NAME, BLACK_TEAM, Position(5, 8), id))

        return figures
    }

}