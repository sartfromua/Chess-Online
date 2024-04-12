package com.example.chessonline.presentation

import com.example.chessonline.Coordinates

object BoardData {
    fun getBoardCoordinates(xMax: Int, yMax: Int): Pair<Coordinates, Coordinates> {
        val boardWidth = xMax * 0.9
        val startCoordinates = Coordinates(xMax * 0.05F, (yMax/2-boardWidth/2).toFloat())
        val endCoordinates = Coordinates(xMax*0.95F, (yMax/2+boardWidth/2).toFloat())
        return Pair(startCoordinates, endCoordinates)
    }

    fun getCellSize(xMax: Int): Float {
        return (xMax*0.9/8).toFloat()
    }

}