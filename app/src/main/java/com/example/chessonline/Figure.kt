package com.example.chessonline

data class Figure(
    val name: String,
    val team: String,
    var position: Position,
)

data class Position(
    val x: Int,
    val y: Int
)

data class Coordinates(
    val x: Float,
    val y: Float
)