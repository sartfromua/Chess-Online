package com.example.chessonline

data class Figure(
    val name: String,
    val team: String,
    var position: Position,
    val id: Int = 0
)

data class Position(
    var x: Int,
    var y: Int
)

data class Coordinates(
    val x: Float,
    val y: Float
)